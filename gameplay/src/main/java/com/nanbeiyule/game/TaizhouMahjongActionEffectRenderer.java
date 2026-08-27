package com.nanbeiyule.game;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import com.nanbeiyule.game.cocosarmature.ArmatureAtlas;
import com.nanbeiyule.game.cocosarmature.ArmatureData;
import com.nanbeiyule.game.cocosarmature.ArmatureExportJson;
import com.nanbeiyule.game.cocosarmature.ArmaturePlayer;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.HashMap;
import java.util.Map;

final class TaizhouMahjongActionEffectRenderer {
    private static final String DIR = "taizhou_mahjong_action_effects/cardtype_ani";

    private final ArmatureData data;
    private final ArmatureAtlas atlas;
    private final Map<String, ArmaturePlayer> players = new HashMap<>();

    TaizhouMahjongActionEffectRenderer(Context context) {
        AssetManager assets = context.getAssets();
        data = ArmatureExportJson.load(assets, DIR + "/cardtype_ani.ExportJson");
        atlas =
                ArmatureAtlas.load(
                        assets,
                        new String[] {
                            DIR + "/cardtype_ani0.png",
                            DIR + "/cardtype_ani1.png",
                            DIR + "/cardtype_ani2.png",
                        },
                        new String[] {
                            DIR + "/cardtype_ani0.json",
                            DIR + "/cardtype_ani1.json",
                            DIR + "/cardtype_ani2.json",
                        });
    }

    void draw(
            Canvas canvas,
            TaizhouMahjongActionEffectTracker.Running running,
            int mySeat,
            int chairCount,
            long nowElapsed) {
        if (running == null) {
            return;
        }
        int localSeat = TaizhouMahjongSeatMapper.toLocalSeat(running.serverSeat(), mySeat, chairCount);
        if (chairCount == 2
                && localSeat != TaizhouMahjongTableLayout.SEAT_BOTTOM
                && localSeat != TaizhouMahjongTableLayout.SEAT_TOP) {
            return;
        }
        ArmaturePlayer player =
                players.computeIfAbsent(
                        running.animationName(),
                        name -> new ArmaturePlayer(data, atlas, data.movement(name)));
        TaizhouMahjongActionEffect.Anchor anchor = TaizhouMahjongActionEffect.anchor(localSeat);
        player.draw(
                canvas,
                (nowElapsed - running.startedAtMillis()) / 1000.0f,
                anchor.designX(),
                anchor.androidY(),
                1.0f);
    }

    void release() {
        if (players.isEmpty()) {
            atlas.recycle();
            return;
        }
        for (ArmaturePlayer player : players.values()) {
            if (player != null) {
                player.recycle();
                break;
            }
        }
        players.clear();
    }
}
