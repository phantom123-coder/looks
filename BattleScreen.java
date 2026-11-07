package com.looksmaxxing.client;

import com.looksmaxxing.mog.MogBattle;
import com.looksmaxxing.net.MogActionPacket;
import com.looksmaxxing.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/** The Mog Arena battle UI: intro, pose + timing choice, round results, final result. */
public class BattleScreen extends Screen {
    private static final int[] POSE_COLORS = {0xFF5555, 0xFF77CC, 0x66BBFF};

    private int ticks, phaseTicks;
    private int lastPhase = -99, lastRound = -99;
    private int selPose = -1;
    private boolean sentLock = false;
    private final Button[] poseBtns = new Button[3];
    private Button lockBtn, forfeitBtn, closeBtn;

    public BattleScreen() {
        super(Component.literal("Mog Arena"));
    }

    private CompoundTag st() {
        return ClientHandler.battleState;
    }

    private int you() {
        return st().getInt("you");
    }

    private boolean participant() {
        return you() >= 0;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int by = this.height - 44;
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            poseBtns[i] = addRenderableWidget(Button.builder(poseText(i), b -> {
                selPose = idx;
                refreshPoseTexts();
            }).bounds(cx - 160 + i * 108, by, 104, 20).build());
        }
        lockBtn = addRenderableWidget(Button.builder(Component.literal("LOCK IN (Space)"), b -> lock())
                .bounds(cx - 60, by - 24, 120, 20).build());
        forfeitBtn = addRenderableWidget(Button.builder(Component.literal("Forfeit"), b -> {
            Net.CH.sendToServer(new MogActionPacket(MogActionPacket.FORFEIT));
        }).bounds(this.width - 70, 6, 64, 18).build());
        closeBtn = addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(cx - 40, by, 80, 20).build());
    }

    private Component poseText(int i) {
        String base = MogBattle.POSES[i] + " (" + MogBattle.POSE_COST[i] + ")";
        return Component.literal(selPose == i ? "> " + base + " <" : base);
    }

    private void refreshPoseTexts() {
        for (int i = 0; i < 3; i++) poseBtns[i].setMessage(poseText(i));
    }

    private boolean myLocked() {
        String c = you() == 0 ? "A" : "B";
        return sentLock || st().getBoolean("lock" + c);
    }

    private boolean canLock() {
        return participant() && st().getInt("phase") == 1 && !myLocked();
    }

    private void lock() {
        if (!canLock()) return;
        if (selPose < 0) selPose = 2;
        sentLock = true;
        Net.CH.sendToServer(new MogActionPacket(MogActionPacket.LOCK, selPose, 0, null));
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE && canLock()) {
            lock();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void tick() {
        ticks++;
        CompoundTag t = st();
        int ph = t.getInt("phase"), rd = t.getInt("round");
        if (ph != lastPhase || rd != lastRound) {
            lastPhase = ph;
            lastRound = rd;
            phaseTicks = 0;
            if (ph == 1) {
                selPose = -1;
                sentLock = false;
                refreshPoseTexts();
            }
        } else {
            phaseTicks++;
        }
        boolean choose = canLock();
        for (Button b : poseBtns) {
            b.visible = choose;
            b.active = choose;
        }
        lockBtn.visible = choose;
        lockBtn.active = choose;
        forfeitBtn.visible = participant() && ph < 3;
        closeBtn.visible = ph == 3 || !participant();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !participant() || st().getInt("phase") == 3;
    }

    @Override
    public void removed() {
        if (!participant() && st().getInt("phase") < 3) {
            Net.CH.sendToServer(new MogActionPacket(MogActionPacket.LEAVE_SPECTATE));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- drawing

    private void drawPortrait(GuiGraphics g, int cx, int y, int size, int hair, int skin) {
        int x = cx - size / 2;
        g.fill(x, y, x + size, y + size, 0xFF000000 | skin);
        g.fill(x - 2, y - size / 5, x + size + 2, y + size / 5, 0xFF000000 | hair);
        int e = size / 8;
        g.fill(x + size / 4, y + size / 2 - e, x + size / 4 + e, y + size / 2, 0xFF202020);
        g.fill(x + size - size / 4 - e, y + size / 2 - e, x + size - size / 4, y + size / 2, 0xFF202020);
        g.fill(x + size / 3, y + size - size / 4, x + size - size / 3, y + size - size / 4 + e / 2 + 1, 0xFF803030);
    }

    private void drawFighter(GuiGraphics g, boolean left, int cx, int baseY) {
        CompoundTag t = st();
        String c = left ? "A" : "B";
        Minecraft mc = Minecraft.getInstance();
        Player ent = null;
        if (t.hasUUID("id" + c) && mc.level != null) {
            UUID id = t.getUUID("id" + c);
            ent = mc.level.getPlayerByUUID(id);
        }
        int scale = Math.max(28, this.height / 7);
        if (ent != null) {
            float a = (left ? 0.5f : -0.5f) + (float) Math.sin(ticks * 0.05 + (left ? 0 : 2)) * 0.1f;
            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI).rotateY(a);
            InventoryScreen.renderEntityInInventory(g, cx, baseY, scale, pose, null, ent);
        } else {
            drawPortrait(g, cx, baseY - scale * 2 + 10, scale, t.getInt("hair" + c), t.getInt("skin" + c));
        }
        String name = t.getString("name" + c) + (you() == (left ? 0 : 1) ? " (YOU)" : "");
        g.drawCenteredString(this.font, name, cx, baseY + 6, 0xFFFFFF);
        // stamina bar
        Hud.bar(g, cx - 40, baseY + 18, 80, 6, t.getInt("stam" + c) / 100.0, 0x55FF55);
        g.drawCenteredString(this.font, "Stamina " + t.getInt("stam" + c), cx, baseY + 26, 0xAAAAAA);
        g.drawCenteredString(this.font, "Total " + t.getInt("total" + c), cx, baseY + 36, 0xFFDD66);
        if (t.getInt("phase") == 1 && t.getBoolean("lock" + c)) {
            g.drawCenteredString(this.font, "LOCKED IN", cx, baseY + 46, 0x66FF66);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        CompoundTag t = st();
        int phase = t.getInt("phase");
        int cx = this.width / 2;
        g.fill(0, 0, this.width, this.height, 0x66000010);

        String type = MogBattle.TYPES[Math.max(0, Math.min(4, t.getInt("type")))];
        String header = "MOG ARENA - " + type + (t.getBoolean("ranked") ? " (Ranked)" : "");
        if (phase >= 1) header += "   Round " + Math.min(t.getInt("round") + 1, t.getInt("rounds")) + "/" + t.getInt("rounds");
        g.drawCenteredString(this.font, header, cx, 8, 0xFFFFFF);

        int baseY = this.height / 2 + 40;
        drawFighter(g, true, this.width / 4, baseY);
        drawFighter(g, false, this.width * 3 / 4, baseY);

        if (phase == 0) {
            drawIntro(g, t, cx);
        } else if (phase == 1) {
            drawChoose(g, t, cx);
        }
        if (phase >= 1) drawScores(g, t, cx);
        if (phase == 3) drawFinal(g, t, cx);
        super.render(g, mx, my, pt);
    }

    private void drawIntro(GuiGraphics g, CompoundTag t, int cx) {
        float slide = Math.min(1f, phaseTicks / 20f);
        int off = (int) ((1f - slide) * 200);
        g.pose().pushPose();
        g.pose().translate(cx, this.height / 2 - 60, 0);
        g.pose().scale(3f, 3f, 1f);
        g.drawCenteredString(this.font, "VS", 0, 0, 0xFFDD33);
        g.pose().popPose();
        g.drawCenteredString(this.font, t.getString("nameA"), cx - 90 - off, this.height / 2 - 28, 0xFFFFFF);
        g.drawCenteredString(this.font, t.getString("nameB"), cx + 90 + off, this.height / 2 - 28, 0xFFFFFF);
        if (t.contains("blurbB")) g.drawCenteredString(this.font, t.getString("blurbB"), cx, this.height / 2 - 12, 0xAAAAAA);
        g.drawCenteredString(this.font, "Get ready!", cx, this.height / 2 + 4, 0x66FF66);
    }

    private void drawChoose(GuiGraphics g, CompoundTag t, int cx) {
        if (!participant()) {
            g.drawCenteredString(this.font, "Spectating...", cx, this.height / 2 - 20, 0xAAAAAA);
            return;
        }
        int w = 220, x = cx - w / 2, y = this.height / 2 - 24;
        g.fill(x - 2, y - 2, x + w + 2, y + 16, 0xFF101018);
        double target = t.getFloat("target");
        int zoneHalf = (int) (0.08 * w);
        int zx = x + (int) (target * w);
        g.fill(zx - (int) (0.25 * w), y, zx + (int) (0.25 * w), y + 14, 0xFF1D3A1D);
        g.fill(zx - zoneHalf, y, zx + zoneHalf, y + 14, 0xFF2E8B2E);
        g.fill(zx - 1, y, zx + 1, y + 14, 0xFF88FF88);
        double p = (phaseTicks % 60) / 30.0;
        double frac = p <= 1 ? p : 2 - p;
        int mxp = x + (int) (frac * w);
        g.fill(mxp - 1, y - 2, mxp + 2, y + 16, myLocked() ? 0xFFFFAA00 : 0xFFFFFFFF);
        g.drawCenteredString(this.font, myLocked() ? "Locked in! Waiting for the opponent..."
                : "Pick a pose, then lock in when the marker is in the green zone", cx, y - 14, 0xDDDDDD);
        g.drawCenteredString(this.font, "Power beats Calm, Calm beats Charm, Charm beats Power", cx, y + 22, 0x999999);
    }

    private void drawScores(GuiGraphics g, CompoundTag t, int cx) {
        int done = t.getInt("roundsDone");
        int[] pa = t.getIntArray("posesA"), pb = t.getIntArray("posesB");
        int[] sa = t.getIntArray("scoresA"), sb = t.getIntArray("scoresB");
        int y = 24;
        for (int i = 0; i < done && i < sa.length; i++) {
            String left = MogBattle.POSES[Math.max(0, pa[i])] + " " + sa[i];
            String right = sb[i] + " " + MogBattle.POSES[Math.max(0, pb[i])];
            g.drawString(this.font, left, cx - 12 - this.font.width(left), y, POSE_COLORS[Math.max(0, pa[i])]);
            g.drawCenteredString(this.font, "R" + (i + 1), cx, y, 0x888888);
            g.drawString(this.font, right, cx + 12, y, POSE_COLORS[Math.max(0, pb[i])]);
            y += 11;
        }
    }

    private void drawFinal(GuiGraphics g, CompoundTag t, int cx) {
        int winner = t.getInt("winner");
        int you = you();
        String text;
        int color;
        if (winner == 2) {
            text = "DRAW";
            color = 0xBBBBBB;
        } else if (you < 0) {
            text = t.getString(winner == 0 ? "nameA" : "nameB") + " WINS";
            color = 0xFFDD33;
        } else if (winner == you) {
            text = "VICTORY";
            color = 0xFFD700;
        } else {
            text = "DEFEAT";
            color = 0xFF5555;
        }
        int bounce = (int) (Math.sin(ticks * 0.3) * 3);
        g.pose().pushPose();
        g.pose().translate(cx, this.height / 2 - 70 + bounce, 0);
        g.pose().scale(3f, 3f, 1f);
        g.drawCenteredString(this.font, text, 0, 0, color);
        g.pose().popPose();
        if (you >= 0) {
            String c = you == 0 ? "A" : "B";
            g.drawCenteredString(this.font, String.format("Battle performance +%.2f", t.getDouble("gain" + c)), cx, this.height / 2 - 12, 0x66FF66);
            if (t.getBoolean("ranked")) {
                int d = t.getInt("eloDelta" + c);
                g.drawCenteredString(this.font, "ELO " + (d >= 0 ? "+" : "") + d + " (now " + t.getInt("elo" + c) + ")",
                        cx, this.height / 2, d >= 0 ? 0x66FF66 : 0xFF8888);
            }
        }
    }
}
