from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

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
GEORGIA = r"C:\Windows\Fonts\georgia.ttf"
SEGOE = r"C:\Windows\Fonts\segoeui.ttf"
SEGOE_B = r"C:\Windows\Fonts\segoeuib.ttf"
SEGOE_SB = r"C:\Windows\Fonts\seguisb.ttf"


def F(path, size):
    return ImageFont.truetype(path, size)


def canvas(w, h):
    img = Image.new("RGB", (w, h), BG_TOP)
    px = img.load()
    for y in range(h):
        t = y / (h - 1)
        row = (
            int(BG_TOP[0] + (BG_BOTTOM[0] - BG_TOP[0]) * t),
            int(BG_TOP[1] + (BG_BOTTOM[1] - BG_TOP[1]) * t),
            int(BG_TOP[2] + (BG_BOTTOM[2] - BG_TOP[2]) * t),
        )
        for x in range(w):
            px[x, y] = row
    glow = Image.new("RGB", (w, h), (0, 0, 0))
    g = ImageDraw.Draw(glow)
    rmax = min(w, h) // 2 + 120
    for r in range(rmax, 0, -8):
        a = int(44 * (1 - r / rmax))
        g.ellipse(
            (w / 2 - r, h * 0.22 - r, w / 2 + r, h * 0.22 + r),
            fill=(a // 3, a, a * 2 // 3),
        )
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    img = Image.blend(img, glow, 0.6)
    return img, ImageDraw.Draw(img)


def header(img, draw, w, centered=False, y=64, icon_sz=84):
    icon = (
        Image.open(r"C:\repo\muslim-pro-plus\docs\store\icon-512.png")
        .convert("RGBA")
        .resize((icon_sz, icon_sz), Image.LANCZOS)
    )
    mask = Image.new("L", (icon_sz, icon_sz), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, icon_sz - 1, icon_sz - 1), radius=int(icon_sz * 0.22), fill=255
    )
    if centered:
        x = (w - icon_sz) // 2
        img.paste(icon, (x, y), mask)
        title = "Muslim Plus"
        f = F(GEORGIA_B, int(icon_sz * 0.5))
        tw = draw.textlength(title, font=f)
        draw.text(((w - tw) / 2, y + icon_sz + 18), title, font=f, fill=INK)
        sub = "Orações · Qibla · Alcorão"
        fs = F(SEGOE, int(icon_sz * 0.24))
        sw = draw.textlength(sub, font=fs)
        draw.text(
            ((w - sw) / 2, y + icon_sz + 18 + int(icon_sz * 0.62)),
            sub,
            font=fs,
            fill=MUTED,
        )
    else:
        img.paste(icon, (72, y), mask)
        draw.text(
            (72 + icon_sz + 22, y + 4),
            "Muslim Plus",
            font=F(GEORGIA_B, int(icon_sz * 0.42)),
            fill=INK,
        )
        draw.text(
            (72 + icon_sz + 24, y + 4 + int(icon_sz * 0.5)),
            "Orações · Qibla · Alcorão",
            font=F(SEGOE, int(icon_sz * 0.22)),
            fill=MUTED,
        )


def arrow(draw, cx, top, bottom, width=48, head_w=168, head_h=118):
    draw.rounded_rectangle(
        (cx - width // 2, top, cx + width // 2, bottom - head_h + 10),
        radius=width // 2,
        fill=EMERALD,
    )
    draw.polygon(
        [
            (cx - head_w // 2, bottom - head_h),
            (cx + head_w // 2, bottom - head_h),
            (cx, bottom),
        ],
        fill=EMERALD,
    )


def cta_pill(draw, cx, y, text="Grátis no Google Play"):
    f = F(SEGOE_SB, 26)
    w = int(draw.textlength(text, font=f) + 92)
    x = cx - w // 2
    draw.rounded_rectangle((x, y, x + w, y + 78), radius=39, fill=EMERALD)
    draw.polygon(
        [(x + 34, y + 24), (x + 34, y + 54), (x + 58, y + 39)], fill=(6, 35, 27)
    )
    draw.text((x + 74, y + 24), text, font=f, fill=(6, 35, 27))


# quadrado 1080
img, draw = canvas(1080, 1080)
header(img, draw, 1080)
eyebrow = "ANDROID · GRÁTIS"
draw.text((72, 258), eyebrow, font=F(SEGOE_B, 18), fill=GOLD)
draw.text((66, 294), "BAIXE", font=F(GEORGIA_B, 124), fill=INK)
draw.text((66, 426), "AQUI", font=F(GEORGIA_B, 124), fill=EMERALD)
arrow(draw, 760, 320, 600, width=54, head_w=182, head_h=126)
cta_pill(draw, 540, 720)
draw.text(
    (72, 880), "Orações, Qibla e Alcorão offline.", font=F(GEORGIA, 28), fill=MUTED
)
draw.text(
    (72, 924),
    "Sem conta. Sem anúncios. Sem rastreamento.",
    font=F(SEGOE, 20),
    fill=MUTED,
)
draw.line((72, 985, 1008, 985), fill=LINE, width=1)
draw.text((72, 1006), "muslimplus.com.br", font=F(GEORGIA, 22), fill=GOLD)
tw = draw.textlength("Muslim Plus · 1.2.0", font=F(SEGOE, 17))
draw.text((1008 - tw, 1010), "Muslim Plus · 1.2.0", font=F(SEGOE, 17), fill=MUTED)
img.save(OUT / "cta-baixe.png")
img.save(OUT / "cta-baixe.jpg", quality=94)

# story 1080x1920
img, draw = canvas(1080, 1920)
header(img, draw, 1080, centered=True, y=120, icon_sz=96)
draw.text((72, 520), "ANDROID · GRÁTIS", font=F(SEGOE_B, 20), fill=GOLD)
draw.text((66, 560), "BAIXE", font=F(GEORGIA_B, 150), fill=INK)
draw.text((66, 720), "AQUI", font=F(GEORGIA_B, 150), fill=EMERALD)
arrow(draw, 540, 980, 1460, width=64, head_w=220, head_h=150)
cta_pill(draw, 540, 1540)
draw.text(
    (72, 1660), "Orações, Qibla e Alcorão offline.", font=F(GEORGIA, 30), fill=MUTED
)
draw.text((72, 1710), "Toque no link abaixo.", font=F(SEGOE, 22), fill=MUTED)
img.save(OUT / "cta-baixe-story.png")

# icone 512 (capa de destaque)
img, draw = canvas(512, 512)
arrow(draw, 256, 96, 330, width=34, head_w=120, head_h=86)
text = "BAIXE AQUI"
tw = draw.textlength(text, font=F(SEGOE_B, 34))
draw.text(((512 - tw) / 2, 382), text, font=F(SEGOE_B, 34), fill=EMERALD)
img.save(OUT / "cta-baixe-icon.png")
print("ok", (OUT / "cta-baixe.jpg").stat().st_size)
