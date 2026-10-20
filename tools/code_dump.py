import struct
import sys

path, off = sys.argv[1], int(sys.argv[2], 0)
d = open(path, "rb").read()
regs, ins, outs, tries, dbg, insns = struct.unpack_from("<HHHHII", d, off)
print(f"regs={regs} ins={ins} outs={outs} tries={tries} dbg={dbg:#x} insns={insns}")
words = struct.unpack_from(f"<{insns}H", d, off + 16)
print("primeiras:", [hex(w) for w in words[:6]])
