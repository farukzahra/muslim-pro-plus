import re, sys, json

p = r"C:\repo\muslim-pro-plus\evidence\ui-01.xml"
t = open(p, encoding="utf-8", errors="replace").read()
texts = [m for m in re.findall(r'text="([^"]*)"', t) if m.strip()]
pkgs = sorted(set(re.findall(r'package="([^"]+)"', t)))
print("textos:", json.dumps(texts[:30], ensure_ascii=False))
print("pacotes:", pkgs)
print("nodes:", t.count("<node"))
