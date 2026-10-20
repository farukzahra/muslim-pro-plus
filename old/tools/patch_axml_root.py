"""Zero chosen attributes on the root element of a compiled layout XML.

Used to collapse a view without touching code: setting layout_width/height to
0dp makes the view invisible and takes no space.
"""

import struct
import sys
import zipfile

APK = sys.argv[1]
ENTRY = sys.argv[2]
OUT = sys.argv[3]
RIDS = {int(x, 16) for x in sys.argv[4].split(",")}

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
        attrs = {}
        for i in range(attr_count):
            a = base + i * 20
            name = struct.unpack_from("<I", data, a + 4)[0]
            rid = resmap[name] if name < len(resmap) else 0
            attrs[rid] = a + 16
        first = attrs
    o += size

if not first:
    raise SystemExit("nenhum elemento encontrado")

zeroed = 0
for rid in RIDS:
    if rid in first:
        struct.pack_into("<I", data, first[rid], 0)
        zeroed += 1

open(OUT, "wb").write(data)
print(f"{ENTRY}: {zeroed} atributos zerados no root")
