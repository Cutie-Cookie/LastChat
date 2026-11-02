import os
from PIL import Image, ImageDraw, ImageOps

SRC = ".github/branding/source.jpg"
RES = "app/src/main/res"

im = Image.open(SRC).convert("RGB")
# average corner colors -> background fill
w, h = im.size
corners = [im.getpixel(p) for p in [(4, 4), (w - 5, 4), (4, h - 5), (w - 5, h - 5)]]
bg = tuple(sum(c[i] for c in corners) // 4 for i in range(3))

def cover(size):
    return ImageOps.fit(im, (size, size), Image.LANCZOS)

def with_bg(size, frac):
    canvas = Image.new("RGB", (size, size), bg)
    inner = int(size * frac)
    canvas.paste(cover(inner), ((size - inner) // 2, (size - inner) // 2))
    return canvas

def round_mask(img):
    m = Image.new("L", img.size, 0)
    d = ImageDraw.Draw(m)
    d.ellipse((0, 0, img.size[0] - 1, img.size[1] - 1), fill=255)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.paste(img, (0, 0), m)
    return out

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
for dpi, s in SIZES.items():
    d = os.path.join(RES, "mipmap-" + dpi)
    os.makedirs(d, exist_ok=True)
    full = cover(s)
    full.save(os.path.join(d, "ic_launcher_lastchat.png"))
    round_mask(full).save(os.path.join(d, "ic_launcher_lastchat_round.png"))
    fg = with_bg({"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}[dpi], 0.66)
    fg.save(os.path.join(d, "ic_launcher_lastchat_foreground.png"))

with_bg(512, 1.0).save(os.path.join(RES, "playstore-icon.png"))
with_bg(512, 1.0).save(os.path.join(RES, "ic_launcher-web.png"))
print("icons generated, bg =", bg)
