"""Minimal DEX reader/patcher.

list  <dex> <class-desc>            -> shows matching methods and code offsets
noop  <dex> <class-desc> <name>     -> rewrites each matching code item to
                                       `return-void` in place and fixes the
                                       header checksum/signature
"""

import struct
import sys
import zlib
import hashlib


class Dex:
    def __init__(self, path):
        self.path = path
        self.data = bytearray(open(path, "rb").read())
        d = self.data
        assert d[:4] == b"dex\n", "not a dex"
        (
            self.file_size,
            self.header_size,
            self.endian,
            self.link_size,
            self.link_off,
            self.map_off,
        ) = struct.unpack_from("<IIIIII", d, 32)
        (self.string_ids_size, self.string_ids_off) = struct.unpack_from("<II", d, 56)
        (self.type_ids_size, self.type_ids_off) = struct.unpack_from("<II", d, 64)
        (self.proto_ids_size, self.proto_ids_off) = struct.unpack_from("<II", d, 72)
        (self.method_ids_size, self.method_ids_off) = struct.unpack_from("<II", d, 88)
        (self.class_defs_size, self.class_defs_off) = struct.unpack_from("<II", d, 96)

    def string(self, idx):
        off = struct.unpack_from("<I", self.data, self.string_ids_off + 4 * idx)[0]
        o = off
        # uleb128 utf16 length
        while self.data[o] & 0x80:
            o += 1
        o += 1
        end = self.data.index(b"\x00", o)
        return self.data[o:end].decode("utf-8", "replace")

    def type_desc(self, idx):
        return self.string(
            struct.unpack_from("<I", self.data, self.type_ids_off + 4 * idx)[0]
        )

    def proto(self, idx):
        shorty, ret, params_off = struct.unpack_from(
            "<III", self.data, self.proto_ids_off + 12 * idx
        )
        return self.type_desc(ret), params_off

    def methods(self):
        out = []
        for i in range(self.method_ids_size):
            class_idx, proto_idx, name_idx = struct.unpack_from(
                "<HHI", self.data, self.method_ids_off + 8 * i
            )
            ret, params_off = self.proto(proto_idx)
            out.append(
                (i, self.type_desc(class_idx), self.string(name_idx), ret, params_off)
            )
        return out

    def class_data_methods(self):
        """Yield (method_idx, access_flags, code_off) for every method with code."""
        for c in range(self.class_defs_size):
            base = self.class_defs_off + 32 * c
            class_idx, _flags, _super, _ifaces, _src, _ann, class_data_off, _sv = (
                struct.unpack_from("<IIIIIIII", self.data, base)
            )
            if class_data_off == 0:
                continue
            o = class_data_off
            vals = []
            for _ in range(4):
                v, o = self._uleb(o)
                vals.append(v)
            sfields, ifields, dmethods, vmethods = vals
            for _ in range(sfields + ifields):
                _, o = self._uleb(o)
                _, o = self._uleb(o)
            for _ in range(dmethods + vmethods):
                idx_diff, o = self._uleb(o)
                flags, o = self._uleb(o)
                code_off, o = self._uleb(o)
                yield class_idx, idx_diff, flags, code_off

    def _uleb(self, o):
        result = 0
        shift = 0
        while True:
            b = self.data[o]
            o += 1
            result |= (b & 0x7F) << shift
            if not b & 0x80:
                return result, o
            shift += 7

    def code_items(self):
        """method_idx -> code_off using cumulative diffs inside each class."""
        for class_idx, idx_diff, flags, code_off in self.class_data_methods():
            yield class_idx, idx_diff, flags, code_off

    def fix_header(self):
        d = self.data
        d[12:32] = hashlib.sha1(bytes(d[32:])).digest()
        struct.pack_into("<I", d, 8, zlib.adler32(bytes(d[12:])) & 0xFFFFFFFF)
        struct.pack_into("<I", d, 32, len(d))

    def save(self, path=None):
        open(path or self.path, "wb").write(self.data)


