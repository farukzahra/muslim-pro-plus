"""Find resource files that reference a given resource id (little endian)."""

import struct
import sys
import zipfile

APK = sys.argv[1]
RID = int(sys.argv[2], 16)
needle = struct.pack("<I", RID)

z = zipfile.ZipFile(APK)
for info in z.infolist():
    if info.filename.endswith((".dex", ".arsc")):
        continue
    data = z.read(info.filename)
    if needle in data:
        print(f"{info.filename}  ({info.file_size} bytes, {data.count(needle)} refs)")
