"""Set attribute values on the root element of a compiled layout XML.

usage: patch_axml_set.py <apk> <entry> <out> <rid=value,rid=value,...>

Values are written as raw typedValue.data (decimal, 0x for hex). Setting a
dimension to 0 makes Android treat the attribute as missing, so use 1 (1px)
to collapse a view.
"""

import struct
import sys
import zipfile

APK, ENTRY, OUT = sys.argv[1], sys.argv[2], sys.argv[3]
PAIRS = {}
for item in sys.argv[4].split(","):
    rid, val = item.split("=")
    PAIRS[int(rid.rstrip("h"), 16)] = int(val, 0)

RES_XML_RESOURCE_MAP = 0x0180
RES_XML_START_ELEMENT = 0x0102

data = bytearray(zipfile.ZipFile(APK).read(ENTRY))
resmap = []
o = 8
first = None
while o < len(data):
    t, hdr, size = struct.unpack_from("<HHI", data, o)
    if size == 0:
        break
    if t == RES_XML_RESOURCE_MAP:
        n = (size - hdr) // 4
        resmap = list(struct.unpack_from(f"<{n}I", data, o + hdr))
    elif t == RES_XML_START_ELEMENT and first is None:
        attr_start, _, attr_count = struct.unpack_from("<HHH", data, o + hdr + 8)
        base = o + hdr + attr_start
        first = {}
        for i in range(attr_count):
            a = base + i * 20
            name = struct.unpack_from("<I", data, a + 4)[0]
            rid = resmap[name] if name < len(resmap) else 0
            first[rid] = a + 16
    o += size

if not first:
    raise SystemExit("sem elemento root")

applied = 0
for rid, val in PAIRS.items():
    if rid in first:
        struct.pack_into("<I", data, first[rid], val)
        applied += 1

open(OUT, "wb").write(data)
print(f"{ENTRY}: {applied}/{len(PAIRS)} valores aplicados no root")
