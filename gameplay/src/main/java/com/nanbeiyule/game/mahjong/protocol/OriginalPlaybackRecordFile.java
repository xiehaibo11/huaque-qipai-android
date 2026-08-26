package com.nanbeiyule.game.mahjong.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Reader for Zhejiang's recovered playback file framing.
 *
 * <p>Evidence source:
 * {@code game/GameBase/Modules/PlayBack/Module.lua} records each {@code Record}
 * frame as 20 ASCII digits timestamp + 6 ASCII digits XY_ID + 6 ASCII digits
 * payload length + raw payload. {@code lobby/Req/PlayerBill/ReqShareGame.lua}
 * and {@code ReqGoldPlayBack.lua} prepend a 20 digit room id plus one
 * timestamp-less fill frame, then split that fill frame into {@code AllDirection}.
 */
public final class OriginalPlaybackRecordFile {
    private static final int TIMESTAMP_WIDTH = 20;
    private static final int XY_ID_WIDTH = 6;
    private static final int LENGTH_WIDTH = 6;
    private static final int RECORD_HEADER_WIDTH = TIMESTAMP_WIDTH + XY_ID_WIDTH + LENGTH_WIDTH;
    private static final int ALL_DIRECTION_HEADER_WIDTH = XY_ID_WIDTH + LENGTH_WIDTH;

    private OriginalPlaybackRecordFile() {}

    public record RecordFrame(long timestamp, int xyId, byte[] payload) {
        public RecordFrame {
            payload = Objects.requireNonNull(payload, "payload").clone();
        }

        @Override
        public byte[] payload() {
            return payload.clone();
        }
    }

    public record AllDirectionFrame(int xyId, byte[] payload) {
        public AllDirectionFrame {
            payload = Objects.requireNonNull(payload, "payload").clone();
        }

        @Override
        public byte[] payload() {
            return payload.clone();
        }
    }

    public record ShareGameArchive(long roomId, AllDirectionFrame fillFrame, byte[] recordBytes) {
        public ShareGameArchive {
            Objects.requireNonNull(fillFrame, "fillFrame");
            recordBytes = Objects.requireNonNull(recordBytes, "recordBytes").clone();
        }

        @Override
        public byte[] recordBytes() {
            return recordBytes.clone();
        }
    }

    /** Parses a {@code *_Record.bin} payload after the room-id/fill frame was removed. */
    public static List<RecordFrame> readRecord(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes, "bytes");
        List<RecordFrame> frames = new ArrayList<>();
        int cursor = 0;
        while (cursor < bytes.length) {
            requireAvailable(bytes, cursor, RECORD_HEADER_WIDTH, "record frame header");
            long timestamp = parseAsciiLong(bytes, cursor, TIMESTAMP_WIDTH, "timestamp");
            cursor += TIMESTAMP_WIDTH;
            int xyId = parseAsciiInt(bytes, cursor, XY_ID_WIDTH, "xyId");
            cursor += XY_ID_WIDTH;
            int length = parseAsciiInt(bytes, cursor, LENGTH_WIDTH, "payload length");
            cursor += LENGTH_WIDTH;
            requireAvailable(bytes, cursor, length, "record payload");
            byte[] payload = Arrays.copyOfRange(bytes, cursor, cursor + length);
            cursor += length;
            frames.add(new RecordFrame(timestamp, xyId, payload));
        }
        return List.copyOf(frames);
    }

    /** Parses the timestamp-less {@code AllDirection} fill frame. */
    public static AllDirectionFrame readAllDirection(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes, "bytes");
        requireAvailable(bytes, 0, ALL_DIRECTION_HEADER_WIDTH, "AllDirection header");
        int xyId = parseAsciiInt(bytes, 0, XY_ID_WIDTH, "xyId");
        int length = parseAsciiInt(bytes, XY_ID_WIDTH, LENGTH_WIDTH, "payload length");
        int expected = ALL_DIRECTION_HEADER_WIDTH + length;
        if (bytes.length != expected) {
            throw new IOException(
                    "AllDirection payload length "
                            + length
                            + " leaves "
                            + (bytes.length - expected)
                            + " trailing bytes");
        }
        byte[] payload = Arrays.copyOfRange(bytes, ALL_DIRECTION_HEADER_WIDTH, expected);
        return new AllDirectionFrame(xyId, payload);
    }

    /**
     * Parses the merged playback file before the original client rewrites it
     * into separate {@code Record} and {@code AllDirection} files.
     */
    public static ShareGameArchive splitShareGameArchive(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes, "bytes");
        requireAvailable(bytes, 0, TIMESTAMP_WIDTH + ALL_DIRECTION_HEADER_WIDTH,
                "share-game archive header");
        long roomId = parseAsciiLong(bytes, 0, TIMESTAMP_WIDTH, "roomId");
        int cursor = TIMESTAMP_WIDTH;
        int xyId = parseAsciiInt(bytes, cursor, XY_ID_WIDTH, "xyId");
        cursor += XY_ID_WIDTH;
        int length = parseAsciiInt(bytes, cursor, LENGTH_WIDTH, "payload length");
        cursor += LENGTH_WIDTH;
        requireAvailable(bytes, cursor, length, "share-game fill payload");
        byte[] fillPayload = Arrays.copyOfRange(bytes, cursor, cursor + length);
        cursor += length;
        byte[] recordBytes = Arrays.copyOfRange(bytes, cursor, bytes.length);
        return new ShareGameArchive(roomId, new AllDirectionFrame(xyId, fillPayload), recordBytes);
    }

    private static void requireAvailable(byte[] bytes, int cursor, int length, String label)
            throws IOException {
        if (length < 0 || cursor < 0 || cursor + length > bytes.length) {
            throw new IOException(
                    "Truncated " + label + " at byte " + cursor + ", need " + length
                            + ", total " + bytes.length);
        }
    }

    private static int parseAsciiInt(byte[] bytes, int offset, int width, String label)
            throws IOException {
        long value = parseAsciiLong(bytes, offset, width, label);
        if (value > Integer.MAX_VALUE) {
            throw new IOException(label + " exceeds int range: " + value);
        }
        return (int) value;
    }

    private static long parseAsciiLong(byte[] bytes, int offset, int width, String label)
            throws IOException {
        long value = 0L;
        for (int i = 0; i < width; i++) {
            byte b = bytes[offset + i];
            if (b < '0' || b > '9') {
                throw new IOException(label + " contains non-digit at byte " + (offset + i));
            }
            value = value * 10L + (b - '0');
        }
        return value;
    }
}
