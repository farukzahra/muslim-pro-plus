"""Repack an APK replacing selected entries, keeping order and compression.

Only the entries listed on the command line are swapped; everything else is
copied byte for byte. Old signature files are dropped so the APK can be signed
again.
"""

import sys
import zipfile

apk_in, apk_out = sys.argv[1], sys.argv[2]
replacements = {}
for pair in sys.argv[3:]:
    name, path = pair.split("=", 1)
    replacements[name] = path

z = zipfile.ZipFile(apk_in)
out = zipfile.ZipFile(apk_out, "w")

for info in z.infolist():
    if info.filename.upper().startswith("META-INF/") and info.filename.upper().endswith(
        (".SF", ".RSA", ".DSA", ".EC")
    ):
        continue

    if info.filename in replacements:
        data = open(replacements[info.filename], "rb").read()
        comp = (
            zipfile.ZIP_STORED
            if info.filename == "resources.arsc"
            else info.compress_type
        )
    else:
        data = z.read(info.filename)
        comp = info.compress_type

    zi = zipfile.ZipInfo(info.filename, date_time=info.date_time)
    zi.compress_type = comp
    zi.external_attr = info.external_attr
    zi.internal_attr = info.internal_attr
    zi.create_system = info.create_system
    out.writestr(zi, data)

out.close()
print("escrito:", apk_out)
print("trocados:", list(replacements))
