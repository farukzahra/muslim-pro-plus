"""Gera o catalogo de cidades a partir do GeoNames cities5000.

Baixa (se faltar em build/cities) e gera:
  app/src/main/assets/cities.txt -> "id|name|asciiname|countryCode|lat|lon|zone|population"

Regras:
  - population >= 50000
  - ordenado por populacao decrescente (desempate por nome, para o arquivo
    ficar deterministico entre execucoes)
  - no maximo 3000 linhas, para o APK nao estourar com o catalogo completo
  - linhas sem fuso IANA valido sao descartadas: o app calcula horario de
    oracao pelo fuso da cidade, entao sem fuso a linha nao serve

Formato de origem (TSV do GeoNames, uma linha por cidade):
  0 geonameid | 1 name | 2 asciiname | 3 alternatenames | 4 lat | 5 lon |
  6 feature class | 7 feature code | 8 country code | 9 cc2 | 10 admin1 |
  11 admin2 | 12 admin3 | 13 admin4 | 14 population | 15 elevation |
  16 dem | 17 timezone | 18 modification date

Atribuicao exigida pela licenca do GeoNames (CC BY 4.0): a tela de Licencas
(F5) precisa citar "GeoNames, https://www.geonames.org/".

Uso: python tools/cities/build_cities.py
"""

import pathlib
import urllib.request
import zipfile

BASE = pathlib.Path(__file__).resolve().parents[2]
SOURCE = BASE / "build" / "cities"
TARGET = BASE / "app" / "src" / "main" / "assets"
URL = "https://download.geonames.org/export/dump/cities5000.zip"
ARCHIVE = "cities5000.zip"
MEMBER = "cities5000.txt"

MIN_POPULATION = 50_000
MAX_CITIES = 3_000


def download(attempts: int = 3) -> pathlib.Path:
    """Baixa o zip em blocos; a conexao cai no meio com frequencia."""
    SOURCE.mkdir(parents=True, exist_ok=True)
    path = SOURCE / ARCHIVE
    if path.exists() and path.stat().st_size > 0:
        return path
    partial = path.with_suffix(".part")
    for attempt in range(1, attempts + 1):
        try:
            request = urllib.request.Request(
                URL,
                headers={"User-Agent": "MuslimPlus/0.1 (data build)"},
            )
            with urllib.request.urlopen(request, timeout=300) as response:
                expected = int(response.headers.get("Content-Length") or 0)
                written = 0
                with partial.open("wb") as handle:
                    while chunk := response.read(1 << 16):
                        handle.write(chunk)
                        written += len(chunk)
            if expected and written != expected:
                raise OSError(f"{written} de {expected} bytes")
            partial.replace(path)
            return path
        except OSError as error:
            partial.unlink(missing_ok=True)
            if attempt == attempts:
                raise SystemExit(f"falha ao baixar {URL}: {error}") from error
    raise SystemExit(f"falha ao baixar {URL}")


def clean(text: str) -> str:
    """O separador do asset e' '|', entao ele nao pode aparecer nos campos."""
    return " ".join(text.replace("|", " ").split())


def rows(archive: pathlib.Path) -> list[dict]:
    parsed = []
    with zipfile.ZipFile(archive) as bundle, bundle.open(MEMBER) as handle:
        for raw in handle:
            columns = raw.decode("utf-8").rstrip("\n").split("\t")
            if len(columns) < 18:
                continue
            population = int(columns[14] or 0)
            zone = columns[17].strip()
            name = clean(columns[1])
            ascii_name = clean(columns[2]) or name
            country = columns[8].strip()
            if population < MIN_POPULATION or not zone or not name or not country:
                continue
            parsed.append(
                {
                    "id": columns[0].strip(),
                    "name": name,
                    "ascii": ascii_name,
                    "country": country,
                    "lat": columns[4].strip(),
                    "lon": columns[5].strip(),
                    "zone": zone,
                    "population": population,
                }
            )
    parsed.sort(key=lambda city: (-city["population"], city["ascii"], city["id"]))
    return parsed[:MAX_CITIES]


def main() -> None:
    cities = rows(download())
    if len(cities) < MAX_CITIES:
        raise SystemExit(f"catalogo incompleto: {len(cities)} cidades")
    TARGET.mkdir(parents=True, exist_ok=True)
    with (TARGET / "cities.txt").open("w", encoding="utf-8", newline="\n") as handle:
        for city in cities:
            handle.write(
                f"{city['id']}|{city['name']}|{city['ascii']}|{city['country']}|"
                f"{city['lat']}|{city['lon']}|{city['zone']}|{city['population']}\n"
            )
    print(
        f"{len(cities)} cidades, menor populacao {cities[-1]['population']}, "
        f"zonas {len({city['zone'] for city in cities})}"
    )


if __name__ == "__main__":
    main()
