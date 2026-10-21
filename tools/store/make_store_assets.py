"""Gera o icone 512x512 e o feature graphic 1024x500 da Play Store.

As formas vem do mesmo vetor do icone adaptativo do app
(`app/src/main/res/drawable/ic_launcher.xml`): crescente esmeralda vazado,
estrela de oito pontas dourada no vao, fundo azul-noite. Nada de asset de
terceiros; tudo desenhado aqui.

Uso:
    python tools/store/make_store_assets.py

Saida em docs/store/:
    icon-512.png                 512x512
    feature-graphic.png          1024x500, sem texto de idioma
    feature-graphic-pt.png       1024x500, legenda em pt-BR
    feature-graphic-en.png       1024x500, legenda em ingles

Requer Pillow e uma fonte serif do Windows (Georgia).
"""

from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "store"

MIDNIGHT = (8, 19, 28)  # #08131C
MIDNIGHT_SOFT = (14, 27, 38)  # #0E1B26
EMERALD = (47, 185, 138)  # #2FB98A
GOLD = (217, 178, 95)  # #D9B25F
ON_BACKGROUND = (233, 238, 244)  # #E9EEF4
ON_SURFACE_VARIANT = (159, 176, 194)  # #9FB0C2

# Coordenadas do vetor do app, em unidades de 108x108 (viewport do drawable).
CRESCENT_C = (54.0, 54.0)
CRESCENT_R = 30.0
CUT_C = (61.5, 49.5)
CUT_R = 27.0
STAR_C = (68.0, 43.0)
STAR_R_OUT = 7.5
STAR_R_IN = 3.2
# Caixa que envolve a marca inteira (crescente 24..84 nos dois eixos).
MARK_BOX = 60.0

SS = 4  # supersampling


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    path = Path("C:/Windows/Fonts") / name
    if not path.exists():
        sys.exit(f"fonte nao encontrada: {path}")
    return ImageFont.truetype(str(path), size)


def star_points(
    cx: float, cy: float, r_out: float, r_in: float
) -> list[tuple[float, float]]:
    pts = []
    for i in range(16):
        angle = math.radians(-90 + i * 22.5)
        r = r_out if i % 2 == 0 else r_in
        pts.append((cx + r * math.cos(angle), cy + r * math.sin(angle)))
    return pts


def draw_mark(canvas: Image.Image, center: tuple[float, float], box: float) -> None:
    """Desenha crescente + estrela centralizados em `center`, com `box` px de largura."""
    s = box / MARK_BOX  # px por unidade do viewport 108
    ox = center[0] - CRESCENT_C[0] * s
    oy = center[1] - CRESCENT_C[1] * s

    mask = Image.new("L", canvas.size, 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.ellipse(
        [
            ox + (CRESCENT_C[0] - CRESCENT_R) * s,
            oy + (CRESCENT_C[1] - CRESCENT_R) * s,
            ox + (CRESCENT_C[0] + CRESCENT_R) * s,
            oy + (CRESCENT_C[1] + CRESCENT_R) * s,
        ],
        fill=255,
    )
    # O corte do crescente e o mesmo circulo deslocado do vetor (evenOdd no SVG).
    mdraw.ellipse(
        [
            ox + (CUT_C[0] - CUT_R) * s,
            oy + (CUT_C[1] - CUT_R) * s,
            ox + (CUT_C[0] + CUT_R) * s,
            oy + (CUT_C[1] + CUT_R) * s,
        ],
        fill=0,
    )
    canvas.paste(Image.new("RGB", canvas.size, EMERALD), (0, 0), mask)

    star = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(star)
    pts = star_points(
        ox + STAR_C[0] * s, oy + STAR_C[1] * s, STAR_R_OUT * s, STAR_R_IN * s
    )
    sdraw.polygon(pts, fill=GOLD + (255,))
    canvas.paste(star, (0, 0), star)


def radial_glow(
    size: tuple[int, int],
    center: tuple[float, float],
    radius: float,
    color: tuple[int, int, int],
    peak: float,
) -> Image.Image:
    """Brilho radial suave, calculado por pixel para nao deixar banding."""
    w, h = size
    cx, cy = center
    img = Image.new("RGB", size, MIDNIGHT)
    px = img.load()
    for y in range(h):
        for x in range(w):
            d = math.hypot(x - cx, y - cy) / radius
            if d >= 1:
                continue
            t = (1 - d) ** 2 * peak
            r, g, b = px[x, y]
            px[x, y] = (
                int(r + (color[0] - r) * t),
                int(g + (color[1] - g) * t),
                int(b + (color[2] - b) * t),
            )
    return img


def make_icon() -> None:
    size = 512 * SS
    base = radial_glow(
        (size, size), (size * 0.5, size * 0.46), size * 0.62, MIDNIGHT_SOFT, 0.85
    )
    draw_mark(base, (size / 2, size / 2), size * 0.62)
    # BOX (media de area) em vez de LANCZOS: o LANCZOS deixa ringing escuro nas
    # bordas do crescente, que vira poeira sobre o fundo. BOX nao inventa cor.
    base.resize((512, 512), Image.BOX).convert("RGB").save(
        OUT / "icon-512.png", "PNG", optimize=True
    )


def make_feature(subtitle: str | None, name: str) -> None:
    w, h = 1024 * SS, 500 * SS
    canvas = radial_glow((w, h), (w * 0.13, h * 0.52), w * 0.42, EMERALD, 0.16)

    mark_box = h * 0.52
    draw_mark(canvas, (w * 0.155, h * 0.50), mark_box)

    title_font = font("georgiab.ttf", int(96 * SS))
    sub_font = font("segoeui.ttf", int(27 * SS))
    draw = ImageDraw.Draw(canvas)
    text_x = w * 0.31

    title = "Muslim Plus"
    t_box = draw.textbbox((0, 0), title, font=title_font)
    t_h = t_box[3] - t_box[1]

    if subtitle:
        gap = 26 * SS
        sub_box = draw.textbbox((0, 0), subtitle, font=sub_font)
        sub_h = sub_box[3] - sub_box[1]
        block = t_h + gap + 10 * SS + sub_h
        top = (h - block) / 2
        draw.text((text_x, top - t_box[1]), title, font=title_font, fill=ON_BACKGROUND)
        rule_y = top + t_h + gap
        draw.rectangle(
            [text_x + 2 * SS, rule_y, text_x + 98 * SS, rule_y + 3 * SS], fill=GOLD
        )
        draw.text(
            (text_x, rule_y + 10 * SS - sub_box[1]),
            subtitle,
            font=sub_font,
            fill=ON_SURFACE_VARIANT,
        )
    else:
        draw.text(
            (text_x, (h - t_h) / 2 - t_box[1]),
            title,
            font=title_font,
            fill=ON_BACKGROUND,
        )

    canvas.resize((1024, 500), Image.BOX).convert("RGB").save(
        OUT / name, "PNG", optimize=True
    )


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    make_icon()
    make_feature(None, "feature-graphic.png")
    make_feature("Horários de oração · Qibla · Alcorão", "feature-graphic-pt.png")
    make_feature("Prayer times · Qibla · Quran", "feature-graphic-en.png")
    for p in sorted(OUT.glob("*.png")):
        with Image.open(p) as im:
            print(f"{p.relative_to(ROOT)}  {im.size[0]}x{im.size[1]}  {im.mode}")


if __name__ == "__main__":
    main()
