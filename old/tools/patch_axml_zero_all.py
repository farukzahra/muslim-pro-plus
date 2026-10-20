"""Zero chosen attributes on every element of a compiled layout XML.

Used to completely collapse an included view: 0dp sizes, no text, no image,
no background.
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
elements = 0
zeroed = 0
while o < len(data):
    t, hdr, size = struct.unpack_from("<HHI", data, o)
    if size == 0:
        break
    if t == RES_XML_RESOURCE_MAP:
        n = (size - hdr) // 4
        resmap = list(struct.unpack_from(f"<{n}I", data, o + hdr))
    elif t == RES_XML_START_ELEMENT:
        elements += 1
        attr_start, _, attr_count = struct.unpack_from("<HHH", data, o + hdr + 8)
        base = o + hdr + attr_start
        for i in range(attr_count):
            a = base + i * 20
            name = struct.unpack_from("<I", data, a + 4)[0]
            rid = resmap[name] if name < len(resmap) else 0
            if rid in RIDS:
                struct.pack_into("<I", data, a + 16, 0)
                zeroed += 1
    o += size

open(OUT, "wb").write(data)
print(f"{ENTRY}: {elements} elementos, {zeroed} atributos zerados")
