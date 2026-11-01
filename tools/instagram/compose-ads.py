from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math

S = 1080
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


def base():
    img = Image.new("RGB", (S, S), BG_TOP)
    px = img.load()
    for y in range(S):
        t = y / (S - 1)
        row = (
            int(BG_TOP[0] + (BG_BOTTOM[0] - BG_TOP[0]) * t),
            int(BG_TOP[1] + (BG_BOTTOM[1] - BG_TOP[1]) * t),
            int(BG_TOP[2] + (BG_BOTTOM[2] - BG_TOP[2]) * t),
        )
        for x in range(S):
            px[x, y] = row

    glow = Image.new("RGB", (S, S), (0, 0, 0))
    g = ImageDraw.Draw(glow)
    for r in range(520, 0, -6):
        a = int(40 * (1 - r / 520))
        g.ellipse(
            (S / 2 - r, -260 - r, S / 2 + r, -260 + r), fill=(a // 3, a, a * 2 // 3)
        )
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    img = Image.blend(img, glow, 0.6)
    draw = ImageDraw.Draw(img)

    icon = 76
    mask = Image.new("L", (icon, icon), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, icon - 1, icon - 1), radius=18, fill=255
    )
    ic = (
        Image.open(r"C:\repo\muslim-pro-plus\docs\store\icon-512.png")
        .convert("RGBA")
        .resize((icon, icon), Image.LANCZOS)
    )
    img.paste(ic, (72, 64), mask)
    draw.text((170, 70), "Muslim Plus", font=F(GEORGIA_B, 34), fill=INK)
    draw.text((172, 116), "Orações · Qibla · Alcorão", font=F(SEGOE, 18), fill=MUTED)
    return img, draw


def block(draw, eyebrow, lines, sub, cta="Grátis no Google Play"):
    draw.text((72, 210), eyebrow, font=F(SEGOE_B, 16), fill=GOLD)
    y = 242
    for text, color in lines:
        draw.text((72, y), text, font=F(GEORGIA_B, 64), fill=color)
        y += 74
    draw.text((72, 806), sub, font=F(SEGOE, 22), fill=MUTED)
    cta_w, cta_h = 384, 76
    draw.rounded_rectangle((72, 896, 72 + cta_w, 896 + cta_h), radius=38, fill=EMERALD)
    draw.polygon(
        [(72 + 40, 896 + 24), (72 + 40, 896 + 52), (72 + 62, 896 + 38)],
        fill=(6, 35, 27),
    )
    draw.text((72 + 80, 896 + 21), cta, font=F(SEGOE_SB, 23), fill=(6, 35, 27))
    draw.text((72, 1012), "Muslim Plus · 1.2.0", font=F(GEORGIA, 18), fill=MUTED)
    draw.text((72, 1042), "pt · en · ar", font=F(SEGOE, 15), fill=GOLD)


def qibla():
    img, draw = base()
    block(
        draw,
        "QIBLA",
        [("A direção certa,", INK), ("onde você estiver.", INK)],
        "Bússola com calibração e aviso de interferência.",
    )
    cx, cy, r = 700, 560, 168
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=LINE, width=3)
    draw.ellipse(
        (cx - r + 14, cy - r + 14, cx + r - 14, cy + r - 14),
        outline=(30, 52, 62),
        width=1,
    )
    for i in range(72):
        ang = math.radians(i * 5)
        long = i % 6 == 0
        inner = r - (22 if long else 11)
        x1 = cx + inner * math.sin(ang)
        y1 = cy - inner * math.cos(ang)
        x2 = cx + (r - 5) * math.sin(ang)
        y2 = cy - (r - 5) * math.cos(ang)
        draw.line(
            (x1, y1, x2, y2), fill=EMERALD if long else LINE, width=3 if long else 1
        )
    angle = math.radians(38)
    tip = (cx + (r - 34) * math.sin(angle), cy - (r - 34) * math.cos(angle))
    left = (cx - 16, cy + 34)
    right = (cx + 16, cy + 34)
    draw.polygon([tip, left, right], fill=EMERALD)
    draw.polygon([tip, (cx - 1, cy + 2), (cx + 1, cy + 2)], fill=GOLD)
    draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), fill=GOLD)
    draw.ellipse((cx - 5, cy - 5, cx + 5, cy + 5), fill=(6, 20, 26))
    for label, ang in (("N", 0), ("L", 90), ("S", 180), ("O", 270)):
        a = math.radians(ang)
        tx = cx + (r - 52) * math.sin(a)
        ty = cy - (r - 52) * math.cos(a)
        draw.text(
            (tx - 6, ty - 10),
            label,
            font=F(SEGOE_B, 18),
            fill=GOLD if label == "N" else MUTED,
        )
    draw.text((cx + 60, cy + 200), "285°", font=F(GEORGIA_B, 30), fill=EMERALD)
    img.save(OUT / "ad-qibla.jpg", quality=94)
    img.save(OUT / "ad-qibla.png")
    return img


