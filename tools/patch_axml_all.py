"""Set attribute values on every element of a compiled layout XML."""

import struct
import sys
import zipfile

APK, ENTRY, OUT = sys.argv[1], sys.argv[2], sys.argv[3]
PAIRS = {}
for item in sys.argv[4].split(","):
    rid, val = item.split("=")
    PAIRS[int(rid, 16)] = int(val, 0)

RES_XML_RESOURCE_MAP = 0x0180
RES_XML_START_ELEMENT = 0x0102

data = bytearray(zipfile.ZipFile(APK).read(ENTRY))
resmap = []
o = 8
elements = applied = 0
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
            if rid in PAIRS:
                vtype = data[a + 15]
                val = PAIRS[rid]
                # referencias (@dimen/@drawable) quebrariam se virassem valor cru
                if val > 0 and vtype in (0x01, 0x02):
                    continue
                struct.pack_into("<I", data, a + 16, val)
                applied += 1
    o += size

open(OUT, "wb").write(data)
print(f"{ENTRY}: {elements} elementos, {applied} atributos ajustados")
