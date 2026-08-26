package com.nanbeiyule.game.wulong;

import java.io.IOException;
import java.io.InputStream;

/** Direct 30588 BaseCardGame component atlas, not the older Common/Image Card_%d atlas. */
final class WuLongCardFrames {
    private final WuLongPlistFrameResolver frames;
    private WuLongCardFrames(WuLongPlistFrameResolver frames) { this.frames = frames; }
    static WuLongCardFrames load(InputStream input) throws IOException {
        return new WuLongCardFrames(WuLongPlistFrameResolver.load(input));
    }
    WuLongPlistFrameResolver resolver() { return frames; }
}
