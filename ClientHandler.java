package com.looksmaxxing.client;

import com.looksmaxxing.data.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientHandler {
    public static PlayerStats stats = new PlayerStats();
    public static boolean synced = false;

    /** How a player looks. Values are 0..1 sliders (jaw/chin already include training). */
    public static class Look {
        public final float height, frame, muscle, jaw, chin, cheek, nose;
        public final int hairStyle, hairColor;

        public Look(float height, float frame, float muscle, float jaw, float chin, float cheek, float nose,
                    int hairStyle, int hairColor) {
            this.height = height;
            this.frame = frame;
            this.muscle = muscle;
            this.jaw = jaw;
            this.chin = chin;
            this.cheek = cheek;
            this.nose = nose;
            this.hairStyle = hairStyle;
            this.hairColor = hairColor;
        }
    }

    public static final Map<UUID, Look> look = new HashMap<>();

    public static void onSync(CompoundTag tag) {
        stats.load(tag);
        synced = true;
    }

    public static void setLook(UUID id, Look l) {
        look.put(id, l);
    }

    public static CompoundTag battleState = new CompoundTag();

    public static boolean inActiveBattle() {
        return battleState.contains("phase") && battleState.getInt("phase") < 3 && battleState.getInt("you") >= 0;
    }

    public static void onMogData(int kind, CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        if (kind == 1) {
            battleState = tag;
            if (!(mc.screen instanceof BattleScreen)) mc.setScreen(new BattleScreen());
            return;
        }
        if (mc.screen instanceof BattleScreen && inActiveBattle()) return; // ignore menus mid-battle
        if (kind == 0) mc.setScreen(new MogMenuScreen(tag));
        else if (kind == 2) mc.setScreen(new LeaderboardScreen(tag));
    }

    public static void openMirror() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && stats.created) mc.setScreen(new CreationScreen(true));
    }
}
