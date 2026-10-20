"""Replace one string inside a binary AndroidManifest.xml in place.

The replacement must have the same length as the original so the string pool
offsets stay valid. Works for both UTF-8 and UTF-16LE pools.
"""

import sys

src, dst = sys.argv[1], sys.argv[2]
old, new = sys.argv[3], sys.argv[4]

if len(old) != len(new):
    raise SystemExit(f"tamanho diferente: {len(old)} != {len(new)}")

data = bytearray(open(src, "rb").read())

patterns = [
    (old.encode("utf-8"), new.encode("utf-8"), "utf-8"),
    (old.encode("utf-16-le"), new.encode("utf-16-le"), "utf-16le"),
]

done = False
for pat, rep, label in patterns:
    idx = bytes(data).find(pat)
    if idx == -1:
        continue
    count = bytes(data).count(pat)
    data[idx : idx + len(pat)] = rep
    print(f"trocado ({label}) em 0x{idx:x}, ocorrencias={count}")
    done = True
    if count != 1:
        print("  aviso: mais de uma ocorrencia, so a primeira foi trocada")

if not done:
    raise SystemExit("string nao encontrada no manifest")

open(dst, "wb").write(data)
print("escrito:", dst)
