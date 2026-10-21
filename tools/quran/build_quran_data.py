"""Gera os assets do Alcorao a partir dos JSON baixados da API do Quran.com.

Saida:
  app/src/main/assets/quran_verses.txt   -> "sura:ayah|texto uthmani"
  app/src/main/assets/quran_surahs.txt   -> "id|nome arabe|nome latino|versiculos"

Uso: python tools/quran/build_quran_data.py
"""

import json
import pathlib

BASE = pathlib.Path(__file__).resolve().parents[2]
SOURCE = BASE / "build" / "quran"
TARGET = BASE / "app" / "src" / "main" / "assets"


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
            text = verse["text_uthmani"].replace("\n", " ").strip()
            handle.write(f"{verse['verse_key']}|{text}\n")

    with (TARGET / "quran_surahs.txt").open(
        "w", encoding="utf-8", newline="\n"
    ) as handle:
        for chapter in chapters:
            handle.write(
                f"{chapter['id']}|{chapter['name_arabic']}|"
                f"{chapter['name_simple']}|{chapter['verses_count']}\n"
            )

    print(f"{len(verses)} versiculos, {len(chapters)} suras")


if __name__ == "__main__":
    main()