def params_desc(dex, params_off):
    if params_off == 0:
        return "()"
    size = struct.unpack_from("<I", dex.data, params_off)[0]
    out = []
    for i in range(size):
        idx = struct.unpack_from("<H", dex.data, params_off + 4 + 2 * i)[0]
        out.append(dex.type_desc(idx))
    return "(" + "".join(out) + ")"


def find_methods(dex, class_desc, name, sig=None):
    found = []
    for mid, cls, mname, ret, params in dex.methods():
        if cls == class_desc and mname == name:
            if sig is not None and f"{params_desc(dex, params)}{ret}" != sig:
                continue
            found.append((mid, ret, params))
    return found


def code_offsets(dex, wanted):
    """Return {method_idx: code_off} for the wanted method indices."""
    result = {}
    for class_idx, idx_diff, flags, code_off in dex.class_data_methods():
        # diffs are cumulative per class in method_ids order
        pass
    # simpler: walk each class and accumulate
    for c in range(dex.class_defs_size):
        base = dex.class_defs_off + 32 * c
        class_data_off = struct.unpack_from("<I", dex.data, base + 24)[0]
        if class_data_off == 0:
            continue
        o = class_data_off
        vals = []
        for _ in range(4):
            v, o = dex._uleb(o)
            vals.append(v)
        sfields, ifields, dmethods, vmethods = vals
        for _ in range(sfields + ifields):
            _, o = dex._uleb(o)
            _, o = dex._uleb(o)
        for kind in range(2):
            method_idx = 0
            for _ in range(dmethods if kind == 0 else vmethods):
                idx_diff, o = dex._uleb(o)
                flags, o = dex._uleb(o)
                code_off, o = dex._uleb(o)
                method_idx += idx_diff
                if method_idx in wanted:
                    result[method_idx] = code_off
    return result


def main():
    mode, path, class_desc = sys.argv[1], sys.argv[2], sys.argv[3]
    dex = Dex(path)

    if mode == "list":
        list_sig = sys.argv[5] if len(sys.argv) > 5 else None
        for mid, ret, params in find_methods(
            dex, class_desc, sys.argv[4] if len(sys.argv) > 4 else "", list_sig
        ):
            print(f"method_idx={mid} ret={ret} params_off={params}")
        return

    name = sys.argv[4]
    sig = sys.argv[5] if len(sys.argv) > 5 else None
    targets = find_methods(dex, class_desc, name, sig)
    if not targets:
        raise SystemExit("method not found")
    offsets = code_offsets(dex, {m[0] for m in targets})
    print(f"alvos: {[(m[0], m[1]) for m in targets]}")

    for mid, ret, _params in targets:
        if ret != "V":
            print(f"  pulando {mid}: retorno {ret} nao e void")
            continue
        co = offsets.get(mid)
        if not co:
            print(f"  pulando {mid}: sem codigo")
            continue
        regs, ins, outs, tries, dbg, insns = struct.unpack_from("<HHHHII", dex.data, co)
        old_len = 16 + 2 * insns
        if old_len < 18:
            print(f"  pulando {mid}: code item pequeno demais")
            continue
        # keep the item's aligned footprint so the next item stays aligned and
        # no non-zero padding is left for the verifier to trip on
        aligned = (old_len + 3) & ~3
        words = (aligned - 16) // 2
        body = struct.pack("<HHHHII", ins, ins, 0, 0, 0, words)
        body += struct.pack(f"<{words}H", 0x000E, *([0x0000] * (words - 1)))
        assert len(body) == aligned, (len(body), aligned)
        dex.data[co : co + len(body)] = body
        print(
            f"  patch {mid} @ {co:#x}: regs={regs} ins={ins} insns={insns} bytes={old_len} -> return-void"
        )

    dex.fix_header()
    dex.save()
    print("salvo:", path)


if __name__ == "__main__":
    main()
