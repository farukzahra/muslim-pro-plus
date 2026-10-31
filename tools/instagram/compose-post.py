from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1080, 1350
OUT = Path(r"C:\repo\muslim-pro-plus\docs\instagram")
OUT.mkdir(parents=True, exist_ok=True)

BG_TOP = (7, 24, 32)
BG_BOTTOM = (10, 31, 41)
EMERALD = (47, 185, 138)
GOLD = (217, 178, 95)
INK = (233, 238, 244)
MUTED = (159, 176, 194)

GEORGIA_B = r"C:\Windows\Fonts\georgiab.ttf"
GEORGIA = r"C:\Windows\Fonts\georgia.ttf"
SEGOE = r"C:\Windows\Fonts\segoeui.ttf"
SEGOE_B = r"C:\Windows\Fonts\segoeuib.ttf"
SEGOE_SB = r"C:\Windows\Fonts\seguisb.ttf"


def F(path, size):
    return ImageFont.truetype(path, size)


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
for r in range(640, 0, -6):
    a = int(40 * (1 - r / 640))
    g.ellipse(
        (W - 240 - r, -240 - r, W - 240 + r, -240 + r), fill=(a // 3, a, a * 2 // 3)
    )
glow = glow.filter(ImageFilter.GaussianBlur(80))
img = Image.blend(img, glow, 0.55)
draw = ImageDraw.Draw(img)

ring = Image.new("RGBA", (W, H), (0, 0, 0, 0))
rd = ImageDraw.Draw(ring)
rd.arc((-240, H - 280, 320, H + 280), start=200, end=340, fill=GOLD + (32,), width=3)
img = Image.alpha_composite(img.convert("RGBA"), ring).convert("RGB")
draw = ImageDraw.Draw(img)

mask = Image.new("L", (104, 104), 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, 103, 103), radius=24, fill=255)
icon = (
    Image.open(r"C:\repo\muslim-pro-plus\docs\store\icon-512.png")
    .convert("RGBA")
    .resize((104, 104), Image.LANCZOS)
)
img.paste(icon, (72, 72), mask)
draw.text((200, 78), "Muslim Plus", font=F(GEORGIA_B, 44), fill=INK)
draw.text((202, 136), "Orações · Qibla · Alcorão", font=F(SEGOE, 23), fill=MUTED)

size = 78
while size > 58:
    f = F(GEORGIA_B, size)
    if draw.textlength("Seu Alcorão. Offline.", font=f) <= 930:
        break
    size -= 2
headline = F(GEORGIA_B, size)
draw.text((72, 252), "Suas orações.", font=headline, fill=INK)
y2 = 252 + int(size * 1.12)
part1 = "Seu Alcorão. "
draw.text((72, y2), part1, font=headline, fill=INK)
x2 = 72 + draw.textlength(part1, font=headline)
draw.text((x2, y2), "Offline.", font=headline, fill=EMERALD)
print(
    "headline size",
    size,
    "line2 width",
    draw.textlength("Seu Alcorão. Offline.", font=headline),
)

sub_y = y2 + size + 26
draw.text((74, sub_y), "Sem conta. Sem anúncios.", font=F(SEGOE, 28), fill=MUTED)
draw.text((74, sub_y + 38), "Sem rastreamento.", font=F(SEGOE, 28), fill=MUTED)

checks = [
    "Rastreador das 5 orações",
    "Lembrete antes do adhan",
    "Alcorão completo, offline",
    "Qibla com calibração",
]
y = sub_y + 118
for item in checks:
    draw.ellipse((76, y + 6, 98, y + 28), fill=(23, 62, 53))
    draw.line((81, y + 17, 86, y + 23), fill=EMERALD, width=3)
    draw.line((86, y + 23, 94, y + 12), fill=EMERALD, width=3)
    draw.text((114, y), item, font=F(SEGOE, 27), fill=(213, 226, 232))
    y += 52

shot = (
    Image.open(r"C:\repo\muslim-pro-plus\docs\instagram\source-today.png")
    .convert("RGB")
    .crop((0, 60, 1080, 1800))
)
phone_w = 456
shot = shot.resize((phone_w, int(shot.height * phone_w / shot.width)), Image.LANCZOS)
phone_h = min(shot.height, 708)
shot = shot.crop((0, 0, phone_w, phone_h))
frame_x, frame_y = 558, 556
pad = 10
frame = Image.new("RGBA", (phone_w + pad * 2, phone_h + pad * 2), (0, 0, 0, 0))
fd = ImageDraw.Draw(frame)
fd.rounded_rectangle(
    (0, 0, frame.width - 1, frame.height - 1),
    radius=46,
    fill=(13, 30, 39, 255),
    outline=(52, 83, 96, 255),
    width=2,
)
mask2 = Image.new("L", (phone_w, phone_h), 0)
ImageDraw.Draw(mask2).rounded_rectangle(
    (0, 0, phone_w - 1, phone_h - 1), radius=38, fill=255
)
frame.paste(shot, (pad, pad), mask2)

shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shadow)
sd.rounded_rectangle(
    (
        frame_x - 6,
        frame_y + 18,
        frame_x + frame.width + 16,
        frame_y + frame.height + 36,
    ),
    radius=52,
    fill=(0, 0, 0, 160),
)
shadow = shadow.filter(ImageFilter.GaussianBlur(28))
img = Image.alpha_composite(img.convert("RGBA"), shadow).convert("RGB")
img.paste(frame, (frame_x, frame_y), frame)

fade_h = 150
fade = Image.new("RGBA", (frame.width, fade_h), (0, 0, 0, 0))
fp = fade.load()
for yy in range(fade_h):
    alpha = int(255 * (yy / (fade_h - 1)) ** 1.6)
    for xx in range(fade.width):
        fp[xx, yy] = BG_BOTTOM + (alpha,)
img.paste(fade, (frame_x, frame_y + frame.height - fade_h), fade)
draw = ImageDraw.Draw(img)

draw.text((74, 850), "NOVIDADE · VERSÃO 1.2.0", font=F(SEGOE_B, 18), fill=GOLD)
cta_x, cta_y, cta_w, cta_h = 72, 898, 388, 84
draw.rounded_rectangle(
    (cta_x, cta_y, cta_x + cta_w, cta_y + cta_h), radius=42, fill=EMERALD
)
draw.polygon(
    [(cta_x + 38, cta_y + 26), (cta_x + 38, cta_y + 58), (cta_x + 62, cta_y + 42)],
    fill=(6, 35, 27),
)
draw.text(
    (cta_x + 82, cta_y + 24),
    "Grátis no Google Play",
    font=F(SEGOE_SB, 26),
    fill=(6, 35, 27),
)

draw.line((74, 1028, 460, 1028), fill=(42, 71, 80), width=1)
draw.text(
    (74, 1054), "Feito para rezar, ler e se orientar.", font=F(GEORGIA, 23), fill=MUTED
)
draw.text((74, 1094), "pt · en · ar", font=F(SEGOE, 18), fill=GOLD)

img.save(OUT / "post-1.png")
img.convert("RGB").save(OUT / "post-1.jpg", quality=94, optimize=True)
print("ok", img.size, (OUT / "post-1.jpg").stat().st_size)
