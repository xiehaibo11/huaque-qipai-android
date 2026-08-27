package com.nanbeiyule.game.cocosarmature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

/** 用仓库内导出的托管骨骼 {@code ios_tuoguan.ExportJson} 校验解析与取样。 */
public final class ArmatureExportJsonTest {
    private static final Path TRUST_ARMATURE =
            Path.of(
                    "src/main/assets/taizhou_trust_effects/tuoguan_ani/ios_tuoguan.ExportJson");
    private static final Path DICE_ARMATURE =
            Path.of(
                    "src/main/assets/taizhou_mahjong_dice_effects/saizi_ani/saizi_ani.ExportJson");

    private static ArmatureData trustArmature() throws Exception {
        return ArmatureExportJson.parse(
                new String(Files.readAllBytes(TRUST_ARMATURE), StandardCharsets.UTF_8));
    }

    private static ArmatureData diceArmature() throws Exception {
        return ArmatureExportJson.parse(
                new String(Files.readAllBytes(DICE_ARMATURE), StandardCharsets.UTF_8));
    }

    @Test
    public void readsTheOriginalBonesInDrawOrder() throws Exception {
        ArmatureData data = trustArmature();

        assertEquals("ios_tuoguan", data.name());
        // bone_data 的 z 分别是 wz=2、dian=3、dian_Copy4=4、dian_Copy4_Copy5=5、jqr=6。
        assertEquals(
                List.of("wz", "dian", "dian_Copy4", "dian_Copy4_Copy5", "jqr"),
                data.bones().stream().map(ArmatureData.Bone::name).toList());
    }

    @Test
    public void keepsTheOriginalStaticBonePose() throws Exception {
        ArmatureData bones = trustArmature();
        ArmatureData.Bone robot =
                bones.bones().stream().filter(b -> b.name().equals("jqr")).findFirst().orElseThrow();

        assertEquals(0.0f, robot.x(), 0.0f);
        assertEquals(125.0f, robot.y(), 0.0f);
        assertEquals(List.of("jqr.png", "jqr2.png"), robot.displays());
    }

    @Test
    public void readsTheLoopingMovementAndItsScale() throws Exception {
        ArmatureData.Movement movement = trustArmature().movement("tuoguan_ani");

        assertEquals(30, movement.durationFrames());
        assertTrue(movement.loop());
        assertEquals(0.5f, movement.scale(), 0.0f);
        assertEquals(5, movement.tracks().size());
    }

    @Test
    public void readsTheTextureAnchorPoints() throws Exception {
        ArmatureData.Texture text = trustArmature().texture("zztg_wz.png");

        assertEquals(0.5f, text.anchorX(), 0.0f);
        assertEquals(0.5f, text.anchorY(), 0.0f);
        assertEquals(840.0f, text.width(), 0.0f);
    }

    @Test
    public void readsTheOriginalDiceMovementsAndDisplays() throws Exception {
        ArmatureData data = diceArmature();

        assertEquals("saizi_ani", data.name());
        assertEquals(35, data.movement("loop").durationFrames());
        for (int value = 1; value <= 6; value++) {
            assertEquals(1, data.movement(String.valueOf(value)).durationFrames());
        }
        assertEquals(
                List.of(
                        "saizi/sz_7.png",
                        "saizi/sz_8.png",
                        "saizi/sz_9.png",
                        "saizi/sz_10.png",
                        "saizi/sz_11.png",
                        "saizi/sz_12.png",
                        "saizi/sz_1.png",
                        "saizi/sz_2.png",
                        "saizi/sz_3.png",
                        "saizi/sz_4.png",
                        "saizi/sz_5.png",
                        "saizi/sz_6.png"),
                data.bones().get(0).displays());
    }
}
