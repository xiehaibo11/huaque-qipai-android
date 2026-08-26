#target photoshop
app.displayDialogs = DialogModes.NO;

var psdPath = "/Users/mosc/Downloads/逆向/UI-skill install/花雀棋牌/棋牌游戏平台-花雀2.psd";
var projectRoot = "/Users/mosc/Downloads/逆向/android";
var outputDir = projectRoot + "/app/src/main/res/drawable-nodpi";

function fail(message) {
    throw new Error(message);
}

function containsName(names, name) {
    for (var i = 0; i < names.length; i++) {
        if (names[i] === name) {
            return true;
        }
    }
    return false;
}

function findLayer(container, name) {
    for (var i = 0; i < container.layers.length; i++) {
        var layer = container.layers[i];
        if (layer.name === name) {
            return layer;
        }
        if (layer.typename === "LayerSet") {
            var nested = findLayer(layer, name);
            if (nested) {
                return nested;
            }
        }
    }
    return null;
}

function setOnlyTargetsVisible(container, names) {
    var found = false;
    for (var i = 0; i < container.layers.length; i++) {
        var layer = container.layers[i];
        if (layer.typename === "LayerSet") {
            var childFound = setOnlyTargetsVisible(layer, names);
            layer.visible = childFound;
            found = found || childFound;
        } else {
            var visible = containsName(names, layer.name);
            layer.visible = visible;
            found = found || visible;
        }
    }
    return found;
}

function commonLayerSet(first, second) {
    var parent = first.parent;
    while (parent && parent.typename === "LayerSet") {
        var candidate = second.parent;
        while (candidate && candidate.typename === "LayerSet") {
            if (candidate === parent) {
                return parent;
            }
            candidate = candidate.parent;
        }
        parent = parent.parent;
    }
    return null;
}

function containsLayerSet(container, target) {
    if (container === target) {
        return true;
    }
    if (!container.layers) {
        return false;
    }
    for (var i = 0; i < container.layers.length; i++) {
        var layer = container.layers[i];
        if (layer.typename === "LayerSet" && containsLayerSet(layer, target)) {
            return true;
        }
    }
    return false;
}

function showOnlyLayerSetBranch(container, target) {
    for (var i = 0; i < container.layers.length; i++) {
        var layer = container.layers[i];
        if (layer === target) {
            layer.visible = true;
        } else if (layer.typename === "LayerSet" && containsLayerSet(layer, target)) {
            layer.visible = true;
            showOnlyLayerSetBranch(layer, target);
        } else {
            layer.visible = false;
        }
    }
}

function savePng(document, path) {
    var options = new PNGSaveOptions();
    options.compression = 9;
    options.interlaced = false;
    document.saveAs(new File(path), options, true, Extension.LOWERCASE);
}

function cropToBounds(document, x, y, width, height) {
    document.crop([
        UnitValue(x, "px"),
        UnitValue(y, "px"),
        UnitValue(x + width, "px"),
        UnitValue(y + height, "px")
    ]);
}

function exportTargets(source, names, path, x, y, width, height) {
    var document = source.duplicate();
    if (!setOnlyTargetsVisible(document, names)) {
        fail("missing target: " + names.join(","));
    }
    cropToBounds(document, x, y, width, height);
    savePng(document, path);
    document.close(SaveOptions.DONOTSAVECHANGES);
}

function exportCardPatch(
        source,
        foregroundName,
        titleName,
        hiddenNames,
        path,
        x,
        y,
        width,
        height) {
    var document = source.duplicate();
    var foreground = findLayer(document, foregroundName);
    var title = findLayer(document, titleName);
    if (!foreground || !title) {
        fail("missing card layer: " + foregroundName + "/" + titleName);
    }
    var cardSet = commonLayerSet(foreground, title);
    if (!cardSet) {
        fail("no common card group: " + foregroundName + "/" + titleName);
    }
    showOnlyLayerSetBranch(document, cardSet);
    for (var i = 0; i < hiddenNames.length; i++) {
        var hidden = findLayer(document, hiddenNames[i]);
        if (!hidden) {
            fail("missing hidden layer: " + hiddenNames[i]);
        }
        hidden.visible = false;
    }
    cropToBounds(document, x, y, width, height);
    savePng(document, path);
    document.close(SaveOptions.DONOTSAVECHANGES);
}

var source = app.open(new File(psdPath));
var tempDir = Folder.temp.fsName + "/huaque-lobby-motion";
new Folder(tempDir).create();

exportTargets(
        source,
        ["红中", "发财"],
        outputDir + "/lobby_icon_taizhou.png",
        1188,
        302,
        295,
        379);
exportTargets(
        source,
        ["韩国花牌"],
        outputDir + "/lobby_icon_wahua.png",
        1597,
        239,
        312,
        214);
exportTargets(
        source,
        ["红十元素-2"],
        outputDir + "/lobby_icon_shisanshui.png",
        1555,
        532,
        356,
        223);

var taizhouPatch = tempDir + "/taizhou-static.png";
var wahuaPatch = tempDir + "/wahua-static.png";
var shisanshuiPatch = tempDir + "/shisanshui-static.png";
exportCardPatch(
        source,
        "红中",
        "台州麻将",
        ["红中", "发财"],
        taizhouPatch,
        1159,
        235,
        330,
        613);
exportCardPatch(
        source,
        "韩国花牌",
        "浙江挖花",
        ["韩国花牌"],
        wahuaPatch,
        1530,
        235,
        393,
        243);
exportCardPatch(
        source,
        "红十元素-2",
        "浙江十三水",
        ["红十元素", "红十元素-2"],
        shisanshuiPatch,
        1530,
        506,
        393,
        343);
source.close(SaveOptions.DONOTSAVECHANGES);
