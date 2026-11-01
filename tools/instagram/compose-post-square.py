from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W = H = 1080
OUT = Path(r"C:\repo\muslim-pro-plus\docs\instagram")

BG_TOP = (7, 24, 32)
BG_BOTTOM = (10, 31, 41)
EMERALD = (47, 185, 138)
GOLD = (217, 178, 95)
INK = (233, 238, 244)
MUTED = (159, 176, 194)
CARD = (14, 27, 38)
LINE = (34, 52, 68)

GEORGIA_B = r"C:\Windows\Fonts\georgiab.ttf"
SEGOE = r"C:\Windows\Fonts\segoeui.ttf"
SEGOE_B = r"C:\Windows\Fonts\segoeuib.ttf"
SEGOE_SB = r"C:\Windows\Fonts\seguisb.ttf"
GEORGIA = r"C:\Windows\Fonts\georgia.ttf"


def F(path, size):
    return ImageFont.truetype(path, size)


def center(draw, y, text, font, fill):
    w = draw.textlength(text, font=font)
    draw.text(((W - w) / 2, y), text, font=font, fill=fill)


img = Image.new("RGB", (W, H), BG_TOP)
px = img.load()
for y in range(H):
    t = y / (H - 1)
    row = (
        int(BG_TOP[0] + (BG_BOTTOM[0] - BG_TOP[0]) * t),
        int(BG_TOP[1] + (BG_BOTTOM[1] - BG_TOP[1]) * t),
        int(BG_TOP[2] + (BG_BOTTOM[2] - BG_TOP[2]) * t),
    )
    for x in range(W):
        px[x, y] = row

glow = Image.new("RGB", (W, H), (0, 0, 0))
g = ImageDraw.Draw(glow)
for r in range(560, 0, -6):
    a = int(42 * (1 - r / 560))
    g.ellipse((W / 2 - r, -260 - r, W / 2 + r, -260 + r), fill=(a // 3, a, a * 2 // 3))
glow = glow.filter(ImageFilter.GaussianBlur(80))
img = Image.blend(img, glow, 0.6)
draw = ImageDraw.Draw(img)

icon = 96
mask = Image.new("L", (icon, icon), 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, icon - 1, icon - 1), radius=22, fill=255)
ic = (
    Image.open(r"C:\repo\muslim-pro-plus\docs\store\icon-512.png")
    .convert("RGBA")
    .resize((icon, icon), Image.LANCZOS)
)
img.paste(ic, (int((W - icon) / 2), 64), mask)

center(draw, 180, "Muslim Plus", F(GEORGIA_B, 42), INK)
center(draw, 236, "Orações · Qibla · Alcorão", F(SEGOE, 21), MUTED)

center(draw, 318, "Suas orações.", F(GEORGIA_B, 74), INK)
center(draw, 402, "Seu Alcorão.", F(GEORGIA_B, 74), INK)
center(draw, 486, "Offline.", F(GEORGIA_B, 74), EMERALD)

center(draw, 600, "Sem conta. Sem anúncios. Sem rastreamento.", F(SEGOE, 23), MUTED)

cards = [
    ("5 orações", "com sequência"),
    ("Adhan", "por oração"),
    ("Qibla", "calibração"),
]
card_w, card_h, gap = 280, 104, 20
total = len(cards) * card_w + (len(cards) - 1) * gap
x = (W - total) / 2
y = 664
for title, sub in cards:
    draw.rounded_rectangle(
        (x, y, x + card_w, y + card_h), radius=20, fill=CARD, outline=LINE, width=1
    )
    draw.ellipse((x + 22, y + 24, x + 44, y + 46), fill=(23, 62, 53))
    draw.line((x + 27, y + 35, x + 32, y + 41), fill=EMERALD, width=3)
    draw.line((x + 32, y + 41, x + 40, y + 29), fill=EMERALD, width=3)
    draw.text((x + 58, y + 18), title, font=F(SEGOE_SB, 23), fill=INK)
    draw.text((x + 58, y + 50), sub, font=F(SEGOE, 17), fill=MUTED)
    x += card_w + gap

cta_w, cta_h = 404, 78
cta_x = (W - cta_w) / 2
cta_y = 824
draw.rounded_rectangle(
    (cta_x, cta_y, cta_x + cta_w, cta_y + cta_h), radius=39, fill=EMERALD
)
draw.polygon(
    [
        (cta_x + 42, cta_y + 24),
        (cta_x + 42, cta_y + 54),
        (cta_x + 66, cta_y + 39),
    ],
    fill=(6, 35, 27),
)
draw.text(
    (cta_x + 86, cta_y + 22),
    "Grátis no Google Play",
    font=F(SEGOE_SB, 24),
    fill=(6, 35, 27),
)

draw.line((300, 948, 780, 948), fill=(42, 71, 80), width=1)
center(draw, 968, "Muslim Plus · 1.2.0", F(GEORGIA, 22), MUTED)
center(draw, 1002, "pt · en · ar", F(SEGOE, 16), GOLD)

img.save(OUT / "post-2.png")
img.save(OUT / "post-2.jpg", quality=94, optimize=True)
print("ok", img.size, (OUT / "post-2.jpg").stat().st_size)
