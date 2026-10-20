"""Compare pixel variance in the timeline premium slot vs a blank area."""

from PIL import Image

img = Image.open(r"C:\repo\muslim-pro-plus\evidence\screen-final.png").convert("RGB")
print("size:", img.size)


def stats(box):
    crop = img.crop(box)
    px = list(crop.getdata())
    colors = set(px)
    avg = tuple(sum(c[i] for c in px) // len(px) for i in range(3))
    return len(colors), avg, crop.size


# premium slot ~ x 827..953, y 138..264 (view 0,10-126,136 inside bar at y=128)
print("premium slot :", stats((830, 145, 950, 260)))
print("account slot :", stats((960, 145, 1075, 260)))
print("bar vazio    :", stats((600, 145, 700, 260)))
