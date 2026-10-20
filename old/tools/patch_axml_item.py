"""Blank one menu item inside a compiled Android menu XML.

Finds the <item> whose android:id matches TARGET_ID and zeroes its
android:id, android:icon and android:title values in place, so the item
renders empty and stops receiving clicks.
"""

import struct
import sys
import zipfile

APK = sys.argv[1]
ENTRY = sys.argv[2]
TARGET_IDS = {int(x, 16) for x in sys.argv[3].split(",")}
OUT = sys.argv[4]

RES_STRING_POOL = 0x0001
RES_XML_RESOURCE_MAP = 0x0180
RES_XML_START_ELEMENT = 0x0102

ATTR_ID = 0x010100D0
ATTR_ICON = 0x01010002
ATTR_TITLE = 0x010101E1

data = bytearray(zipfile.ZipFile(APK).read(ENTRY))

resmap = []
o = 8
elements = []
while o < len(data):
    t, hdr, size = struct.unpack_from("<HHI", data, o)
    if size == 0:
        break
    if t == RES_XML_RESOURCE_MAP:
        count = (size - hdr) // 4
        resmap = list(struct.unpack_from(f"<{count}I", data, o + hdr))
    elif t == RES_XML_START_ELEMENT:
        name = struct.unpack_from("<I", data, o + hdr + 4)[0]
        attr_start, attr_size, attr_count = struct.unpack_from(
            "<HHH", data, o + hdr + 8
        )
        base = o + hdr + attr_start
        attrs = []
        for i in range(attr_count):
            a = base + i * 20
            a_name = struct.unpack_from("<I", data, a + 4)[0]
            rid = resmap[a_name] if a_name < len(resmap) else 0
            attrs.append((rid, a + 16))  # offset of typedValue.data
        elements.append((name, attrs))
    o += size

patched = 0
for name, attrs in elements:
    ids = {rid: off for rid, off in attrs}
    if ids.get(ATTR_ID) is None:
        continue
    cur = struct.unpack_from("<I", data, ids[ATTR_ID])[0]
    if cur not in TARGET_IDS:
        continue
    for rid in (ATTR_ICON, ATTR_TITLE):
        if rid in ids:
            struct.pack_into("<I", data, ids[rid], 0)
            patched += 1

if not patched:
    raise SystemExit("item alvo nao encontrado")

open(OUT, "wb").write(data)
print(f"{ENTRY}: {patched} atributos zerados (id/icon/title)")
