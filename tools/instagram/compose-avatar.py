from pathlib import Path
from PIL import Image, ImageDraw
import math

S = 1080
SS = 4
W = S * SS
OUT = Path(r"C:\repo\muslim-pro-plus\docs\instagram")

BLACK = (0, 0, 0)
GREEN = (47, 185, 138)
GOLD = (217, 178, 95)

img = Image.new("RGB", (W, W), BLACK)
d = ImageDraw.Draw(img)

cx, cy, r = 486 * SS, 566 * SS, 322 * SS
d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=GREEN)

# círculo preto deslocado para a direita/cima: forma o crescente
bx, by, br = 566 * SS, 522 * SS, 300 * SS
d.ellipse((bx - br, by - br, bx + br, by + br), fill=BLACK)

# estrela dourada de cinco pontas
sx, sy, outer = 648 * SS, 470 * SS, 76 * SS
inner = outer * 0.382
points = []
for i in range(10):
    ang = -math.pi / 2 + i * math.pi / 5
    rad = outer if i % 2 == 0 else inner
    points.append((sx + rad * math.cos(ang), sy + rad * math.sin(ang)))
d.polygon(points, fill=GOLD)

avatar = img.resize((S, S), Image.LANCZOS)
avatar.save(OUT / "avatar.png")
avatar.save(OUT / "avatar.jpg", quality=95, optimize=True)
print("ok", avatar.size, (OUT / "avatar.jpg").stat().st_size)
