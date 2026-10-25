"""Gera os PNGs do icone (crescente + estrela de cinco pontas).

Saidas:
  docs/store/icon-512.png                          (icone da ficha da Play Store)
  app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png   (camada colorida)
  app/src/main/res/drawable-xxxhdpi/ic_launcher_monochrome_layer.png (tema escuro)

A geometria fica no viewport de 108dp usado pelo icone adaptativo.
"""

from math import cos, radians, sin
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
STORE = ROOT / "docs/store/icon-512.png"
FOREGROUND = ROOT / "app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png"
MONOCHROME = ROOT / "app/src/main/res/drawable-xxxhdpi/ic_launcher_monochrome_layer.png"

BG = (11, 22, 34, 255)  # #0B1622
CRESCENT = (47, 185, 138, 255)  # #2FB98A
STAR = (217, 178, 95, 255)  # #D9B25F
CLEAR = (0, 0, 0, 0)

OUTER_C, OUTER_R = (53.0, 55.0), 30.0
INNER_C, INNER_R = (61.0, 49.0), 25.0
STAR_C, STAR_R, STAR_INNER = (71.0, 42.0), 9.5, 3.63


def star_points(scale: float) -> list[tuple[float, float]]:
    cx, cy = STAR_C
    points = []
    for index in range(10):
        angle = radians(-90 + index * 36)
        radius = STAR_R if index % 2 == 0 else STAR_INNER
        points.append(
            ((cx + radius * cos(angle)) * scale, (cy + radius * sin(angle)) * scale)
        )
    return points


def draw_mark(image: Image.Image, scale: float, crescent, star) -> None:
    draw = ImageDraw.Draw(image)
    o = OUTER_C
    i = INNER_C
    draw.ellipse(
        [
            (o[0] - OUTER_R) * scale,
            (o[1] - OUTER_R) * scale,
            (o[0] + OUTER_R) * scale,
            (o[1] + OUTER_R) * scale,
        ],
        fill=crescent,
    )
    draw.ellipse(
        [
            (i[0] - INNER_R) * scale,
            (i[1] - INNER_R) * scale,
            (i[0] + INNER_R) * scale,
            (i[1] + INNER_R) * scale,
        ],
        fill=CLEAR,
    )
    draw.polygon(star_points(scale), fill=star)


def main() -> None:
    STORE.parent.mkdir(parents=True, exist_ok=True)
    FOREGROUND.parent.mkdir(parents=True, exist_ok=True)

    # Icone da loja: 512 com fundo opaco.
    store = Image.new("RGBA", (512, 512), BG)
    draw_mark(store, 512 / 108.0, CRESCENT, STAR)
    store.save(STORE, format="PNG")

    # Camada adaptativa colorida: transparente, 432px = 108dp em xxxhdpi.
    foreground = Image.new("RGBA", (432, 432), CLEAR)
    draw_mark(foreground, 4.0, CRESCENT, STAR)
    foreground.save(FOREGROUND, format="PNG")

    # Camada monocromatica: o sistema tinge pelo alfa.
    monochrome = Image.new("RGBA", (432, 432), CLEAR)
    draw_mark(monochrome, 4.0, (255, 255, 255, 255), (255, 255, 255, 255))
    monochrome.save(MONOCHROME, format="PNG")

    for path in (STORE, FOREGROUND, MONOCHROME):
        print(path.relative_to(ROOT), path.stat().st_size)


if __name__ == "__main__":
    main()
