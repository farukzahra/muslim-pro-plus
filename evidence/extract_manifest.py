import json

src = r"C:\repo\muslim-pro-plus\evidence\01-inspect-package.json"
out = r"C:\repo\muslim-pro-plus\evidence\AndroidManifest.decoded.xml"
d = json.load(open(src, encoding="utf-8"))
m = d["normalized_result"]["manifest"]["text"]
open(out, "w", encoding="utf-8").write(m)
print("manifest bytes:", len(m))
print("status:", d["normalized_result"]["manifest"]["status"])