def adhan():
    img, draw = base()
    block(
        draw,
        "ADHAN",
        [("O chamado no horário.", INK), ("O aviso antes.", EMERALD)],
        "Lembrete silencioso por oração, de 5 a 30 minutos.",
    )
    cx, cy = 700, 520
    draw.ellipse((cx - 40, cy - 130, cx + 40, cy - 50), fill=GOLD)
    draw.rectangle((cx - 6, cy - 76, cx + 6, cy - 50), fill=GOLD)
    draw.ellipse((cx - 104, cy - 62, cx + 104, cy + 110), fill=GOLD)
    draw.rectangle((cx - 104, cy + 20, cx + 104, cy + 92), fill=GOLD)
    draw.polygon(
        [
            (cx - 128, cy + 96),
            (cx + 128, cy + 96),
            (cx + 104, cy + 64),
            (cx - 104, cy + 64),
        ],
        fill=GOLD,
    )
    draw.ellipse((cx - 20, cy + 100, cx + 20, cy + 140), fill=GOLD)
    draw.ellipse((cx - 10, cy + 110, cx + 10, cy + 130), fill=(7, 24, 32))
    chips = ["5 min", "10 min", "15 min", "30 min"]
    x = 470
    y = 710
    for c in chips:
        w = draw.textlength(c, font=F(SEGOE_SB, 17)) + 28
        draw.rounded_rectangle(
            (x, y, x + w, y + 40),
            radius=20,
            fill=CARD,
            outline=EMERALD if c == "10 min" else LINE,
            width=2,
        )
        draw.text(
            (x + 14, y + 8),
            c,
            font=F(SEGOE_SB, 17),
            fill=EMERALD if c == "10 min" else MUTED,
        )
        x += w + 12
    img.save(OUT / "ad-adhan.jpg", quality=94)
    img.save(OUT / "ad-adhan.png")
    return img


def quran():
    img, draw = base()
    block(
        draw,
        "ALCORÃO",
        [("6.236 versos.", INK), ("No seu bolso.", EMERALD)],
        "Leitura, tradução e áudio. Sem internet.",
    )
    cx, cy = 700, 540
    left = [
        (cx - 190, cy - 90),
        (cx - 16, cy - 64),
        (cx - 16, cy + 96),
        (cx - 190, cy + 62),
    ]
    right = [
        (cx + 190, cy - 90),
        (cx + 16, cy - 64),
        (cx + 16, cy + 96),
        (cx + 190, cy + 62),
    ]
    draw.polygon(left, fill=(18, 42, 50), outline=GOLD)
    draw.polygon(right, fill=(18, 42, 50), outline=GOLD)
    draw.line((cx, cy - 64, cx, cy + 96), fill=GOLD, width=3)
    for i in range(5):
        yy = cy - 38 + i * 24
        draw.line((cx - 168, yy - 8, cx - 40, yy + 6), fill=(80, 110, 118), width=3)
        draw.line((cx + 40, yy + 6, cx + 168, yy - 8), fill=(80, 110, 118), width=3)
    draw.rounded_rectangle(
        (cx - 66, cy + 126, cx + 66, cy + 170),
        radius=22,
        fill=(23, 62, 53),
        outline=EMERALD,
    )
    draw.text((cx - 36, cy + 136), "Offline", font=F(SEGOE_SB, 19), fill=EMERALD)
    img.save(OUT / "ad-quran.jpg", quality=94)
    img.save(OUT / "ad-quran.png")
    return img


def privacy():
    img, draw = base()
    block(
        draw,
        "PRIVACIDADE",
        [("Sem conta.", INK), ("Sem anúncios.", INK), ("Sem rastreamento.", EMERALD)],
        "Tudo fica no seu aparelho. Nada vai para servidor.",
    )
    cx, cy, s = 856, 590, 0.78
    shield = [
        (cx - 150 * s, cy - 120 * s),
        (cx, cy - 160 * s),
        (cx + 150 * s, cy - 120 * s),
        (cx + 128 * s, cy + 60 * s),
        (cx, cy + 170 * s),
        (cx - 128 * s, cy + 60 * s),
    ]
    draw.polygon(shield, fill=(16, 48, 48), outline=EMERALD)
    draw.line(shield + [shield[0]], fill=EMERALD, width=3)
    draw.line(
        (cx - 54 * s, cy + 8 * s, cx - 14 * s, cy + 48 * s), fill=EMERALD, width=11
    )
    draw.line(
        (cx - 14 * s, cy + 48 * s, cx + 68 * s, cy - 44 * s), fill=EMERALD, width=11
    )
    img.save(OUT / "ad-privacy.jpg", quality=94)
    img.save(OUT / "ad-privacy.png")
    return img


if __name__ == "__main__":
    imgs = [qibla(), adhan(), quran(), privacy()]
    sheet = Image.new("RGB", (S * 2, S * 2))
    for i, im in enumerate(imgs):
        sheet.paste(im, ((i % 2) * S, (i // 2) * S))
    sheet.save(OUT / "ads-sheet.jpg", quality=90)
    print("ok", [im.size for im in imgs])
