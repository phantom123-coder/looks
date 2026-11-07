package com.looksmaxxing.client;

import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.net.CreationPacket;
import com.looksmaxxing.net.Net;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Quaternionf;

public class CreationScreen extends Screen {
    private static final String[] LABELS = {"Jawline", "Chin", "Cheekbones", "Eye spacing", "Nose size", "Frame", "Height"};
    private static final String[] HAIR_NAMES = {"Buzz", "Crew", "Quiff", "Long", "Curly", "Slicked"};
    private static final int[] HAIR_COLORS = {0x1A1110, 0x3B2A1A, 0x6B4A2B, 0xB8860B, 0xD8C28A, 0x8B2E16, 0x808080, 0xFFFFFF};

    private final boolean edit;
    private final float[] sliders = {0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
    private float skin = 0.5f;
    private int hairStyle = 0;
    private int hairColorIdx = 1;
    private boolean confirmed = false;

    public CreationScreen() {
        this(false);
    }

    public CreationScreen(boolean edit) {
        super(Component.literal(edit ? "Mirror - edit your look" : "Create your character"));
        this.edit = edit;
        if (edit) {
            PlayerStats s = ClientHandler.stats;
            System.arraycopy(s.sliders, 0, sliders, 0, PlayerStats.SLIDER_COUNT);
            skin = s.skinTone;
            hairStyle = Math.max(0, Math.min(HAIR_NAMES.length - 1, s.hairStyle));
            for (int i = 0; i < HAIR_COLORS.length; i++) {
                if (HAIR_COLORS[i] == s.hairColor) hairColorIdx = i;
            }
        }
    }

    @Override
    protected void init() {
        int w = 150;
        int x = this.width - w - 20;
        int step = Math.max(20, Math.min(24, (this.height - 50) / 11));
        int y = 20;
        for (int i = 0; i < PlayerStats.SLIDER_COUNT; i++) {
            final int idx = i;
            addRenderableWidget(new FloatSlider(x, y, w, 20, LABELS[i], sliders[i], v -> sliders[idx] = v));
            y += step;
        }
        addRenderableWidget(new FloatSlider(x, y, w, 20, "Skin tone", skin, v -> skin = v));
        y += step;
        addRenderableWidget(Button.builder(hairStyleText(), b -> {
            hairStyle = (hairStyle + 1) % HAIR_NAMES.length;
            b.setMessage(hairStyleText());
        }).bounds(x, y, w, 20).build());
        y += step;
        addRenderableWidget(Button.builder(hairColorText(), b -> {
            hairColorIdx = (hairColorIdx + 1) % HAIR_COLORS.length;
            b.setMessage(hairColorText());
        }).bounds(x, y, w, 20).build());
        y += step;
        addRenderableWidget(Button.builder(Component.literal(edit ? "Save look" : "Create character"), b -> confirm())
                .bounds(x, y + 4, w, 20).build());
    }

    private Component hairStyleText() {
        return Component.literal("Hairstyle: " + HAIR_NAMES[hairStyle]);
    }

    private Component hairColorText() {
        return Component.literal("Hair color: \u25A0\u25A0\u25A0")
                .withStyle(Style.EMPTY.withColor(HAIR_COLORS[hairColorIdx]));
    }

    private ClientHandler.Look previewLook() {
        PlayerStats s = ClientHandler.stats;
        float muscle = (float) (s.strength / 100.0);
        float jaw = Math.min(1f, sliders[0] * 0.7f + (float) (s.jaw / 100.0) * 0.3f);
        float chin = Math.min(1f, sliders[1] * 0.7f + (float) (s.chin / 100.0) * 0.3f);
        return new ClientHandler.Look(sliders[6], sliders[5], muscle, jaw, chin, sliders[2], sliders[4],
                hairStyle, HAIR_COLORS[hairColorIdx]);
    }

    private void confirm() {
        Net.CH.sendToServer(new CreationPacket(sliders.clone(), hairStyle, HAIR_COLORS[hairColorIdx], skin));
        confirmed = true;
        ClientHandler.stats.created = true; // optimistic; the server confirms with a sync
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) ClientHandler.setLook(mc.player.getUUID(), previewLook());
        mc.setScreen(null);
    }

    @Override
    public void removed() {
        // cancelled edit: go back to the saved look
        Minecraft mc = Minecraft.getInstance();
        if (edit && !confirmed && mc.player != null) {
            PlayerStats s = ClientHandler.stats;
            float jaw = Math.min(1f, s.sliders[0] * 0.7f + (float) (s.jaw / 100.0) * 0.3f);
            float chin = Math.min(1f, s.sliders[1] * 0.7f + (float) (s.chin / 100.0) * 0.3f);
            ClientHandler.setLook(mc.player.getUUID(), new ClientHandler.Look(s.sliders[6], s.sliders[5],
                    (float) (s.strength / 100.0), jaw, chin, s.sliders[2], s.sliders[4], s.hairStyle, s.hairColor));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return edit;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int panelRight = this.width - 190;
        g.fill(10, 10, panelRight, this.height - 10, 0xAA101018);
        g.drawCenteredString(this.font, this.title, panelRight / 2, 16, 0xFFFFFF);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // live preview on the real model: height, frame, face features and hair
            ClientHandler.setLook(mc.player.getUUID(), previewLook());
            int scale = Math.max(30, this.height / 5);
            float angle = (float) ((Util.getMillis() % 8000L) / 8000.0 * Math.PI * 2.0);
            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI).rotateY(angle);
            int feetY = this.height / 2 + (int) (scale * 0.95);
            InventoryScreen.renderEntityInInventory(g, panelRight / 2, feetY, scale, pose, null, mc.player);
        }
        g.drawCenteredString(this.font, "Skin tone is saved only: your own skin is never recolored.",
                panelRight / 2, this.height - 24, 0x888888);
        super.render(g, mouseX, mouseY, partial);
    }
}
