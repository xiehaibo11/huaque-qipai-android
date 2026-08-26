package com.nanbeiyule.game;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Native AAC/M4A recorder for the hold-to-talk button.
 *
 * <p>The 30s ceiling is原版: Voice/Module.lua:72 sets MAX_RECORD_TM = 30 and stops recording once
 * the per-second schedule reaches it. The minimum duration and byte ceiling are NOT原版 — the
 * original module has no lower bound and hands the clip to the platform SDK without a size check.
 * Both are南北娱乐 protection values for our own upload endpoint and must stay in sync with the
 * backend's RoomToolsService.validateVoice bounds.
 */
final class TaizhouVoiceRecorder {
    /** 南北娱乐 protection value; the original has no minimum recording length. */
    static final int MIN_DURATION_MILLIS = 400;

    /** 原版 Voice/Module.lua:72 MAX_RECORD_TM = 30 seconds. */
    static final int MAX_DURATION_MILLIS = 30_000;

    /** 南北娱乐 protection value; not an original constraint. */
    static final int MAX_BYTES = 512 * 1024;

    interface Listener {
        void onMaximumDuration(Recording recording);

        void onRecorderError(String message);
    }

    record Recording(byte[] data, int durationMillis) {}

    private final Context context;
    private final Listener listener;
    private MediaRecorder recorder;
    private File output;
    private long startedAt;
    private boolean maximumHandled;

    TaizhouVoiceRecorder(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    boolean start() {
        cancel();
        try {
            output = File.createTempFile("taizhou-voice-", ".m4a", context.getCacheDir());
            recorder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? new MediaRecorder(context)
                    : new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.setAudioSamplingRate(16_000);
            recorder.setAudioEncodingBitRate(64_000);
            recorder.setMaxDuration(MAX_DURATION_MILLIS);
            recorder.setMaxFileSize(MAX_BYTES);
            recorder.setOutputFile(output.getAbsolutePath());
            recorder.setOnInfoListener(
                    (ignored, what, extra) -> {
                        if (maximumHandled) return;
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                                || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                            maximumHandled = true;
                            Recording recording = stop();
                            if (recording != null) listener.onMaximumDuration(recording);
                        }
                    });
            recorder.setOnErrorListener(
                    (ignored, what, extra) -> {
                        cancel();
                        listener.onRecorderError("录音失败，请重新按住语音按钮");
                    });
            recorder.prepare();
            recorder.start();
            startedAt = SystemClock.elapsedRealtime();
            maximumHandled = false;
            return true;
        } catch (IOException | RuntimeException exception) {
            cancel();
            return false;
        }
    }

    Recording stop() {
        MediaRecorder active = recorder;
        File file = output;
        int duration = (int) Math.min(
                MAX_DURATION_MILLIS,
                Math.max(0L, SystemClock.elapsedRealtime() - startedAt));
        recorder = null;
        output = null;
        if (active == null || file == null) return null;
        try {
            if (duration < MIN_DURATION_MILLIS) {
                release(active);
                delete(file);
                return null;
            }
            active.stop();
            release(active);
            byte[] data = read(file);
            delete(file);
            if (data.length == 0 || data.length > MAX_BYTES) return null;
            return new Recording(data, duration);
        } catch (IOException | RuntimeException exception) {
            release(active);
            delete(file);
            return null;
        }
    }

    void cancel() {
        MediaRecorder active = recorder;
        File file = output;
        recorder = null;
        output = null;
        if (active != null) {
            try {
                active.stop();
            } catch (RuntimeException ignored) {
                // MediaRecorder throws when canceled before a complete audio frame.
            }
            release(active);
        }
        delete(file);
    }

    boolean isRecording() {
        return recorder != null;
    }

    private static void release(MediaRecorder value) {
        try {
            value.reset();
        } catch (RuntimeException ignored) {
            // Release is still safe after a recorder state error.
        }
        value.release();
    }

    private static void delete(File value) {
        if (value != null && value.exists()) {
            //noinspection ResultOfMethodCallIgnored
            value.delete();
        }
    }

    private static byte[] read(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }
}
