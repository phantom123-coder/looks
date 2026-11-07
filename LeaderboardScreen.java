package com.looksmaxxing.client;

import com.looksmaxxing.Rank;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class LeaderboardScreen extends Screen {
    private final CompoundTag data;

    public LeaderboardScreen(CompoundTag data) {
        super(Component.literal("Leaderboard"));
        this.data = data;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(this.width / 2 - 40, this.height - 32, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int cx = this.width / 2;
        g.fill(cx - 160, 10, cx + 160, this.height - 10, 0xCC101018);
        g.drawCenteredString(this.font, "MOG ARENA LEADERBOARD", cx, 16, 0xFFDD33);
        ListTag list = data.getList("entries", Tag.TAG_COMPOUND);
        int y = 36;
        g.drawString(this.font, "#", cx - 150, y, 0x888888);
        g.drawString(this.font, "Player", cx - 130, y, 0x888888);
        g.drawString(this.font, "ELO", cx + 10, y, 0x888888);
        g.drawString(this.font, "W/L", cx + 50, y, 0x888888);
        g.drawString(this.font, "Rating", cx + 100, y, 0x888888);
        y += 14;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            int color = i == 0 ? 0xFFD700 : 0xFFFFFF;
            g.drawString(this.font, String.valueOf(i + 1), cx - 150, y, color);
            g.drawString(this.font, e.getString("name"), cx - 130, y, color);
            g.drawString(this.font, String.valueOf(e.getInt("elo")), cx + 10, y, color);
            g.drawString(this.font, e.getInt("wins") + "/" + e.getInt("losses"), cx + 50, y, color);
            double r = e.getDouble("rating");
            g.drawString(this.font, String.format("%.1f %s", r, Rank.ratingLabel(r)), cx + 100, y, color);
            y += 12;
        }
        if (list.isEmpty()) g.drawCenteredString(this.font, "No battles yet. Be the first!", cx, y + 10, 0x888888);
        int rank = data.getInt("yourRank");
        g.drawCenteredString(this.font, rank > 0 ? "Your rank: #" + rank + " of " + data.getInt("total") : "You are not ranked yet",
                cx, this.height - 50, 0xAAAAAA);
        super.render(g, mx, my, pt);
    }
}
