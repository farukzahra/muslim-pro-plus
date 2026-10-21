"""E2E do APK instalado, via adb: cinco abas, troca de idioma e modo aviao.

Roda sobre o app ja instalado no emulador/aparelho, sem desinstalar nem limpar
dados. Cobre o que o teste Compose nao alcanca: o app de verdade, aberto pelo
launcher, com o aparelho sem rede.

Passos:
  1. abre o app (pulando o onboarding se for a primeira execucao);
  2. toca nas cinco abas e confere um marcador de cada tela;
  3. troca para arabe, confere as abas em arabe e volta para o padrao;
  4. liga o modo aviao, reinicia o app e confere que Hoje ainda mostra os
     horarios e que o Alcorao abre offline;
  5. desliga o modo aviao no fim, mesmo se algum passo falhar.

usage: python tests/e2e_app.py
"""

import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = "com.farukzahra.muslimplus"
ACTIVITY = f"{PKG}/.MainActivity"

# Rotulos das abas nos tres idiomas do app.
TODAY = {"Hoje", "Today", "اليوم"}
PRAYERS = {"Orações", "Prayers", "الصلاة"}
QURAN = {"Alcorão", "Quran", "القرآن"}
QIBLA = {"Qibla", "القبلة"}
MORE = {"Mais", "More", "المزيد"}
ALL_TABS = TODAY | PRAYERS | QURAN | QIBLA | MORE

SKIP = {"Pular", "Skip", "تخطّي"}
ALLOW = {"Permitir", "Allow", "السماح"}

# Marcadores de tela, um por aba.
TODAY_MARK = {"Fajr", "الفجر"}
PRAYERS_MARK = {"Ajustes", "Adjust", "التعديلات"}
MORE_MARK = {"Configurações", "Settings", "الإعدادات"}
READER_TITLE = "Al-Fatihah"

TIME_RE = re.compile(r"^\d{1,2}:\d{2}$")
VERSE_REF_RE = re.compile(r"^[0-9٠-٩]{1,3}:[0-9٠-٩]{1,3}$")
DEGREE_RE = re.compile(r"^\d{1,3}°$")


