package com.looksmaxxing.client;

import com.looksmaxxing.mog.MogBattle;
import com.looksmaxxing.net.MogActionPacket;
import com.looksmaxxing.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/** Matchmaking menu: NPCs, friendly challenges, ranked queue, leaderboard, challenge toggle. */
public class MogMenuScreen extends Screen {
    private static int battleType = 0;
    private static final int PER_PAGE = 6;

    private final CompoundTag data;
    private int page = 0;

    public MogMenuScreen(CompoundTag data) {
        super(Component.literal("Mog Arena"));
        this.data = data;
    }

    private void send(MogActionPacket p) {
        Net.CH.sendToServer(p);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 160;
        int right = this.width / 2 + 10;
        int y = 44;
        addRenderableWidget(Button.builder(Component.literal("Battle type: " + MogBattle.TYPES[battleType]), b -> {
            battleType = (battleType + 1) % MogBattle.TYPES.length;
            b.setMessage(Component.literal("Battle type: " + MogBattle.TYPES[battleType]));
        }).bounds(left, y, 150, 20).build());
        y += 28;
        String[] tiers = {"Easy NPC", "Normal NPC", "Hard NPC"};
        for (int i = 0; i < 3; i++) {
            final int tier = i + 1;
            addRenderableWidget(Button.builder(Component.literal("Fight: " + tiers[i]), b -> {
                send(new MogActionPacket(MogActionPacket.NPC, battleType, tier, null));
                onClose();
            }).bounds(left, y, 150, 20).build());
            y += 24;
        }
        y += 4;
        boolean inQueue = data.getBoolean("inQueue");
        addRenderableWidget(Button.builder(Component.literal(inQueue ? "Leave ranked queue" : "Join ranked queue"), b -> {
            send(inQueue ? new MogActionPacket(MogActionPacket.QUEUE_LEAVE)
                    : new MogActionPacket(MogActionPacket.QUEUE_JOIN, battleType, 0, null));
        }).bounds(left, y, 150, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Leaderboard"), b ->
                send(new MogActionPacket(MogActionPacket.LEADERBOARD))).bounds(left, y, 150, 20).build());
        y += 24;
        boolean on = data.getBoolean("challengesOn");
        addRenderableWidget(Button.builder(Component.literal("Challenges: " + (on ? "ON" : "OFF")), b ->
                send(new MogActionPacket(MogActionPacket.TOGGLE))).bounds(left, y, 150, 20).build());

        // players list
        ListTag players = data.getList("players", Tag.TAG_COMPOUND);
        int py = 62;
        int start = page * PER_PAGE;
        for (int i = start; i < Math.min(players.size(), start + PER_PAGE); i++) {
            CompoundTag p = players.getCompound(i);
            final UUID id = p.getUUID("id");
            String label = p.getString("name") + "  (ELO " + p.getInt("elo") + ")";
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                send(new MogActionPacket(MogActionPacket.CHALLENGE, battleType, 0, id));
                onClose();
            }).bounds(right, py, 150, 20).build());
            py += 24;
        }
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                page--;
                rebuildWidgets();
            }).bounds(right, py + 4, 30, 18).build());
        }
        if (start + PER_PAGE < players.size()) {
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                page++;
                rebuildWidgets();
            }).bounds(right + 120, py + 4, 30, 18).build());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int cx = this.width / 2;
        g.fill(cx - 175, 10, cx + 175, this.height - 10, 0xCC101018);
        g.drawCenteredString(this.font, "MOG ARENA", cx, 16, 0xFFDD33);
        g.drawCenteredString(this.font, String.format("ELO %d   W %d / L %d   Win streak %d",
                data.getInt("elo"), data.getInt("wins"), data.getInt("losses"), data.getInt("streak")), cx, 30, 0xDDDDDD);
        g.drawString(this.font, "Online players (challenge):", cx + 10, 50, 0xAAAAAA);
        if (data.getList("players", Tag.TAG_COMPOUND).isEmpty()) {
            g.drawString(this.font, "Nobody available right now", cx + 10, 66, 0x777777);
        }
        super.render(g, mx, my, pt);
    }
}
