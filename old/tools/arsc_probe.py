import struct, sys

path = (
    sys.argv[1]
    if len(sys.argv) > 1
    else r"C:\repo\muslim-pro-plus\apktool_out\resources.arsc"
)
data = open(path, "rb").read()

RES_TABLE = 0x0002
RES_STRING_POOL = 0x0001
UTF8_FLAG = 0x100

t_type, t_hdr, t_size, t_count = struct.unpack_from("<HHII", data, 0)
print(
    f"table type={t_type:#x} header={t_hdr} size={t_size} file={len(data)} packages={t_count}"
)

p_type, p_hdr, p_size, s_count, st_count, flags, s_start, st_start = struct.unpack_from(
    "<HHIIIIII", data, 12
)
print(
    f"pool type={p_type:#x} header={p_hdr} size={p_size} strings={s_count} styles={st_count} flags={flags:#x} strings_start={s_start} styles_start={st_start}"
)
utf8 = bool(flags & UTF8_FLAG)
print("utf8:", utf8)

base = 12
offs = struct.unpack_from(f"<{s_count}I", data, base + p_hdr)


def read_str(i):
    o = base + s_start + offs[i]
    if utf8:
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
        return data[o : o + blen].decode("utf-8", "replace")
    else:
        n = struct.unpack_from("<H", data, o)[0]
        o += 2
        return data[o : o + n * 2].decode("utf-16-le", "replace")


hits = []
for i in range(s_count):
    s = read_str(i)
    if s in ("Pular", "Skip", "Skip now", "Continuar sem conta"):
        hits.append((i, s))

print("hits:", hits)
for i, s in hits:
    o = base + s_start + offs[i]
    print(f"  idx={i} off={o} raw={data[o : o + 12].hex()}")
