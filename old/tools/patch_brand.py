"""Rebrand visible strings in resources.arsc: Muslim Pro -> Muslim Plus.

URLs, domains and resource paths are left untouched so API calls, downloads
and deep links keep working.
"""

import re
import struct
import sys

SRC = (
    sys.argv[1]
    if len(sys.argv) > 1
    else r"C:\repo\muslim-pro-plus\build\resources.arsc"
)
DST = (
    sys.argv[2]
    if len(sys.argv) > 2
    else r"C:\repo\muslim-pro-plus\build\resources.brand.arsc"
)

BRAND = re.compile(r"muslim\s*pro", re.I)
SKIP = re.compile(r"(?i)^(https?://|www\.|//)|\.muslimpro\.com|^res/|^/")

UTF8_FLAG = 0x100


def is_url_like(s):
    if SKIP.search(s):
        return True
    # share/deep-link payloads and api paths
    return bool(re.search(r"(?i)muslimpro\.com|scheme=.?muslimpro", s))


def repl(match):
    src = match.group(0)
    spaced = bool(re.search(r"\s", src))
    if src.isupper():
        return "MUSLIM PLUS" if spaced else "MUSLIMPLUS"
    if src.islower():
        return "muslim plus" if spaced else "muslimplus"
    return "Muslim Plus" if spaced else "MuslimPlus"


# nomes localizados que nao usam alfabeto latino
LITERALS = {
    "مسلم برو": "مسلم بلس",
    "মুসলিম প্রো": "মুসলিম প্লাস",
}


def read_entry(data, start):
    o = start
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
    text = data[o : o + blen].decode("utf-8", "replace")
    return text, data[start : o + blen + 1]


def encode_entry(text):
    raw = text.encode("utf-8")
    u16 = len(text.encode("utf-16-le")) // 2
    out = bytearray()
    for val in (u16, len(raw)):
        if val > 0x7F:
            out += bytes([0x80 | (val >> 8), val & 0xFF])
        else:
            out.append(val)
    out += raw
    out.append(0)
    return bytes(out)


data = bytearray(open(SRC, "rb").read())
t_type, t_hdr, t_size, t_count = struct.unpack_from("<HHII", data, 0)
base = 12
(p_type, p_hdr, p_size, s_count, st_count, flags, s_start, st_start) = (
    struct.unpack_from("<HHIIIIII", data, base)
)
assert flags & UTF8_FLAG, "pool nao e UTF-8"

offs = list(struct.unpack_from(f"<{s_count}I", data, base + p_hdr))
bounds = offs + [st_start - s_start]

entries = []
texts = []
for i in range(s_count):
    text, raw = read_entry(data, base + s_start + bounds[i])
    texts.append(text)
    entries.append(raw)

changed = 0
skipped = 0
samples = []
for i, text in enumerate(texts):
    if not BRAND.search(text) and not any(k in text for k in LITERALS):
        continue
    if is_url_like(text):
        skipped += 1
        continue
    new = text
    for old_lit, new_lit in LITERALS.items():
        new = new.replace(old_lit, new_lit)
    new = BRAND.sub(repl, new)
    if new != text:
        entries[i] = encode_entry(new)
        changed += 1
        if len(samples) < 8:
            samples.append((text[:70], new[:70]))

blob = bytearray()
new_offs = []
for e in entries:
    new_offs.append(len(blob))
    blob += e
while len(blob) % 4:
    blob.append(0)

old_styles = bytes(data[base + st_start : base + p_size])
new_st_start = s_start + len(blob)
new_pool_size = new_st_start + len(old_styles)

head = bytearray()
head += struct.pack(
    "<HHIIIIII",
    p_type,
    p_hdr,
    new_pool_size,
    s_count,
    st_count,
    flags,
    s_start,
    new_st_start,
)
head += struct.pack(f"<{s_count}I", *new_offs)
head += bytes(data[base + p_hdr + 4 * s_count : base + s_start])

payload = head + blob + old_styles
assert len(payload) == new_pool_size

tail = bytes(data[base + p_size :])
out = bytearray()
out += struct.pack("<HHII", t_type, t_hdr, 12 + len(payload) + len(tail), t_count)
out += payload
out += tail
open(DST, "wb").write(out)

print(f"strings={s_count} trocadas={changed} ignoradas_url={skipped}")
print(f"tamanho {t_size} -> {len(out)}")
for a, b in samples:
    print(f"  {a!r} -> {b!r}")
