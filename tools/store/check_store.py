"""Confere a ficha de loja e os assets de `docs/store/`.

Verifica o que o Play Console cobra e o que e facil de quebrar sem perceber:
limites de texto, dimensoes dos PNG, ausencia de canal alfa nos assets que o
Play exige em 24 bits, e screenshots que nao sejam tela preta/branca.

Uso:
    python tools/store/check_store.py

Sai com codigo 1 e lista o que falhou.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageStat

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "docs" / "store"

# chave do heading -> (nome do campo, limite de caracteres)
LISTING_FIELDS = {
    "Título": ("titulo", 30),
    "Descrição curta": ("curta", 80),
    "Descrição completa": ("completa", 4000),
    "O que há de novo": ("novidades", 500),
}

SCREENSHOTS = {
    "pt": ["01-hoje.png", "02-oracoes.png", "03-leitor-alcorao.png", "04-qibla.png"],
    "en": ["01-today.png", "02-prayers.png", "03-quran-reader.png", "04-qibla.png"],
}

PROBLEMS: list[str] = []


def fail(msg: str) -> None:
    PROBLEMS.append(msg)


def parse_listing(path: Path) -> dict[str, str]:
    """Le os campos pelo heading `## <campo>`; o texto vai ate o proximo heading."""
    fields: dict[str, str] = {}
    current: str | None = None
    buffer: list[str] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("## "):
            if current:
                fields[current] = "\n".join(buffer).strip()
            current = line[3:].strip()
            buffer = []
        elif current:
            buffer.append(line)
    if current:
        fields[current] = "\n".join(buffer).strip()
    return fields


def check_listing(locale: str) -> None:
    path = STORE / f"listing-{locale}.md"
    if not path.exists():
        fail(f"{path.name}: arquivo ausente")
        return
    fields = parse_listing(path)
    for heading, (name, limit) in LISTING_FIELDS.items():
        match = next((k for k in fields if k.startswith(heading)), None)
        if match is None:
            fail(f"{path.name}: campo '{heading}' ausente")
            continue
        text = fields[match]
        if not text:
            fail(f"{path.name}: campo '{heading}' vazio")
            continue
        if name in ("titulo", "curta"):
            if "\n" in text:
                fail(f"{path.name}: '{heading}' deve ser uma linha so")
            size = len(text)
        else:
            size = len(text)
        status = "ok" if size <= limit else "ESTOUROU"
        print(f"{path.name:16s} {heading:22s} {size:5d}/{limit} {status}")
        if size > limit:
            fail(f"{path.name}: '{heading}' com {size} caracteres (limite {limit})")


def check_image(
    path: Path, size: tuple[int, int], allow_alpha: bool = False
) -> Image.Image | None:
    if not path.exists():
        fail(f"{path.relative_to(ROOT)}: ausente")
        return None
    with Image.open(path) as im:
        real = im.size
        mode = im.mode
        im.load()
        if real != size:
            fail(
                f"{path.relative_to(ROOT)}: {real[0]}x{real[1]}, esperado {size[0]}x{size[1]}"
            )
        if not allow_alpha and mode not in ("RGB", "P"):
            fail(
                f"{path.relative_to(ROOT)}: modo {mode}; o Play pede PNG 24 bits (sem alfa)"
            )
        print(f"{str(path.relative_to(ROOT)):48s} {real[0]:4d}x{real[1]:<4d} {mode}")
        return im.copy()


def check_not_blank(im: Image.Image, label: str) -> None:
    rgb = im.convert("RGB")
    colors = rgb.getcolors(maxcolors=1 << 20)
    unique = len(colors) if colors else 1 << 20
    stat = ImageStat.Stat(rgb)
    spread = max(stat.stddev)
    if unique < 500 or spread < 8:
        fail(f"{label}: parece tela vazia/preta (cores={unique}, stddev={spread:.1f})")
    else:
        print(f"{label:48s} cores={unique:<6d} stddev={spread:.1f}")


def main() -> int:
    for locale in ("pt", "en"):
        check_listing(locale)

    check_image(STORE / "icon-512.png", (512, 512))
    check_image(STORE / "feature-graphic.png", (1024, 500))
    check_image(STORE / "feature-graphic-pt.png", (1024, 500))
    check_image(STORE / "feature-graphic-en.png", (1024, 500))

    for locale, names in SCREENSHOTS.items():
        folder = STORE / "screenshots" / locale
        present = (
            sorted(p.name for p in folder.glob("*.png")) if folder.exists() else []
        )
        if len(present) != len(names):
            fail(f"screenshots/{locale}: {len(present)} PNGs, esperado {len(names)}")
        for name in names:
            path = folder / name
            im = check_image(path, (1080, 1920))
            if im is not None:
                check_not_blank(im, str(path.relative_to(ROOT)))

    if PROBLEMS:
        print("\nFALHAS:")
        for p in PROBLEMS:
            print(" -", p)
        return 1
    print("\nTudo certo: ficha e assets dentro das regras da Play.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
