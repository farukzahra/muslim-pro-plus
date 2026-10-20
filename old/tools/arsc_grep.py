"""List every string in resources.arsc matching a pattern (case-insensitive)."""

import re
import struct
import sys

path = sys.argv[1]
pattern = re.compile(sys.argv[2], re.I)

data = open(path, "rb").read()
t_type, t_hdr, t_size, t_count = struct.unpack_from("<HHII", data, 0)
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


hits = []
for i in range(s_count):
    s = read_str(i)
    if pattern.search(s):
        hits.append((i, s))

print(f"total strings={s_count} correspondencias={len(hits)}")
for i, s in hits[:60]:
    print(f"  [{i}] {s[:120]}")
if len(hits) > 60:
    print(f"  ... +{len(hits) - 60}")
