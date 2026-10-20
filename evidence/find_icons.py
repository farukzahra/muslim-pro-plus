"""Locate the toolbar icons by scanning the screenshot for non-uniform areas."""

from PIL import Image

img = Image.open(r"C:\repo\muslim-pro-plus\evidence\screen-account.png").convert("RGB")
w, h = img.size
print("size:", w, h)


def colors(box):
    crop = img.crop(box)
    return len(set(crop.getdata()))


# varre a faixa vertical onde a barra superior pode estar
for y0 in range(100, 900, 40):
    row = [colors((x, y0, x + 120, y0 + 110)) for x in (830, 960)]
    if max(row) > 60:
        print(f"y={y0:4d}  x830={row[0]:3d}  x960={row[1]:3d}")
