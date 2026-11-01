"""Atualiza as notas de versao de uma trilha do Google Play.

Le o arquivo `docs/releases/<versao>.md`, extrai os blocos de codigo das
secoes "Google Play" em pt-BR, en e ar, e grava nas releases da trilha.

Uso:
    python scripts/play_notes.py 1.3.0 internal
    python scripts/play_notes.py 1.3.0 production
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

ROOT = Path(__file__).resolve().parents[1]
PACKAGE = "com.farukzahra.muslimplus"
KEY = Path(r"C:\repo\secrets\google-play\muslim-plus-service-account.json")
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]


def notes_for(version: str) -> dict[str, str]:
    path = ROOT / "docs" / "releases" / f"{version}.md"
    if not path.exists():
        raise SystemExit(f"release ausente: {path}")
    text = path.read_text(encoding="utf-8")
    notes: dict[str, str] = {}
    for heading, body in re.findall(
        r"^## (.+?)\n(.*?)(?=^## |\Z)", text, re.MULTILINE | re.DOTALL
    ):
        if "Google Play" not in heading:
            continue
        code = re.search(r"```\n(.*?)```", body, re.DOTALL)
        if not code:
            continue
        if "(pt-BR)" in heading:
            locale = "pt-BR"
        elif "(en)" in heading:
            locale = "en-US"
        elif "(ar)" in heading:
            locale = "ar"
        else:
            continue
        notes[locale] = code.group(1).strip()
    missing = {"pt-BR", "en-US", "ar"} - set(notes)
    if missing:
        raise SystemExit(f"faltam notas em: {', '.join(sorted(missing))}")
    for locale, note in notes.items():
        if len(note) > 500:
            raise SystemExit(f"nota {locale} tem {len(note)} caracteres (max 500)")
    return notes


def main() -> int:
    if len(sys.argv) < 3:
        print("uso: python scripts/play_notes.py <versao> <trilha>")
        return 2
    version, track = sys.argv[1], sys.argv[2]
    notes = notes_for(version)

    creds = service_account.Credentials.from_service_account_file(
        str(KEY), scopes=SCOPES
    )
    svc = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)

    try:
        edit = svc.edits().insert(packageName=PACKAGE, body={}).execute()
        edit_id = edit["id"]
        print(f"edit {edit_id}")

        tracks = (
            svc.edits().tracks().list(packageName=PACKAGE, editId=edit_id).execute()
        )
        target = next(
            (item for item in tracks.get("tracks", []) if item["track"] == track),
            None,
        )
        if target is None:
            print(f"trilha {track} nao encontrada")
            return 1
        releases = target.get("releases") or []
        if not releases:
            print(f"trilha {track} sem release")
            return 1

        for release in releases:
            release["releaseNotes"] = [
                {"language": locale, "text": text} for locale, text in notes.items()
            ]
        svc.edits().tracks().update(
            packageName=PACKAGE,
            editId=edit_id,
            track=track,
            body=target,
        ).execute()
        print(
            f"trilha {track}: notas em {', '.join(sorted(notes))} "
            f"(versionCodes {', '.join(releases[0].get('versionCodes', []))})"
        )
        result = svc.edits().commit(packageName=PACKAGE, editId=edit_id).execute()
        print(f"commit ok: {result.get('id')}")
        return 0
    except HttpError as error:
        print(
            f"HTTP {error.resp.status}: {error.content.decode('utf-8', 'replace')[:800]}"
        )
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
