import zipfile, collections, os

apk = r"C:\repo\muslim-pro-plus\apk\muslimpro-12.6.10.apk"
z = zipfile.ZipFile(apk)
infos = z.infolist()
print("entradas:", len(infos))
by_top = collections.Counter(i.filename.split("/")[0] for i in infos)
for k, v in by_top.most_common(15):
    print(f"  {k}/ => {v}")
for n in ["resources.arsc", "AndroidManifest.xml", "classes.dex"]:
    i = z.getinfo(n)
    print(f"{n}: comp={i.compress_type} raw={i.file_size} comp_size={i.compress_size}")
# sample res layout paths
res = [i.filename for i in infos if i.filename.startswith("res/")][:10]
print("exemplos res:", res)
print("dex:", sorted(i.filename for i in infos if i.filename.endswith(".dex")))