def adb_binary():
    candidates = [
        os.environ.get("ADB"),
        os.path.join(
            os.environ.get("ANDROID_HOME", ""),
            "platform-tools",
            "adb.exe" if os.name == "nt" else "adb",
        ),
        os.path.join(
            os.environ.get("LOCALAPPDATA", ""),
            "Android",
            "Sdk",
            "platform-tools",
            "adb.exe",
        ),
        r"C:\Users\T-GAMER\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    ]
    for candidate in candidates:
        if candidate and os.path.exists(candidate):
            return candidate
    return "adb"


ADB = adb_binary()


def sh(*args, timeout=90):
    return subprocess.run(
        [ADB, *args],
        capture_output=True,
        text=True,
        timeout=timeout,
        encoding="utf-8",
        errors="replace",
    )


def dump(retries=4):
    for _ in range(retries):
        r = sh("shell", "uiautomator dump /sdcard/v.xml")
        if "dumped" in (r.stdout + r.stderr):
            xml = sh("shell", "cat /sdcard/v.xml").stdout
            try:
                return ET.fromstring(xml)
            except ET.ParseError:
                pass
        time.sleep(1.5)
    return None


def texts(root):
    """Pares (texto, x, y) dos nos visiveis, para tocar pelo rotulo."""
    out = []
    if root is None:
        return out
    for node in root.iter("node"):
        text = (node.get("text") or "").strip()
        match = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds") or "")
        if text and match:
            x1, y1, x2, y2 = map(int, match.groups())
            out.append((text, (x1 + x2) // 2, (y1 + y2) // 2))
    return out


def visible_texts():
    return {text for text, _, _ in texts(dump())}


def tap_text(labels, wait=2.0, tries=6):
    """Toca no primeiro rotulo encontrado, aceitando sinonimos de idioma."""
    if isinstance(labels, str):
        labels = {labels}
    for _ in range(tries):
        for text, x, y in texts(dump()):
            if text in labels:
                sh("shell", "input", "tap", str(x), str(y))
                time.sleep(wait)
                return text
        time.sleep(1.5)
    return None


def tap_match(pattern, wait=2.0, tries=6):
    for _ in range(tries):
        for text, x, y in texts(dump()):
            if pattern.match(text):
                sh("shell", "input", "tap", str(x), str(y))
                time.sleep(wait)
                return text
        time.sleep(1.5)
    return None


def back(wait=2.0):
    sh("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(wait)


def wait_for(condition, tries=8, pause=1.5):
    for _ in range(tries):
        value = condition()
        if value:
            return value
        time.sleep(pause)
    return None


def start_app():
    sh("shell", "am", "force-stop", PKG)
    time.sleep(1)
    sh("shell", "am", "start", "-n", ACTIVITY)
    time.sleep(5)


def foreground_package():
    """Pacote em primeiro plano; evita confundir o app com outro de abas iguais."""
    out = sh("shell", "dumpsys", "activity", "activities").stdout
    match = re.search(r"topResumedActivity=ActivityRecord\{\S+ u\d+ ([\w.]+)/", out)
    return match.group(1) if match else None


def installed():
    out = sh("shell", "pm", "list", "packages", PKG).stdout
    return f"package:{PKG}" in out


def ensure_home():
    """Abre o app, sai do onboarding se aparecer e para na tela com as abas."""
    # Sem o dialogo de permissao de notificacao o onboarding fica deterministico.
    sh("shell", "pm", "grant", PKG, "android.permission.POST_NOTIFICATIONS")
    for _ in range(8):
        if foreground_package() != PKG:
            start_app()
            continue
        labels = visible_texts()
        if labels & ALL_TABS:
            return True
        if labels & SKIP:
            tap_text(SKIP, tries=1)
            continue
        if labels & ALLOW:
            tap_text(ALLOW, tries=1)
            continue
        # Onboarding rolavel em tela pequena: sobe e tenta de novo.
        sh("shell", "input", "swipe", "540", "1800", "540", "700", "300")
        time.sleep(1.5)
        if visible_texts() & SKIP:
            tap_text(SKIP, tries=1)
            continue
        start_app()
    return foreground_package() == PKG and bool(visible_texts() & ALL_TABS)


def device_tabs():
    """Abas no idioma do aparelho, para conferir a volta do "padrao do sistema"."""
    r = sh("shell", "settings", "get", "system", "system_locales")
    tag = (r.stdout or "").strip().lower()
    if tag.startswith("ar"):
        return {"اليوم", "الصلاة", "القرآن", "القبلة", "المزيد"}
    if tag.startswith("pt"):
        return {"Hoje", "Orações", "Alcorão", "Qibla", "Mais"}
    return {"Today", "Prayers", "Quran", "Qibla", "More"}


def check_tabs():
    results = []
    for name, tab, marker, allow_degree in (
        ("Hoje", TODAY, TODAY_MARK, False),
        ("Orações", PRAYERS, PRAYERS_MARK, False),
        ("Alcorão", QURAN, {READER_TITLE}, False),
        ("Qibla", QIBLA, DEGREE_RE, True),
        ("Mais", MORE, MORE_MARK, False),
    ):
        if not tap_text(tab):
            results.append((name, False, "aba nao encontrada"))
            continue
        found = wait_for(
            lambda: bool(
                visible_texts() & marker
                if not allow_degree
                else {t for t, _, _ in texts(dump()) if DEGREE_RE.match(t)}
            ),
        )
        results.append((name, bool(found), "sem marcador" if not found else ""))
    return results


def switch_language_to_arabic():
    if not tap_text(TODAY | PRAYERS | QURAN | QIBLA | MORE):
        return False
    if not tap_text(MORE):
        return False
    if not tap_text({"Idioma", "Language", "اللغة"}):
        return False
    if not tap_text({"العربية"}):
        return False
    time.sleep(3)
    back()
    return True


def switch_language_to_system():
    if not tap_text(MORE):
        return False
    if not tap_text({"اللغة"}):
        return False
    if not tap_text({"لغة النظام الافتراضية"}):
        return False
    time.sleep(3)
    back()
    return True


def airplane(mode):
    sh("shell", "cmd", "connectivity", "airplane-mode", mode)
    time.sleep(2)
    state = sh("shell", "cmd", "connectivity", "airplane-mode").stdout.strip()
    return state == f"{mode}d"


def main():
    failures = []

    def report(name, ok, detail=""):
        suffix = "" if ok else f" ({detail})"
        print(f"{'PASS' if ok else 'FAIL'} {name}{suffix}")
        if not ok:
            failures.append(name)

    try:
        if not installed():
            report("app instalado", False, f"{PKG} nao esta instalado")
            return 1

        start_app()
        report("app aberto na tela inicial", ensure_home())
        if foreground_package() != PKG:
            report("cinco abas na barra", False, "o app nao abriu em primeiro plano")
            return 1

        tabs = visible_texts() & ALL_TABS
        report("cinco abas na barra", len(tabs) == 5, f"visiveis: {sorted(tabs)}")

        for name, ok, detail in check_tabs():
            report(f"aba {name}", ok, detail)

        report("idioma: troca para arabe", switch_language_to_arabic())
        arabic = visible_texts()
        expected_ar = {"اليوم", "الصلاة", "القرآن", "القبلة", "المزيد"}
        report(
            "idioma: abas em arabe",
            expected_ar.issubset(arabic),
            f"visiveis: {sorted(arabic & expected_ar)}",
        )

        report("idioma: volta para o padrao do sistema", switch_language_to_system())
        expected_home = device_tabs()
        report(
            "idioma: abas no idioma do aparelho",
            bool(visible_texts() & expected_home),
            f"esperado um de {sorted(expected_home)}",
        )

        report("modo aviao ligado", airplane("enable"))
        start_app()
        report("offline: app abre", ensure_home())

        report(
            "offline: aba Hoje mostra horarios",
            bool(tap_text(TODAY))
            and bool(
                wait_for(lambda: any(TIME_RE.match(t) for t, _, _ in texts(dump())))
            ),
        )

        report(
            "offline: Alcorao abre",
            bool(tap_text(QURAN))
            and bool(wait_for(lambda: READER_TITLE in visible_texts()))
            and bool(tap_text({READER_TITLE}))
            and bool(
                wait_for(
                    lambda: any(VERSE_REF_RE.match(t) for t, _, _ in texts(dump()))
                )
            ),
        )
    finally:
        report("modo aviao desligado", airplane("disable"))
        start_app()

    print()
    if failures:
        print(f"FALHOU: {len(failures)} passo(s): {', '.join(failures)}")
        return 1
    print("TUDO VERDE")
    return 0


if __name__ == "__main__":
    sys.exit(main())
