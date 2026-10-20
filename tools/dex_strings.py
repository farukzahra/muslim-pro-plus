"""Scan DEX string pools for a pattern and report hits."""

import re
import struct
import sys

PATH = sys.argv[1]
PATTERN = re.compile(sys.argv[2], re.I)

d = open(PATH, "rb").read()
(sz, hsz, endian, lsz, loff, mapoff) = struct.unpack_from("<IIIIII", d, 32)
(sids, soff) = struct.unpack_from("<II", d, 56)

hits = []
for i in range(sids):
    off = struct.unpack_from("<I", d, soff + 4 * i)[0]
    o = off
    while d[o] & 0x80:
        o += 1
    o += 1
    end = d.index(b"\x00", o)
    s = d[o:end].decode("utf-8", "replace")
    if PATTERN.search(s):
        hits.append((i, off, s))

print(f"{PATH}: strings={sids} hits={len(hits)}")
for i, off, s in hits[:40]:
    print(f"  [{i}] @0x{off:x} len={len(s)} {s[:100]}")
