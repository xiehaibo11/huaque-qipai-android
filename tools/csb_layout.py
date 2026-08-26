#!/usr/bin/env python3
"""Dump CocosStudio 2.1.0.0 .csb (flatbuffer) node trees as layout evidence.

Field offsets were recovered empirically from the original Zhejiang lobby 1.5.4
resources; see WIDGET_FIELDS below.
"""
import argparse
import json
import struct
from pathlib import Path

# WidgetOptions vtable slots, confirmed against TrustLayer.csb:
#   _KW_TRUST_TIP_BG   visible=false  position=(-20,20)   size=(746,300)
#   _KW_TEXT_TIME_STR  text="已托管10秒" position=(373,223.26) size=(339,80)
WIDGET_NAME = 4
WIDGET_ACTION_TAG = 6
WIDGET_ROTATION_SKEW_X = 8
WIDGET_ROTATION_SKEW_Y = 10
WIDGET_VISIBLE = 12
WIDGET_ALPHA = 14
WIDGET_TAG = 16
WIDGET_POSITION = 18
WIDGET_SCALE = 20
WIDGET_ANCHOR = 22
WIDGET_COLOR = 24
WIDGET_SIZE = 26

# Per-widget option tables all keep WidgetOptions at slot 4.
OPTION_WIDGET = 4
# ResourceData { path:4, resourceType:6, plistFile:8 }
RESOURCE_PATH = 4
RESOURCE_PLIST = 8
# ImageViewOptions { widgetOptions:4, fileNameData:6, capInsets:8, scale9Size:10,
#                    scale9Enabled:12 }
IMAGE_CAP_INSETS = 8
IMAGE_SCALE9_SIZE = 10
IMAGE_SCALE9_ENABLED = 12
# TextOptions { widgetOptions:4, fontResource:6, fontName:8, fontSize:10, text:12,
#               ... textColor:34 }
TEXT_FONT_SIZE = 10
TEXT_COLOR = 34


