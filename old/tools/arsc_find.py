import re
import struct
import sys

path = sys.argv[1]
data = open(path, "rb").read()
base = 12
(p_type, p_hdr, p_size, s_count, st_count, flags, s_start, st_start) = (
    struct.unpack_from("<HHIIIIII", data, base)
)
offs = struct.unpack_from(f"<{s_count}I", data, base + p_hdr)


def read_str(i):
    o = base + s_start + offs[i]
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


rx = re.compile(sys.argv[2], re.I)
limit = int(sys.argv[3]) if len(sys.argv) > 3 else 40
for i in range(s_count):
    s = read_str(i)
    if rx.search(s) and len(s) < 90:
        print(f"[{i}] {s!r}")
