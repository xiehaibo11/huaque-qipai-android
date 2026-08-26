package com.nanbeiyule.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Owns one table's permission, recorder, overlay, upload, download and playback lifecycle. */
final class TaizhouVoiceFlow {
    private final MainActivityGameHomeDisplayFlow owner;
    private FrameLayout container;
    private TaizhouRoomToolsCoordinator coordinator;
    private TaizhouVoiceRecorder recorder;
    private TaizhouVoicePlayer player;
    private TaizhouVoiceOverlayView overlay;
    private final ArrayDeque<String> playbackQueue = new ArrayDeque<>();
    private final Set<String> queuedMessageIds = new HashSet<>();
    private boolean playbackActive;

    TaizhouVoiceFlow(MainActivityGameHomeDisplayFlow owner) {
        this.owner = owner;
    }

    void open(FrameLayout container, TaizhouRoomToolsCoordinator coordinator) {
        close();
        this.container = container;
        this.coordinator = coordinator;
        player = new TaizhouVoicePlayer(owner);
        PersonalCenterSystemSettings settings = owner.personalCenterSystemSettings;
        player.setVolume(
                settings != null && settings.voiceEnabled()
                        ? settings.voiceVolume() / 100f
                        : 0f);
        recorder =
                new TaizhouVoiceRecorder(
                        owner,
                        new TaizhouVoiceRecorder.Listener() {
                            @Override
                            public void onMaximumDuration(
                                    TaizhouVoiceRecorder.Recording recording) {
                                removeOverlay();
                                send(recording);
                            }

                            @Override
                            public void onRecorderError(String message) {
                                removeOverlay();
                                toast(message);
                            }
                        });
    }

    void onGesture(TaizhouMahjongVoiceGesture.Result result) {
        if (result == null) return;
        switch (result.action()) {
            case START -> start();
            case UPDATE -> {
                if (overlay != null) overlay.setCancelPending(result.cancelPending());
            }
            case SEND -> finish(true);
            case CANCEL -> finish(false);
            case NONE -> {
                // No active recording gesture.
            }
        }
    }

    void onPermissionResult(boolean granted) {
        toast(granted ? "录音权限已开启，请再次按住语音按钮" : "需要录音权限才能发送语音");
    }

    void play(String messageId) {
        if (coordinator == null || player == null || messageId == null || messageId.isBlank()) return;
        if (!queuedMessageIds.add(messageId)) return;
        playbackQueue.addLast(messageId);
        playNext();
    }

    private void playNext() {
        if (playbackActive || coordinator == null || player == null || playbackQueue.isEmpty()) {
            return;
        }
        String messageId = playbackQueue.getFirst();
        playbackActive = true;
        coordinator.loadVoice(
                messageId,
                new TaizhouRoomToolsCoordinator.VoiceCallback() {
                    @Override
                    public void onVoice(byte[] data) {
                        player.play(
                                data,
                                new TaizhouVoicePlayer.Callback() {
                                    @Override public void onComplete() { finishPlayback(null); }
                                    @Override public void onError(String message) {
                                        finishPlayback(message);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        finishPlayback(message);
                    }
                });
    }

    void close() {
        removeOverlay();
        if (recorder != null) {
            recorder.cancel();
            recorder = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        playbackQueue.clear();
        queuedMessageIds.clear();
        playbackActive = false;
        container = null;
        coordinator = null;
    }

    private void start() {
        if (container == null || recorder == null || coordinator == null) return;
        PersonalCenterSystemSettings settings = owner.personalCenterSystemSettings;
        if (settings != null && !settings.voiceEnabled()) {
            toast("请先在系统设置中开启语音");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && owner.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            owner.requestPermissions(
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    MainActivityState.REQUEST_RECORD_AUDIO);
            return;
        }
        if (!recorder.start()) {
            toast("录音启动失败，请检查麦克风");
            return;
        }
        removeOverlay();
        overlay = new TaizhouVoiceOverlayView(owner);
        container.addView(
                overlay,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void finish(boolean send) {
        if (recorder == null || !recorder.isRecording()) {
            removeOverlay();
            return;
        }
        if (!send) {
            recorder.cancel();
            removeOverlay();
            return;
        }
        TaizhouVoiceRecorder.Recording recording = recorder.stop();
        removeOverlay();
        if (recording == null) {
            toast("录音时间太短，请按住说话");
            return;
        }
        send(recording);
    }

    private void send(TaizhouVoiceRecorder.Recording recording) {
        if (coordinator != null) {
            coordinator.sendVoice(recording.durationMillis(), recording.data());
        }
    }

    private void removeOverlay() {
        if (overlay != null) {
            if (overlay.getParent() instanceof ViewGroup parent) {
                parent.removeView(overlay);
            }
            overlay = null;
        }
    }

    private void finishPlayback(String error) {
        String completed = playbackQueue.pollFirst();
        if (completed != null) queuedMessageIds.remove(completed);
        playbackActive = false;
        if (error != null) toast(error);
        playNext();
    }

    private void toast(String message) {
        if (!owner.isFinishing()) Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
    }
}
