package com.looksmaxxing.client;

import com.looksmaxxing.Rank;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ProfileScreen extends Screen {
    public ProfileScreen() {
        super(Component.literal("Looksmaxxing Profile"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        PlayerStats s = ClientHandler.stats;
        int w = 260, h = 275;
        int x = (this.width - w) / 2, y = (this.height - h) / 2;
        g.fill(x, y, x + w, y + h, 0xEE101018);

        Rank rank = Rank.fromScore(s.overall, s.difficulty);
        double rating = s.rating();
        g.drawCenteredString(this.font, this.title, x + w / 2, y + 8, 0xFFFFFF);
        g.drawCenteredString(this.font, rank.colored().append(Component.literal("  " + rank.title)), x + w / 2, y + 22, 0xFFFFFF);
        g.drawCenteredString(this.font, Component.literal(String.format("Rating %.2f / 8  -  %s  (fictional, game-only)", rating, Rank.ratingLabel(rating))),
                x + w / 2, y + 34, 0xCCCCCC);

        double p = rank.progress(s.overall, s.difficulty);
        Hud.bar(g, x + 10, y + 48, w - 20, 8, p, rank.color);
        g.drawString(this.font, rank.isLast() ? "Max rank" : "To " + rank.next().tag + ": " + (int) (p * 100) + "%", x + 10, y + 59, 0xAAAAAA);

        String[] names = {"Facial harmony", "Grooming", "Strength", "Endurance", "Posture", "Battle", "Stamina", "Fatigue", "Body fat", "Jawline", "Chin", "Confidence"};
        double[] vals = {s.facial, s.grooming, s.strength, s.endurance, s.posture, s.battle, s.stamina, s.fatigue, s.bodyFat, s.jaw, s.chin, s.confidence};
        int yy = y + 76;
        for (int i = 0; i < names.length; i++) {
            g.drawString(this.font, names[i], x + 10, yy, 0xFFFFFF);
            Hud.bar(g, x + 100, yy + 1, 120, 7, vals[i] / 100.0, 0x55AAFF);
            g.drawString(this.font, String.valueOf((int) vals[i]), x + 226, yy, 0xAAAAAA);
            yy += 13;
        }
        g.drawString(this.font, String.format("Distance sprinted: %.0f blocks", s.totalDistance), x + 10, yy + 4, 0xAAAAAA);
        super.render(g, mx, my, pt);
    }
}
