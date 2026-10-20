import re, sys

p = sys.argv[1] if len(sys.argv) > 1 else r"C:\repo\muslim-pro-plus\evidence\ui-01.xml"
t = open(p, encoding="utf-8", errors="replace").read()

for m in re.finditer(r"<node[^>]*>", t):
    s = m.group(0)

    def g(name):
        mm = re.search(name + r'="([^"]*)"', s)
        return mm.group(1) if mm else ""

    print(
        repr(g("text"))[:70],
        "|",
        g("class").split(".")[-1],
        "| bounds",
        g("bounds"),
        "| click",
        g("clickable"),
        "| vis",
        g("visible-to-user") or "-",
        "|",
        g("resource-id"),
    )
