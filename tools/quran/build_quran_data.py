"""Gera os assets do Alcorao a partir da API do Quran.com.

Baixa (se faltar em build/quran) e gera:
  app/src/main/assets/quran_verses.txt          -> "sura:ayah|uthmani"
  app/src/main/assets/quran_surahs.txt          -> "id|nome arabe|nome latino|versiculos"
  app/src/main/assets/quran_transliteration.txt -> "sura:ayah|transliteracao"
  app/src/main/assets/quran_translation_<locale>.txt -> "sura:ayah|traducao"

REGRA: toda lingua suportada pelo app (AppLocale.supported, exceto ar) precisa
de uma entrada em TRANSLATIONS. O teste QuranLocaleCoverageTest quebra o build
quando alguem adiciona idioma sem traducao.

Uso: python tools/quran/build_quran_data.py
"""

import html
import json
import pathlib
import re
import urllib.request

BASE = pathlib.Path(__file__).resolve().parents[2]
SOURCE = BASE / "build" / "quran"
TARGET = BASE / "app" / "src" / "main" / "assets"
API = "https://api.quran.com/api/v4/quran/translations"

TRANSLATIONS = {
    "en": 19,  # M. Pickthall (dominio publico)
    "pt-BR": 103,  # Helmi Nasr
}
TRANSLITERATION = 57


def fetch(name: str, resource_id: int) -> dict:
    SOURCE.mkdir(parents=True, exist_ok=True)
    path = SOURCE / f"{name}.json"
    if not path.exists():
        url = f"{API}/{resource_id}"
        request = urllib.request.Request(
            url,
            headers={
                "User-Agent": "MuslimPlus/0.1 (data build)",
                "Accept": "application/json",
            },
        )
        with urllib.request.urlopen(request, timeout=120) as response:
            path.write_bytes(response.read())
    return json.loads(path.read_text(encoding="utf-8"))


def clean(text: str) -> str:
    text = re.sub(r"<[^>]+>", "", text)
    return html.unescape(text).replace("\n", " ").strip()


def write_verses(name: str, verses: list, texts: list) -> None:
    with (TARGET / name).open("w", encoding="utf-8", newline="\n") as handle:
        for verse, text in zip(verses, texts):
            handle.write(f"{verse['verse_key']}|{clean(text)}\n")


def main() -> None:
    verses = json.loads((SOURCE / "verses.json").read_text(encoding="utf-8"))["verses"]
    chapters = json.loads((SOURCE / "chapters.json").read_text(encoding="utf-8"))[
        "chapters"
    ]
    TARGET.mkdir(parents=True, exist_ok=True)

    with (TARGET / "quran_verses.txt").open(
        "w", encoding="utf-8", newline="\n"
    ) as handle:
        for verse in verses:
            handle.write(f"{verse['verse_key']}|{clean(verse['text_uthmani'])}\n")

    with (TARGET / "quran_surahs.txt").open(
        "w", encoding="utf-8", newline="\n"
    ) as handle:
        for chapter in chapters:
            handle.write(
                f"{chapter['id']}|{chapter['name_arabic']}|"
                f"{chapter['name_simple']}|{chapter['verses_count']}\n"
            )

    for locale, resource_id in TRANSLATIONS.items():
        data = fetch(f"translation_{resource_id}", resource_id)["translations"]
        if len(data) != len(verses):
            raise SystemExit(
                f"{locale}: {len(data)} versiculos, esperado {len(verses)}"
            )
        write_verses(
            f"quran_translation_{locale}.txt", verses, [item["text"] for item in data]
        )

    translit = fetch(f"translation_{TRANSLITERATION}", TRANSLITERATION)["translations"]
    write_verses(
        "quran_transliteration.txt", verses, [item["text"] for item in translit]
    )

    print(
        f"{len(verses)} versiculos, {len(chapters)} suras, {len(TRANSLATIONS)} traducoes"
    )


if __name__ == "__main__":
    main()
