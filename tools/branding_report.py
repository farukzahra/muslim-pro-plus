"""Categorise branding strings in resources.arsc and write a report."""

import json
import re
import struct

SRC = r"C:\repo\muslim-pro-plus\build\resources.arsc"
OUT = r"C:\repo\muslim-pro-plus\evidence\branding-report.json"

data = open(SRC, "rb").read()
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


brand = re.compile(r"muslim\s*pro|muslimpro", re.I)
prem = re.compile(r"premium|subscription|subscribe|upgrade", re.I)

report = {"brand": [], "premium": [], "brand_count": 0, "premium_count": 0}
for i in range(s_count):
    s = read_str(i)
    if brand.search(s):
        report["brand_count"] += 1
        report["brand"].append({"idx": i, "text": s})
    if prem.search(s):
        report["premium_count"] += 1
        report["premium"].append({"idx": i, "text": s})

json.dump(report, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

urls = [b for b in report["brand"] if b["text"].startswith(("http", "www", "//"))]
domains = sorted({re.sub(r"^https?://", "", u["text"]).split("/")[0] for u in urls})
print(
    "brand total:",
    report["brand_count"],
    "| urls:",
    len(urls),
    "| texto:",
    report["brand_count"] - len(urls),
)
print("premium total:", report["premium_count"])
print("dominios:", ", ".join(domains[:20]))
print("--- amostra brand (nao url) ---")
shown = 0
for b in report["brand"]:
    if not b["text"].startswith(("http", "www", "//")):
        print(f"  [{b['idx']}] {b['text'][:100]}")
        shown += 1
        if shown >= 25:
            break
print("--- amostra premium ---")
for b in report["premium"][:20]:
    print(f"  [{b['idx']}] {b['text'][:100]}")
