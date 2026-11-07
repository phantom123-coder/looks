package com.looksmaxxing.client;

import com.looksmaxxing.Rank;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class Hud {
    public static void bar(GuiGraphics g, int x, int y, int w, int h, double frac, int rgb) {
        frac = Math.max(0, Math.min(1, frac));
        g.fill(x, y, x + w, y + h, 0xFF202020);
        g.fill(x + 1, y + 1, x + 1 + (int) ((w - 2) * frac), y + h - 1, 0xFF000000 | rgb);
    }

    public static void render(ForgeGui gui, GuiGraphics g, float partial, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        PlayerStats s = ClientHandler.stats;
        if (mc.options.hideGui || !ClientHandler.synced || !s.created) return;

        Rank rank = Rank.fromScore(s.overall, s.difficulty);
        double rating = s.rating();
        int x = 6, y = 6;
        g.fill(x - 3, y - 3, x + 130, y + 88, 0x88000000);
        g.drawString(mc.font, rank.colored(), x, y, 0xFFFFFF);
        g.drawString(mc.font, Component.literal(String.format("Rating %.1f/8 (%s)", rating, Rank.ratingLabel(rating))), x, y + 11, 0xFFFFFF);
        double p = rank.progress(s.overall, s.difficulty);
        bar(g, x, y + 23, 120, 6, p, rank.color);
        String next = rank.isLast() ? "MAX" : "Next: " + rank.next().tag + " " + (int) (p * 100) + "%";
        g.drawString(mc.font, Component.literal(next), x, y + 32, 0xAAAAAA);
        g.drawString(mc.font, Component.literal(String.format("STR %d  END %d", (int) s.strength, (int) s.endurance)), x, y + 42, 0xAAAAAA);
        g.drawString(mc.font, Component.literal("Stamina"), x, y + 52, 0xAAAAAA);
        bar(g, x + 46, y + 53, 74, 6, s.stamina / 100.0, 0x55FF55);
        g.drawString(mc.font, Component.literal("Fatigue"), x, y + 62, 0xAAAAAA);
        bar(g, x + 46, y + 63, 74, 6, s.fatigue / 100.0, s.fatigue >= 80 ? 0xFF5555 : 0xFFAA00);
        g.drawString(mc.font, Component.literal(String.format("JAW %d  CHIN %d  CONF %d", (int) s.jaw, (int) s.chin, (int) s.confidence)), x, y + 73, 0xAAAAAA);
    }
}
