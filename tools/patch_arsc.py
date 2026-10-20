"""Replace strings inside the global ResStringPool of resources.arsc.

The pool is UTF-8 in this APK. Entries are rebuilt from their original bytes
except for the ones being replaced, so untouched strings stay byte-identical.
The package data after the pool is shifted by the size delta and the table
header size is updated.
"""

import struct
import sys

UTF8_FLAG = 0x100


def read_entry(data, start):
    """Return (text, raw_bytes, next_offset) for one UTF-8 pool entry."""
    o = start
    n = data[o]
    o += 1
    if n & 0x80:
        n = ((n & 0x7F) << 8) | data[o]
        o += 1
    blen = data[o]
    o += 1
    if blen & 0x80:
        blen = ((blen & 0x7F) << 8) | data[o]
        o += 1
    text = data[o : o + blen].decode("utf-8", "replace")
    return text, data[start : o + blen + 1], o + blen + 1


def encode_entry(text):
    raw = text.encode("utf-8")
    u16len = len(text.encode("utf-16-le")) // 2
    out = bytearray()
    for val in (u16len, len(raw)):
        if val > 0x7F:
            out += bytes([0x80 | (val >> 8), val & 0xFF])
        else:
            out.append(val)
    out += raw
    out.append(0)
    return bytes(out)


def main():
    src, dst = sys.argv[1], sys.argv[2]
    replacements = {}
    for pair in sys.argv[3:]:
        old, new = pair.split("=", 1)
        replacements[old] = new

    data = bytearray(open(src, "rb").read())
    t_type, t_hdr, t_size, t_count = struct.unpack_from("<HHII", data, 0)
    assert t_type == 0x0002, "not a ResTable"

    base = 12
    (p_type, p_hdr, p_size, s_count, st_count, flags, s_start, st_start) = (
        struct.unpack_from("<HHIIIIII", data, base)
    )
    assert p_type == 0x0001 and flags & UTF8_FLAG, "pool is not UTF-8"

    offs = list(struct.unpack_from(f"<{s_count}I", data, base + p_hdr))
    assert st_count == s_count or st_count >= 0

    old_strings = bytes(data[base + s_start : base + st_start])
    old_styles = bytes(data[base + st_start : base + p_size])

    # walk entries using consecutive offsets; last one ends at strings end
    bounds = offs + [len(old_strings)]
    entries = []
    texts = []
    for i in range(s_count):
        text, raw, _ = read_entry(bytes(data), base + s_start + bounds[i])
        texts.append(text)
        entries.append(raw)

    hits = {}
    for i, text in enumerate(texts):
        if text in replacements:
            hits.setdefault(text, []).append(i)

    missing = [k for k in replacements if k not in hits]
    if missing:
        raise SystemExit(f"string(s) not found: {missing}")

    for text, new in replacements.items():
        for i in hits[text]:
            entries[i] = encode_entry(new)

    blob = bytearray()
    new_offs = []
    for e in entries:
        new_offs.append(len(blob))
        blob += e
    while len(blob) % 4:
        blob.append(0)

    new_s_start = s_start
    new_st_start = s_start + len(blob)
    new_pool_size = new_st_start + len(old_styles)

    head = bytearray()
    head += struct.pack(
        "<HHIIIIII",
        p_type,
        p_hdr,
        new_pool_size,
        s_count,
        st_count,
        flags,
        new_s_start,
        new_st_start,
    )
    head += struct.pack(f"<{s_count}I", *new_offs)
    head += bytes(data[base + p_hdr + 4 * s_count : base + new_s_start])

    new_pool = head + blob + old_styles
    assert len(new_pool) == new_pool_size, (len(new_pool), new_pool_size)

    tail = bytes(data[base + p_size :])
    out = bytearray()
    out += struct.pack("<HHII", t_type, t_hdr, 12 + len(new_pool) + len(tail), t_count)
    out += new_pool
    out += tail

    open(dst, "wb").write(out)
    print("ok:", dst)
    print("  substituicoes:", replacements)
    print("  tamanho:", t_size, "->", len(out), f"(delta {len(out) - t_size:+d})")
    print("  pool:", p_size, "->", new_pool_size)


if __name__ == "__main__":
    main()
