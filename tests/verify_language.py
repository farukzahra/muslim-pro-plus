"""Verifica o seletor de idioma no APK instalado, via adb.

Cobre o criterio do M0: trocar para arabe deixa a UI em arabe (RTL) e voltar
para "padrao do sistema" restaura o idioma do aparelho. Em RTL a barra
espelha, entao a aba e encontrada pelo rotulo, nunca por coordenada fixa.

usage: python tests/verify_language.py
"""

import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

ADB = r"C:\Users\T-GAMER\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.farukzahra.muslimplus"

AR_TABS = {"اليوم", "الصلاة", "القرآن", "القبلة", "المزيد"}
PT_TABS = {"Hoje", "Orações", "Alcorão", "Qibla", "Mais"}


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
    out = []
    if root is None:
        return out
    for n in root.iter("node"):
        t = (n.get("text") or "").strip()
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds") or "")
        if t and m:
            x1, y1, x2, y2 = map(int, m.groups())
            out.append((t, (x1 + x2) // 2, (y1 + y2) // 2))
    return out


def tap_text(label, wait=2.0, tries=6):
    for _ in range(tries):
        for t, x, y in texts(dump()):
            if t == label:
                sh("shell", "input", "tap", str(x), str(y))
                time.sleep(wait)
                return True
        time.sleep(1.5)
    return False


def back():
    sh("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(2.5)


def visible_tabs():
    return {t for t, _, _ in texts(dump())} & (AR_TABS | PT_TABS)


def main():
    sh("shell", "pm", "clear", PKG)
    time.sleep(1)
    sh("shell", "am", "force-stop", PKG)
    time.sleep(1)
    sh("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(5)

    if not tap_text("Mais"):
        print("FAIL aba Mais nao encontrada")
        return 1
    if not tap_text("Idioma"):
        print("FAIL item Idioma nao encontrado")
        return 1
    if not tap_text("العربية"):
        print("FAIL opcao arabe nao encontrada")
        return 1
    time.sleep(3)
    back()

    arabic = visible_tabs()
    ok_ar = AR_TABS.issubset(arabic)
    print(f"arabe: {sorted(arabic)} -> {'PASS' if ok_ar else 'FAIL'}")

    if not tap_text("المزيد"):
        print("FAIL aba المزيد nao encontrada")
        return 1
    if not tap_text("اللغة"):
        print("FAIL item اللغة nao encontrado")
        return 1
    if not tap_text("لغة النظام الافتراضية"):
        print("FAIL opcao sistema nao encontrada")
        return 1
    time.sleep(3)
    back()

    portuguese = visible_tabs()
    ok_pt = bool(portuguese & PT_TABS)
    print(f"sistema: {sorted(portuguese)} -> {'PASS' if ok_pt else 'FAIL'}")

    return 0 if ok_ar and ok_pt else 1


if __name__ == "__main__":
    sys.exit(main())
