#!/usr/bin/env python3

import argparse
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont


TEXT = "\u5357\u5317\u5a31\u4e50"


def offset_mask(mask, dx, dy):
    shifted = Image.new("L", mask.size, 0)
    shifted.paste(mask, (dx, dy))
    return shifted


def color_layer(mask, color, opacity=255):
    layer = Image.new("RGBA", mask.size, color + (0,))
    if opacity == 255:
        layer.putalpha(mask)
    else:
        layer.putalpha(mask.point(lambda value: value * opacity // 255))
    return layer


def render(font_path, output_path):
    canvas_size = (1400, 460)
    font = ImageFont.truetype(str(font_path), 250)
    text_box = font.getbbox(TEXT)
    text_width = text_box[2] - text_box[0]
    text_height = text_box[3] - text_box[1]
    origin = (
        round((canvas_size[0] - text_width) / 2 - text_box[0]),
        round((canvas_size[1] - text_height) / 2 - text_box[1] - 12),
    )

    glyph_mask = Image.new("L", canvas_size, 0)
    ImageDraw.Draw(glyph_mask).text(origin, TEXT, font=font, fill=255)

    image = Image.new("RGBA", canvas_size, (0, 0, 0, 0))

    shadow = offset_mask(glyph_mask.filter(ImageFilter.MaxFilter(39)), 7, 32)
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    image.alpha_composite(color_layer(shadow, (50, 15, 8), 150))

    dark_outline = glyph_mask.filter(ImageFilter.MaxFilter(37))
    extrusion = Image.new("L", canvas_size, 0)
    for depth in range(9, 33, 2):
        extrusion = ImageChops.lighter(extrusion, offset_mask(dark_outline, depth // 5, depth))
    image.alpha_composite(color_layer(extrusion, (91, 32, 14)))

    image.alpha_composite(color_layer(dark_outline, (83, 28, 12)))
    gold_outline = glyph_mask.filter(ImageFilter.MaxFilter(17))
    image.alpha_composite(color_layer(gold_outline, (247, 174, 52)))

    gradient = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    gradient_pixels = gradient.load()
    top = max(0, origin[1] + text_box[1])
    bottom = min(canvas_size[1] - 1, origin[1] + text_box[3])
    span = max(1, bottom - top)
    for y in range(top, bottom + 1):
        position = (y - top) / span
        if position < 0.45:
            local = position / 0.45
            start = (255, 248, 173)
            end = (255, 199, 77)
        else:
            local = (position - 0.45) / 0.55
            start = (255, 199, 77)
            end = (213, 119, 20)
        color = tuple(round(start[index] + (end[index] - start[index]) * local) for index in range(3))
        for x in range(canvas_size[0]):
            gradient_pixels[x, y] = color + (255,)
    gradient.putalpha(glyph_mask)
    image.alpha_composite(gradient)

    top_edge = ImageChops.subtract(glyph_mask, offset_mask(glyph_mask, 0, 5))
    top_edge = top_edge.filter(ImageFilter.GaussianBlur(1.2))
    image.alpha_composite(color_layer(top_edge, (255, 255, 223), 220))

    bottom_edge = ImageChops.subtract(glyph_mask, offset_mask(glyph_mask, 0, -4))
    bottom_edge = bottom_edge.filter(ImageFilter.GaussianBlur(1.2))
    image.alpha_composite(color_layer(bottom_edge, (145, 63, 9), 150))

    shine = Image.new("L", canvas_size, 0)
    ImageDraw.Draw(shine).polygon(
        [(250, 70), (430, 70), (1050, 350), (850, 350)],
        fill=115,
    )
    shine = shine.filter(ImageFilter.GaussianBlur(12))
    shine = ImageChops.multiply(shine, glyph_mask)
    image.alpha_composite(color_layer(shine, (255, 255, 239), 145))

    alpha_box = image.getchannel("A").getbbox()
    if alpha_box is None:
        raise RuntimeError("Rendered title is empty")
    padding = 12
    crop_box = (
        max(0, alpha_box[0] - padding),
        max(0, alpha_box[1] - padding),
        min(canvas_size[0], alpha_box[2] + padding),
        min(canvas_size[1], alpha_box[3] + padding),
    )
    image = image.crop(crop_box)
    image.thumbnail((1100, 320), Image.Resampling.LANCZOS)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    image.save(output_path, "PNG", optimize=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--font", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    render(args.font, args.output)


if __name__ == "__main__":
    main()
