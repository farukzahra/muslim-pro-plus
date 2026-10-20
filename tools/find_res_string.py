"""Find which resource files (binary XML) mention a given string."""

import sys
import zipfile

APK = sys.argv[1]
NEEDLE = sys.argv[2]
NEEDLES = [NEEDLE.encode("utf-8"), NEEDLE.encode("utf-16-le")]

z = zipfile.ZipFile(APK)
for info in z.infolist():
    if info.filename.endswith((".dex", ".arsc")):
        continue
    data = z.read(info.filename)
    for n in NEEDLES:
        if n in data:
            print(f"{info.filename}  ({info.file_size} bytes)")
            break
