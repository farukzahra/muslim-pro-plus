"""Print source context around matches, or around a line number.

usage: ctx.py <file> <pattern> [before] [after]
       ctx.py <file> :<line> [before] [after]
"""

import re
import sys

path = sys.argv[1]
arg = sys.argv[2]
before = int(sys.argv[3]) if len(sys.argv) > 3 else 8
after = int(sys.argv[4]) if len(sys.argv) > 4 else 8

lines = open(path, encoding="utf-8", errors="replace").read().split("\n")

if arg.startswith(":"):
    start = max(0, int(arg[1:]) - before - 1)
    end = min(len(lines), int(arg[1:]) + after)
    for i in range(start, end):
        print(f"{i + 1}: {lines[i]}")
    raise SystemExit

rx = re.compile(arg)
for i, line in enumerate(lines):
    if rx.search(line):
        start = max(0, i - before)
        end = min(len(lines), i + after + 1)
        print(f"--- linha {i + 1} ---")
        for j in range(start, end):
            print(f"{j + 1}: {lines[j]}")
