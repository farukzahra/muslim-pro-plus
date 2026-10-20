"""Minimal Android binary XML dumper."""

import struct
import sys
import zipfile

APK = sys.argv[1]
ENTRY = sys.argv[2]

raw = zipfile.ZipFile(APK).read(ENTRY)

RES_XML_TYPE = 0x0003
RES_STRING_POOL_TYPE = 0x0001
RES_XML_RESOURCE_MAP_TYPE = 0x0180
RES_XML_START_ELEMENT_TYPE = 0x0102
RES_XML_END_ELEMENT_TYPE = 0x0103
RES_XML_CDATA_TYPE = 0x0104
UTF8_FLAG = 0x100

strings = []
resmap = []


def parse_len8(data, o):
    n = data[o]
    o += 1
    if n & 0x80:
        n = ((n & 0x7F) << 8) | data[o]
        o += 1
    return n, o


def parse_pool(data, off, hdr, size):
    st_count, style_count, flags, strings_start, styles_start = struct.unpack_from(
        "<IIIII", data, off + 8
    )
    utf8 = bool(flags & UTF8_FLAG)
    offs = struct.unpack_from(f"<{st_count}I", data, off + hdr)
    out = []
    for i in range(st_count):
        o = off + strings_start + offs[i]
        if utf8:
            _, o = parse_len8(data, o)
            blen, o = parse_len8(data, o)
            out.append(data[o : o + blen].decode("utf-8", "replace"))
        else:
            n = struct.unpack_from("<H", data, o)[0]
            o += 2
            out.append(data[o : o + n * 2].decode("utf-16-le", "replace"))
    return out


o = 8
end = len(raw)
while o < end:
    t, hdr, size = struct.unpack_from("<HHI", raw, o)
    if t == RES_STRING_POOL_TYPE:
        strings = parse_pool(raw, o, hdr, size)
    elif t == RES_XML_RESOURCE_MAP_TYPE:
        count = (size - hdr) // 4
        resmap = list(struct.unpack_from(f"<{count}I", raw, o + hdr))
    elif t == RES_XML_START_ELEMENT_TYPE:
        ns, name = struct.unpack_from("<II", raw, o + hdr)
        attr_start, attr_size, attr_count = struct.unpack_from("<HHH", raw, o + hdr + 8)
        print(f"<{strings[name]}>  (attrs={attr_count})")
        base = o + hdr + attr_start
        for i in range(attr_count):
            a = base + i * 20
            a_ns, a_name, a_raw, v_size, v_res0, v_type, v_data = struct.unpack_from(
                "<IIIHBBI", raw, a
            )
            attr_name = strings[a_name]
            rid = resmap[a_name] if a_name < len(resmap) else 0
            raw_s = strings[a_raw] if 0 < a_raw < len(strings) else f"#{a_raw}"
            print(
                f"    {attr_name} (0x{rid:08x}) type=0x{v_type:02x} data=0x{v_data:08x} raw={raw_s!r}"
            )
    elif t == RES_XML_END_ELEMENT_TYPE:
        ns, name = struct.unpack_from("<II", raw, o + hdr)
        print(f"</{strings[name]}>")
    o += size
    if size == 0:
        break