class Buf:
    def __init__(self, data):
        self.d = data

    def u8(self, o):
        return self.d[o]

    def u16(self, o):
        return struct.unpack_from("<H", self.d, o)[0]

    def i32(self, o):
        return struct.unpack_from("<i", self.d, o)[0]

    def u32(self, o):
        return struct.unpack_from("<I", self.d, o)[0]

    def f32(self, o):
        return struct.unpack_from("<f", self.d, o)[0]

    def vec2(self, o):
        return list(struct.unpack_from("<ff", self.d, o))

    def indirect(self, o):
        return o + self.i32(o)

    def string(self, o):
        p = self.indirect(o)
        n = self.u32(p)
        return self.d[p + 4:p + 4 + n].decode("utf-8", "replace")

    def fields(self, tbl):
        vt = tbl - self.i32(tbl)
        vlen = self.u16(vt)
        out = {}
        for i in range((vlen - 4) // 2):
            off = self.u16(vt + 4 + i * 2)
            if off:
                out[4 + i * 2] = tbl + off
        return out

    def vector(self, p):
        q = self.indirect(p)
        n = self.u32(q)
        return [q + 4 + i * 4 for i in range(n)]


def read_widget(b, tbl):
    f = b.fields(tbl)
    node = {}
    if WIDGET_NAME in f:
        node["name"] = b.string(f[WIDGET_NAME])
    node["visible"] = b.u8(f[WIDGET_VISIBLE]) != 0 if WIDGET_VISIBLE in f else True
    node["alpha"] = b.u8(f[WIDGET_ALPHA]) if WIDGET_ALPHA in f else 255
    node["position"] = b.vec2(f[WIDGET_POSITION]) if WIDGET_POSITION in f else [0.0, 0.0]
    node["scale"] = b.vec2(f[WIDGET_SCALE]) if WIDGET_SCALE in f else [1.0, 1.0]
    node["anchor"] = b.vec2(f[WIDGET_ANCHOR]) if WIDGET_ANCHOR in f else [0.0, 0.0]
    node["size"] = b.vec2(f[WIDGET_SIZE]) if WIDGET_SIZE in f else [0.0, 0.0]
    if WIDGET_ROTATION_SKEW_X in f:
        node["rotation"] = round(b.f32(f[WIDGET_ROTATION_SKEW_X]), 4)
    return node


def read_resource(b, p):
    f = b.fields(b.indirect(p))
    out = {}
    if RESOURCE_PATH in f:
        out["path"] = b.string(f[RESOURCE_PATH])
    if RESOURCE_PLIST in f:
        try:
            plist = b.string(f[RESOURCE_PLIST])
            if plist:
                out["plist"] = plist
        except Exception:
            pass
    return out


def looks_like_string(b, p):
    try:
        q = b.indirect(p)
        if not 0 < q < len(b.d) - 4:
            return False
        n = b.u32(q)
        if not 0 < n < 200 or q + 4 + n >= len(b.d):
            return False
        if b.d[q + 4 + n] != 0:
            return False
        b.d[q + 4:q + 4 + n].decode("utf-8")
        return True
    except Exception:
        return False


def read_options(b, tbl, classname):
    f = b.fields(tbl)
    node = {}
    if OPTION_WIDGET in f:
        try:
            node.update(read_widget(b, b.indirect(f[OPTION_WIDGET])))
        except Exception:
            pass
    if not node.get("name") and OPTION_WIDGET in f and looks_like_string(b, f[OPTION_WIDGET]):
        # Root Node stores WidgetOptions inline instead of nesting it.
        node.update(read_widget(b, tbl))
    resources = []
    texts = []
    for slot, p in sorted(f.items()):
        if slot == OPTION_WIDGET:
            continue
        try:
            sub = b.fields(b.indirect(p))
        except Exception:
            sub = None
        if sub and RESOURCE_PATH in sub and looks_like_string(b, sub[RESOURCE_PATH]):
            res = read_resource(b, p)
            if res.get("path"):
                resources.append(res)
            continue
        if looks_like_string(b, p):
            value = b.string(p)
            if (value
                    and value.isprintable()
                    and not value.endswith((".png", ".plist", ".TTF", ".ttf"))):
                texts.append(value)
    if classname == "ImageView" and IMAGE_SCALE9_ENABLED in f and b.u8(f[IMAGE_SCALE9_ENABLED]):
        node["scale9"] = True
        if IMAGE_CAP_INSETS in f:
            node["capInsets"] = [
                round(v, 3) for v in struct.unpack_from("<ffff", b.d, f[IMAGE_CAP_INSETS])]
    if classname == "Text":
        if TEXT_FONT_SIZE in f:
            node["fontSize"] = b.i32(f[TEXT_FONT_SIZE])
        if TEXT_COLOR in f:
            p = f[TEXT_COLOR]
            node["textColor"] = [b.u8(p), b.u8(p + 1), b.u8(p + 2), b.u8(p + 3)]
    if resources:
        node["resources"] = resources
    if texts:
        node["texts"] = texts
    node["class"] = classname
    return node


def walk(b, tbl):
    f = b.fields(tbl)
    classname = b.string(f[4]) if 4 in f else "?"
    node = {"class": classname}
    if 8 in f:
        opt = b.fields(b.indirect(f[8]))
        if OPTION_WIDGET in opt:
            node.update(read_options(b, b.indirect(opt[OPTION_WIDGET]), classname))
    children = []
    if 6 in f:
        for cp in b.vector(f[6]):
            children.append(walk(b, b.indirect(cp)))
    if children:
        node["children"] = children
    return node


def parse(path):
    b = Buf(Path(path).read_bytes())
    root = b.fields(b.indirect(0))
    return {"version": b.string(root[4]), "root": walk(b, b.indirect(root[10]))}


def render(node, depth=0, out=None):
    out = out if out is not None else []
    px, py = node.get("position", [0, 0])
    w, h = node.get("size", [0, 0])
    ax, ay = node.get("anchor", [0, 0])
    line = f"{'  ' * depth}{node.get('name', '(anon)')} <{node['class']}>"
    line += f" pos=({px:g},{py:g}) size=({w:g},{h:g}) anchor=({ax:g},{ay:g})"
    if not node.get("visible", True):
        line += " HIDDEN"
    if node.get("alpha", 255) != 255:
        line += f" alpha={node['alpha']}"
    if node.get("scale", [1, 1]) != [1.0, 1.0]:
        line += f" scale=({node['scale'][0]:g},{node['scale'][1]:g})"
    if node.get("fontSize"):
        line += f" font={node['fontSize']}"
    if node.get("textColor"):
        a, r, g, bb = node["textColor"]
        line += f" color=#{a:02X}{r:02X}{g:02X}{bb:02X}"
    if node.get("scale9"):
        ci = node.get("capInsets")
        line += " scale9" + (f"={ci}" if ci else "")
    for res in node.get("resources", []):
        line += f" [{res['path']}]"
    for text in node.get("texts", []):
        line += f" text={text!r}"
    out.append(line)
    for child in node.get("children", []):
        render(child, depth + 1, out)
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("csb", type=Path, nargs="+")
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()
    for path in args.csb:
        tree = parse(path)
        if args.json:
            print(json.dumps({"file": str(path), **tree}, ensure_ascii=False, indent=2))
        else:
            print(f"===== {path} (CocosStudio {tree['version']}) =====")
            print("\n".join(render(tree["root"])))
            print()


if __name__ == "__main__":
    main()
