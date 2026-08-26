package com.nanbeiyule.game.mahjong.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Java port of Zhejiang's recovered {@code GameBase.Modules.PlayBack.Module}. */
public final class OriginalPlaybackModule {
    private static final int SERVER_TO_CLIENT_MESSAGE_XY_ID = 11014;

    public interface FrameSink {
        void onMessage(int xyId, byte[] payload);
    }

    public interface ProcessListener {
        void onProcessChanged(int playedCount, int allCount);
    }

    private final FrameSink frameSink;
    private final ProcessListener processListener;
    private List<OriginalPlaybackRecordFile.RecordFrame> gameMsgList = List.of();
    private final List<OriginalPlaybackRecordFile.RecordFrame> playMsgList = new ArrayList<>();
    private double speed = 1.0;
    private boolean pause;
    private double lastSysTime;
    private double allPlayBackTime;
    private long justRecordGameMsgFirstTime;
    private boolean playback;

    public OriginalPlaybackModule(FrameSink frameSink, ProcessListener processListener) {
        this.frameSink = Objects.requireNonNull(frameSink, "frameSink");
        this.processListener = processListener;
    }

    public void start(List<OriginalPlaybackRecordFile.RecordFrame> frames) {
        gameMsgList = List.copyOf(Objects.requireNonNull(frames, "frames"));
        playMsgList.clear();
        playMsgList.addAll(gameMsgList);
        lastSysTime = 0.0;
        allPlayBackTime = 0.0;
        justRecordGameMsgFirstTime = 0L;
        pause = false;
        playback = true;
    }

    public void stopPlayback() {
        playback = false;
        gameMsgList = List.of();
        playMsgList.clear();
        lastSysTime = 0.0;
        allPlayBackTime = 0.0;
        justRecordGameMsgFirstTime = 0L;
    }

    public boolean isPlayback() {
        return playback;
    }

    public void pause() {
        pause = true;
    }

    public void play() {
        pause = false;
    }

    public boolean isPaused() {
        return pause;
    }

    public void fast(double multiple) {
        speed = multiple;
        dispatchProcessChanged();
    }

    public void slow(double multiple) {
        speed = multiple;
        dispatchProcessChanged();
    }

    public double playbackSpeed() {
        return speed;
    }

    /** Equivalent to one scheduled {@code analysisGameMsg()} tick. */
    public void advance(double now) {
        if (!playback) {
            return;
        }
        if (playMsgList.isEmpty()) {
            stopPlayback();
            return;
        }
        dropDuplicateLeadingServerForward();
        if (playMsgList.isEmpty()) {
            stopPlayback();
            return;
        }
        if (pause) {
            lastSysTime = now;
            return;
        }
        if (lastSysTime > 0.0) {
            allPlayBackTime += (now - lastSysTime) * speed;
        }
        lastSysTime = now;
        OriginalPlaybackRecordFile.RecordFrame next = playMsgList.get(0);
        if (justRecordGameMsgFirstTime == 0L) {
            justRecordGameMsgFirstTime = next.timestamp();
        }
        if (allPlayBackTime >= next.timestamp() - justRecordGameMsgFirstTime) {
            dispatch(next);
            playMsgList.remove(0);
            dispatchProcessChanged();
        }
    }

    /**
     * Java equivalent of the original inclusive slider loop in
     * {@code fastPlay(playCount)}.
     */
    public void fastPlay(int playCount, double now) {
        if (!playback) {
            return;
        }
        boolean pauseState = pause;
        pause = true;
        int nowCount = playedMessageCount();
        if (nowCount > playCount) {
            playMsgList.clear();
            playMsgList.addAll(gameMsgList);
            nowCount = 1;
        }
        try {
            if (playMsgList.isEmpty()) {
                stopPlayback();
                return;
            }
            dropDuplicateLeadingServerForward();
            if (playMsgList.isEmpty()) {
                stopPlayback();
                return;
            }
            lastSysTime = now;
            long lastTime = playMsgList.get(0).timestamp();
            for (int i = nowCount; i <= playCount && !playMsgList.isEmpty(); i++) {
                OriginalPlaybackRecordFile.RecordFrame next = playMsgList.get(0);
                lastTime = next.timestamp();
                dispatch(next);
                playMsgList.remove(0);
            }
            allPlayBackTime = 0.0;
            justRecordGameMsgFirstTime = lastTime;
            dispatchProcessChanged();
        } finally {
            pause = pauseState;
        }
    }

    public int allMessageCount() {
        return gameMsgList.size();
    }

    public int playedMessageCount() {
        return gameMsgList.size() - playMsgList.size();
    }

    private void dropDuplicateLeadingServerForward() {
        if (playMsgList.size() >= 2
                && playMsgList.get(0).xyId() == SERVER_TO_CLIENT_MESSAGE_XY_ID
                && playMsgList.get(1).xyId() == SERVER_TO_CLIENT_MESSAGE_XY_ID) {
            playMsgList.remove(0);
            dispatchProcessChanged();
        }
    }

    private void dispatch(OriginalPlaybackRecordFile.RecordFrame frame) {
        frameSink.onMessage(frame.xyId(), frame.payload());
    }

    private void dispatchProcessChanged() {
        if (processListener != null) {
            processListener.onProcessChanged(playedMessageCount(), allMessageCount());
        }
    }
}
