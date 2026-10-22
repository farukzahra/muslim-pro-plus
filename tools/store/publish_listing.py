"""Publica a ficha da loja e as notas de versao no Google Play via API.

A API do Play cobre textos, imagens, testadores e trilhas. O que ela **nao**
cobre (conteudo do app, classificacao, seguranca de dados) continua sendo
feito no Console.

Uso:
    python tools/store/publish_listing.py             # mostra o que faria
    python tools/store/publish_listing.py --apply     # grava no Play
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "docs" / "store"
PACKAGE = "com.farukzahra.muslimplus"
KEY = Path(r"C:\repo\secrets\google-play\muslim-plus-service-account.json")
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]

CONTACT_EMAIL = "farukz@gmail.com"
CONTACT_WEBSITE = "https://muslimplus.faruk.dev.br/"

# locale do Play -> arquivos que alimentam a ficha
LISTINGS = {
    "en-US": {
        "text": "listing-en.md",
        "feature": "feature-graphic-en.png",
        "screenshots": [
            "screenshots/en/01-today.png",
            "screenshots/en/02-prayers.png",
            "screenshots/en/03-quran-reader.png",
            "screenshots/en/04-qibla.png",
        ],
    },
    "pt-BR": {
        "text": "listing-pt.md",
        "feature": "feature-graphic-pt.png",
        "screenshots": [
            "screenshots/pt/01-hoje.png",
            "screenshots/pt/02-oracoes.png",
            "screenshots/pt/03-leitor-alcorao.png",
            "screenshots/pt/04-qibla.png",
        ],
    },
}

ICON = "icon-512.png"

HEADINGS = {
    "titulo": "Título",
    "curta": "Descrição curta",
    "completa": "Descrição completa",
    "novidades": "O que há de novo",
}


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


def field(fields: dict[str, str], key: str) -> str:
    heading = HEADINGS[key]
    match = next((k for k in fields if k.startswith(heading)), None)
    if match is None:
        raise SystemExit(f"campo '{heading}' ausente")
    return fields[match]


def service():
    creds = service_account.Credentials.from_service_account_file(
        str(KEY), scopes=SCOPES
    )
    return build("androidpublisher", "v3", credentials=creds, cache_discovery=False)


def upload(svc, edit_id: str, language: str, image_type: str, path: Path) -> None:
    media = MediaFileUpload(str(path), mimetype="image/png", resumable=False)
    svc.edits().images().upload(
        packageName=PACKAGE,
        editId=edit_id,
        language=language,
        imageType=image_type,
        media_body=media,
    ).execute()
    print(f"  {image_type:18s} {path.name} ({path.stat().st_size} bytes)")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--apply", action="store_true", help="grava no Play (sem isso, so mostra)"
    )
    parser.add_argument("--skip-images", action="store_true")
    parser.add_argument("--skip-release-notes", action="store_true")
    args = parser.parse_args()

    if not KEY.exists():
        print(f"service account ausente: {KEY}")
        return 1

    svc = service()
    edit = svc.edits().insert(packageName=PACKAGE, body={}).execute()
    edit_id = edit["id"]
    print(f"edit {edit_id}{'' if args.apply else ' (ensaio)'}")

    try:
        notes: dict[str, str] = {}
        for language, spec in LISTINGS.items():
            fields = parse_listing(STORE / spec["text"])
            title = field(fields, "titulo")
            short = field(fields, "curta")
            full = field(fields, "completa")
            notes[language] = field(fields, "novidades")
            print(
                f"\n{language}: {title!r} ({len(title)}) / {len(short)} / {len(full)}"
            )
            if args.apply:
                svc.edits().listings().update(
                    packageName=PACKAGE,
                    editId=edit_id,
                    language=language,
                    body={
                        "language": language,
                        "title": title,
                        "shortDescription": short,
                        "fullDescription": full,
                    },
                ).execute()
                print("  texto gravado")
                if not args.skip_images:
                    svc.edits().images().deleteall(
                        packageName=PACKAGE,
                        editId=edit_id,
                        language=language,
                        imageType="phoneScreenshots",
                    ).execute()
                    upload(svc, edit_id, language, "icon", STORE / ICON)
                    upload(
                        svc,
                        edit_id,
                        language,
                        "featureGraphic",
                        STORE / spec["feature"],
                    )
                    for shot in spec["screenshots"]:
                        upload(svc, edit_id, language, "phoneScreenshots", STORE / shot)

        if args.apply and not args.skip_release_notes:
            tracks = (
                svc.edits().tracks().list(packageName=PACKAGE, editId=edit_id).execute()
            )
            for track in tracks.get("tracks", []):
                releases = track.get("releases") or []
                if not releases:
                    continue
                for release in releases:
                    release["releaseNotes"] = [
                        {"language": language, "text": text}
                        for language, text in notes.items()
                    ]
                svc.edits().tracks().update(
                    packageName=PACKAGE,
                    editId=edit_id,
                    track=track["track"],
                    body=track,
                ).execute()
                print(f"\ntrilha {track['track']}: notas em {', '.join(notes)}")

        if args.apply:
            svc.edits().details().update(
                packageName=PACKAGE,
                editId=edit_id,
                body={
                    "defaultLanguage": "en-US",
                    "contactEmail": CONTACT_EMAIL,
                    "contactWebsite": CONTACT_WEBSITE,
                },
            ).execute()
            print(f"contato: {CONTACT_EMAIL} / {CONTACT_WEBSITE}")

            result = svc.edits().commit(packageName=PACKAGE, editId=edit_id).execute()
            print(f"commit ok: {result.get('id')}")
        else:
            svc.edits().delete(packageName=PACKAGE, editId=edit_id).execute()
            print("\nensaio: edit descartado; use --apply para gravar")
        return 0
    except HttpError as error:
        print(
            f"HTTP {error.resp.status}: {error.content.decode('utf-8', 'replace')[:900]}"
        )
        try:
            svc.edits().delete(packageName=PACKAGE, editId=edit_id).execute()
        except HttpError:
            pass
        return 1


if __name__ == "__main__":
    sys.exit(main())
