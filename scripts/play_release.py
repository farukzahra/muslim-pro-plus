"""Publica o AAB no Google Play via service account.

Uso:
  python scripts/play_release.py internal              # sobe o AAB
  python scripts/play_release.py production 1          # usa o versionCode 1 ja enviado

Faz: cria edit, (sobe o bundle ou reusa um versionCode), atualiza a track com
status completed e commita.
"""

import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

PACKAGE = "com.farukzahra.muslimplus"
KEY = Path(r"C:\repo\secrets\google-play\muslim-plus-service-account.json")
AAB = (
    Path(__file__).resolve().parents[1]
    / "app/build/outputs/bundle/release/app-release.aab"
)
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]


def main() -> int:
    track = sys.argv[1] if len(sys.argv) > 1 else "internal"
    existing = sys.argv[2] if len(sys.argv) > 2 else None
    status = sys.argv[3] if len(sys.argv) > 3 else "completed"

    if not KEY.exists():
        print(f"service account ausente: {KEY}")
        return 1
    if not AAB.exists():
        print(f"AAB ausente: {AAB}")
        return 1

    creds = service_account.Credentials.from_service_account_file(
        str(KEY), scopes=SCOPES
    )
    service = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)

    try:
        edit = service.edits().insert(packageName=PACKAGE, body={}).execute()
        edit_id = edit["id"]
        print(f"edit {edit_id}")

        if existing:
            version_code = int(existing)
            print(f"usando bundle ja enviado: versionCode {version_code}")
        else:
            bundle = (
                service.edits()
                .bundles()
                .upload(
                    packageName=PACKAGE,
                    editId=edit_id,
                    media_body=str(AAB),
                    media_mime_type="application/octet-stream",
                )
                .execute()
            )
            version_code = bundle["versionCode"]
            print(
                f"bundle enviado: versionCode {version_code} ({AAB.stat().st_size} bytes)"
            )

        service.edits().tracks().update(
            packageName=PACKAGE,
            editId=edit_id,
            track=track,
            body={
                "track": track,
                "releases": [
                    {
                        "versionCodes": [str(version_code)],
                        "status": status,
                    }
                ],
            },
        ).execute()
        print(f"track {track} atualizada")

        result = service.edits().commit(packageName=PACKAGE, editId=edit_id).execute()
        print(f"commit ok: {result.get('id')}")
        return 0
    except HttpError as error:
        print(
            f"HTTP {error.resp.status}: {error.content.decode('utf-8', 'replace')[:800]}"
        )
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
