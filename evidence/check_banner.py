from PIL import Image

img = Image.open(r"C:\repo\muslim-pro-plus\evidence\screen-banner2.png").convert("RGB")
band = img.crop((0, 2010, 1080, 2176))
px = list(band.getdata())
greens = sum(1 for r, g, b in px if g > 90 and g > r + 40 and g > b + 20)
print("pixels verdes na faixa do banner:", greens, "de", len(px))
print("cores distintas:", len(set(px)))
print("media:", tuple(sum(c[i] for c in px) // len(px) for i in range(3)))
