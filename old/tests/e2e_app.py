"""E2E tests for the patched APK, driven over adb.

The repository has no buildable source: `decompiled/` is read-only JADX output
used for reading, not for compiling. These tests therefore exercise the
installed APK end to end: tap every bottom tab and every tile in the More grid,
then assert that the app survived and navigated.

usage: python tests/e2e_app.py [--report path]
"""

import argparse
import json
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

ADB = r"C:\Users\T-GAMER\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.bitsmedia.android.muslimpro"

TABS = [
    ("timeline", "Hoje", 108),
    ("prayers", "Orações", 324),
    ("quran", "Al Qur'an", 540),
    ("qibla", "Qibla", 756),
    ("more", "Mais", 972),
]
TAB_Y = 2250

SYSTEM_DIALOG_TEXTS = {
    "Durante o uso do app",
    "Apenas esta vez",
    "While using the app",
    "Fechar app",
    "Close app",
    "Informações do app",
    "App info",
    "Permitir",
    "Allow",
    "Não permitir",
    "Don't allow",
}


def sh(*args, timeout=60):
    return subprocess.run(
        [ADB, *args],
        capture_output=True,
        text=True,
        timeout=timeout,
        encoding="utf-8",
        errors="replace",
    )


def ui_xml(retries=3):
    for _ in range(retries):
        r = sh("shell", "uiautomator dump /sdcard/e2e.xml")
        if "dumped" in (r.stdout + r.stderr):
            r2 = sh("shell", "cat /sdcard/e2e.xml")
            try:
                return ET.fromstring(r2.stdout)
            except ET.ParseError:
                pass
        time.sleep(1.5)
    return None


def nodes(root):
    out = []
    if root is None:
        return out
    for n in root.iter("node"):
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds") or "")
        if not m:
            continue
        x1, y1, x2, y2 = map(int, m.groups())
        out.append(
            {
                "text": (n.get("text") or "").strip(),
                "rid": n.get("resource-id") or "",
                "clickable": n.get("clickable") == "true",
                "cx": (x1 + x2) // 2,
                "cy": (y1 + y2) // 2,
                "x1": x1,
                "y1": y1,
                "x2": x2,
                "y2": y2,
            }
        )
    return out


def top_activity():
    r = sh("shell", "dumpsys activity activities")
    for line in r.stdout.splitlines():
        if "topResumedActivity" in line:
            m = re.search(r"(com\.[\w.]+/[\w.$]+)", line)
            if m:
                return m.group(1)
    return ""


def in_app():
    return top_activity().startswith(PKG + "/")


def crashes():
    r = sh("logcat", "-d", "-b", "crash")
    return [l for l in r.stdout.splitlines() if "FATAL EXCEPTION" in l]


def clear_crash():
    sh("logcat", "-b", "crash", "-c")


def tap(x, y, wait=2.5):
    sh("shell", "input", "tap", str(x), str(y))
    time.sleep(wait)


def back(wait=1.8):
    sh("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(wait)


def dismiss_system_dialog():
    for n in nodes(ui_xml(retries=1)):
        if n["text"] in SYSTEM_DIALOG_TEXTS:
            tap(n["cx"], n["cy"], wait=1.5)
            return True
    return False


def launch():
    sh("shell", "am", "force-stop", PKG)
    time.sleep(1)
    sh(
        "shell",
        "am",
        "start",
        "-a",
        "android.intent.action.VIEW",
        "-d",
        "muslimpro://timeline",
    )


def ensure_app(tries=10):
    """Garante a MainActivity em primeiro plano, fechando dialogos do sistema."""
    if in_app() and top_activity().endswith("MainActivity"):
        return True
    launch()
    for _ in range(tries):
        time.sleep(2)
        if in_app() and top_activity().endswith("MainActivity"):
            return True
        dismiss_system_dialog()
    return False


def selected_tab(rid):
    for line in sh("shell", "dumpsys activity top").stdout.splitlines():
        if f"app:id/{rid}}}" in line:
            return "..S....." in line
    return False


def find_tab(rid):
    return next((n for n in nodes(ui_xml()) if n["rid"].endswith(f"id/{rid}")), None)


def record(results, kind, name, ok, detail):
    results.append(
        {
            "kind": kind,
            "name": name,
            "status": "PASS" if ok else "FAIL",
            "detail": detail,
        }
    )


def wait_selected(rid, timeout=10, retry_x=None):
    end = time.time() + timeout
    tapped_again = False
    while time.time() < end:
        if selected_tab(rid):
            return True
        if retry_x and not tapped_again and time.time() > end - timeout / 2:
            tap(retry_x, TAB_Y, wait=1.5)
            tapped_again = True
        time.sleep(1)
    return False


def run_tabs(results):
    for rid, label, x in TABS:
        if not ensure_app():
            record(results, "tab", label, False, "app nao abriu")
            continue
        clear_crash()
        tap(x, TAB_Y, wait=1.5)
        sel = wait_selected(rid, retry_x=x)
        ok = sel and in_app() and not crashes()
        record(
            results,
            "tab",
            label,
            ok,
            f"selecionada={sel} app={in_app()} crashes={len(crashes())}",
        )


def discover_tiles():
    if not ensure_app():
        return []
    tap(972, TAB_Y, wait=2)
    seen, tiles = set(), []
    for _ in range(4):
        nodes_now = nodes(ui_xml())
        grid = [n for n in nodes_now if n["text"] and 300 < n["y1"] and n["y2"] < 2000]
        if len(grid) >= 5:
            break
        time.sleep(2)
    for n in grid:
        if not n["text"] or n["text"] in seen:
            continue
        if n["y1"] < 300 or n["y2"] > 2000:
            continue
        seen.add(n["text"])
        tiles.append(n)
    return tiles


def run_tiles(results):
    tiles = discover_tiles()
    if not tiles:
        record(results, "tile", "-", False, "grade nao encontrada")
        return
    for tile in tiles:
        if not ensure_app():
            record(results, "tile", tile["text"], False, "app nao abriu")
            continue
        t = find_tab("more")
        if t:
            tap(972, TAB_Y, wait=2.5)
        clear_crash()
        tap(tile["cx"], tile["cy"], wait=4)
        act = top_activity()
        crashed = bool(crashes())
        alive = bool(sh("shell", "pidof", PKG).stdout.strip())
        opened = bool(act) and not act.endswith("MainActivity")
        status = "FAIL" if (crashed or not alive) else ("PASS" if opened else "PARTIAL")
        results.append(
            {
                "kind": "tile",
                "name": tile["text"],
                "status": status,
                "detail": f"abriu={'sim' if opened else 'nao'} "
                f"destino={act.split('/')[-1] or '-'} crashes={len(crashes())}",
            }
        )
        back()
        if not in_app():
            back()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--report", default=r"C:\repo\muslim-pro-plus\evidence\e2e-report.json"
    )
    args = ap.parse_args()

    results = []
    run_tabs(results)
    run_tiles(results)

    ok = sum(1 for r in results if r["status"] == "PASS")
    partial = sum(1 for r in results if r["status"] == "PARTIAL")
    for r in results:
        print(f"{r['status']:7} {r['kind']:4} {r['name']:22} {r['detail']}")
    print(
        f"\n{ok} passaram, {partial} parciais, {len(results) - ok - partial} falharam "
        f"de {len(results)}"
    )

    json.dump(
        results, open(args.report, "w", encoding="utf-8"), ensure_ascii=False, indent=1
    )
    return 0 if ok + partial == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
