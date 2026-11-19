"""Gera as telas da ficha da Play Store com legenda.

Entrada: capturas cruas do app em `docs/store/screenshots/src/{pt,en}/`, tiradas
no emulador em 1080x1920 (o script corta a barra de status e a barra de gestos,
que trazem hora e idioma do sistema).

Saida: `docs/store/screenshots/{pt,en}/01..07.png`, 1080x1920, RGB.

Regras do layout:
- fundo #0A0C10, uma mensagem por quadro, titulo em Georgia Bold de 74 a 84 px
  (cabe em duas linhas), sobretitulo dourado em caixa alta.
- o app aparece inteiro dentro de uma moldura arredondada; nada de texto solto
  cobrindo a interface.
- o ultimo quadro nao tem captura: e o painel de privacidade, que e o argumento
  que separa o app do Muslim Pro.

Uso:
    python tools/store/make_screenshots.py
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "docs" / "store"
SRC = STORE / "screenshots" / "src"

W, H = 1080, 1920
BACKGROUND = (10, 12, 16)  # #0A0C10
PANEL = (18, 22, 28)  # #12161C
BORDER = (36, 43, 54)  # #242B36
TEXT = (233, 237, 243)  # #E9EDF3
TEXT_DIM = (152, 163, 179)  # #98A3B3
EMERALD = (94, 203, 141)  # #5ECB8D
GOLD = (217, 178, 95)  # #D9B25F

CROP_TOP = 72
CROP_BOTTOM = 1864
PHONE_W = 820
PHONE_X = (W - PHONE_W) // 2
PHONE_Y = 470
PHONE_RADIUS = 48

# (arquivo de origem, sobretitulo, titulo, texto de apoio)
FRAMES = {
    "pt": [
        (
            "today",
            "HORÁRIOS DE ORAÇÃO",
            "No seu fuso, mesmo offline",
            "3.000 cidades com o fuso da cidade",
        ),
        (
            "reader",
            "ALCORÃO",
            "6.236 versos, com tradução e áudio",
            "Árabe, transliteração e português",
        ),
        (
            "qibla",
            "QIBLA",
            "Bússola e distância até a Caaba",
            "Calibração em figura-8 quando precisa",
        ),
        (
            "tracker",
            "RASTREADOR",
            "Acompanhe sua sequência de orações",
            "Anel do dia, semana e histórico",
        ),
        (
            "adhan",
            "ADHAN",
            "Um lembrete em cada oração",
            "22 vozes, alarme completo e aviso antes",
        ),
        (
            "prayers",
            "TABELA DO MÊS",
            "O mês inteiro, oração por oração",
            "12 métodos de cálculo e ajuste fino",
        ),
        ("privacy", None, None, None),
    ],
    "en": [
        (
            "today",
            "PRAYER TIMES",
            "In your city, even offline",
            "3,000 cities with their own time zone",
        ),
        (
            "reader",
            "QURAN",
            "6,236 verses, translation and audio",
            "Arabic, transliteration and English",
        ),
        (
            "qibla",
            "QIBLA",
            "Compass and distance to the Kaaba",
            "Figure-8 calibration when it drifts",
        ),
        (
            "tracker",
            "PRAYER TRACKER",
            "Keep your streak going",
            "Daily ring, week view and history",
        ),
        (
            "adhan",
            "ADHAN",
            "One reminder at every prayer",
            "22 voices, full alarm and pre-adhan",
        ),
        (
            "prayers",
            "MONTH TABLE",
            "The whole month, prayer by prayer",
            "12 calculation methods and fine tuning",
        ),
        ("privacy", None, None, None),
    ],
}

PRIVACY = {
    "pt": {
        "title": "Sem conta.\nSem anúncios.\nSem rastreamento.",
        "bullets": [
            "Nenhum SDK de anúncio, analytics ou rastreio",
            "A localização não sai do aparelho",
            "Alcorão e horários funcionam sem internet",
        ],
        "foot": "Muslim Plus · grátis, sem assinatura",
    },
    "en": {
        "title": "No account.\nNo ads.\nNo tracking.",
        "bullets": [
            "No ad, analytics or tracking SDK",
            "Location never leaves the device",
            "Quran and prayer times work offline",
        ],
        "foot": "Muslim Plus · free, no subscription",
    },
}


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    path = Path("C:/Windows/Fonts") / name
    if not path.exists():
        sys.exit(f"fonte nao encontrada: {path}")
    return ImageFont.truetype(str(path), size)


def tracked(draw: ImageDraw.ImageDraw, xy, text: str, f, fill, extra: int) -> None:
    x, y = xy
    for ch in text:
        draw.text((x, y), ch, font=f, fill=fill)
        x += draw.textlength(ch, font=f) + extra


def wrap(draw: ImageDraw.ImageDraw, text: str, f, max_w: int) -> list[str]:
    words = text.split()
    lines: list[str] = []
    line = ""
    for word in words:
        candidate = f"{line} {word}".strip()
        if draw.textlength(candidate, font=f) <= max_w or not line:
            line = candidate
        else:
            lines.append(line)
            line = word
    if line:
        lines.append(line)
    return lines


def fit_lines(draw: ImageDraw.ImageDraw, text: str, max_w: int, max_lines: int = 2):
    for size in (84, 80, 76, 72, 68):
        f = font("georgiab.ttf", size)
        lines = wrap(draw, text, f, max_w)
        if len(lines) <= max_lines:
            return f, size, lines
    return font("georgiab.ttf", 68), 68, lines


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, size[0] - 1, size[1] - 1], radius=radius, fill=255
    )
    return mask


def glow(canvas: Image.Image, center: tuple[float, float], radius: float) -> None:
    """Brilho radial esmeralda bem discreto no topo."""
    overlay = Image.new("RGB", canvas.size, BACKGROUND)
    px = overlay.load()
    cx, cy = center
    for y in range(min(canvas.size[1], int(cy + radius))):
        for x in range(0, canvas.size[0], 2):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 / radius
            if d >= 1:
                continue
            t = (1 - d) ** 3 * 0.22
            r, g, b = px[x, y]
            px[x, y] = (
                int(r + (EMERALD[0] - r) * t),
                int(g + (EMERALD[1] - g) * t),
                int(b + (EMERALD[2] - b) * t),
            )
            if x + 1 < canvas.size[0]:
                px[x + 1, y] = px[x, y]
    canvas.paste(overlay, (0, 0))


def phone_frame(src: Path) -> Image.Image:
    shot = Image.open(src).convert("RGB")
    if shot.size != (1080, 1920):
        sys.exit(f"{src}: esperado 1080x1920, veio {shot.size}")
    shot = shot.crop((0, CROP_TOP, 1080, CROP_BOTTOM))
    height = round(shot.size[1] * PHONE_W / shot.size[0])
    shot = shot.resize((PHONE_W, height), Image.LANCZOS)

    frame = Image.new("RGB", (PHONE_W + 8, height + 8), BORDER)
    frame.paste(shot, (4, 4), rounded_mask(shot.size, PHONE_RADIUS - 4))
    return frame


def make_frame(locale: str, name: str, kicker: str, title: str, support: str) -> None:
    canvas = Image.new("RGB", (W, H), BACKGROUND)
    glow(canvas, (W * 0.5, -120), 900)
    draw = ImageDraw.Draw(canvas)

    tracked(draw, (90, 112), kicker, font("segoeuib.ttf", 32), GOLD, 6)

    f, size, lines = fit_lines(draw, title, W - 180)
    y = 176
    for line in lines:
        draw.text((90, y), line, font=f, fill=TEXT)
        y += round(size * 1.18)

    draw.text((90, y + 14), support, font=font("segoeui.ttf", 34), fill=TEXT_DIM)

    frame = phone_frame(SRC / locale / f"{name}.png")
    canvas.paste(
        frame, (PHONE_X - 4, PHONE_Y - 4), rounded_mask(frame.size, PHONE_RADIUS)
    )

    out = STORE / "screenshots" / locale
    out.mkdir(parents=True, exist_ok=True)
    return canvas


def make_privacy(locale: str, index: int) -> Image.Image:
    data = PRIVACY[locale]
    canvas = Image.new("RGB", (W, H), BACKGROUND)
    glow(canvas, (W * 0.5, H * 0.15), 1100)
    draw = ImageDraw.Draw(canvas)

    # marca igual ao icone do app: crescente esmeralda e estrela dourada, sem
    # transparencia (mesmo draw_mark que gera o icone e o feature graphic).
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    from make_store_assets import draw_mark

    draw_mark(canvas, (W / 2, 400), 300)

    f = font("georgiab.ttf", 88)
    y = 620
    for line in data["title"].split("\n"):
        w = draw.textlength(line, font=f)
        draw.text(((W - w) / 2, y), line, font=f, fill=TEXT)
        y += 108

    y = 1030
    bf = font("segoeui.ttf", 38)
    for bullet in data["bullets"]:
        draw.ellipse([120, y + 14, 138, y + 32], fill=EMERALD)
        draw.text((170, y), bullet, font=bf, fill=TEXT)
        y += 88

    foot = data["foot"]
    ff = font("segoeui.ttf", 32)
    draw.text(
        ((W - draw.textlength(foot, font=ff)) / 2, 1660), foot, font=ff, fill=TEXT_DIM
    )
    return canvas


def main() -> None:
    for locale, frames in FRAMES.items():
        out_dir = STORE / "screenshots" / locale
        out_dir.mkdir(parents=True, exist_ok=True)
        for i, (name, kicker, title, support) in enumerate(frames, start=1):
            if name == "privacy":
                image = make_privacy(locale, i)
            else:
                image = make_frame(locale, name, kicker, title, support)
            path = out_dir / f"{i:02d}-{name}.png"
            image.convert("RGB").save(path, "PNG", optimize=True)
            print(f"{path.relative_to(ROOT)}  {image.size[0]}x{image.size[1]}  RGB")


if __name__ == "__main__":
    main()
