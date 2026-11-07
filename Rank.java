package com.looksmaxxing;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public enum Rank {
    SUB3("SUB3", "SUB3", 0x8B0000, 0),
    SUB5("SUB5", "SUB5", 0xFF5555, 12),
    LTN("LTN", "Low-Tier Normie", 0xFFAA00, 25),
    MTN("MTN", "Mid-Tier Normie", 0xDDDD33, 40),
    HTN("HTN", "High-Tier Normie", 0x55FF55, 55),
    CHAD("CHAD", "CHAD", 0x55FFFF, 70),
    TRUE_CHAD("TRUE CHAD", "TRUE CHAD", 0xAA55FF, 85),
    TRUE_ADAM("TRUE ADAM", "TRUE ADAM", 0xFFD700, 95);

    public final String tag;
    public final String title;
    public final int color;
    public final double threshold;

    Rank(String tag, String title, int color, double threshold) {
        this.tag = tag;
        this.title = title;
        this.color = color;
        this.threshold = threshold;
    }

    public static Rank fromScore(double overall, double difficulty) {
        double adj = overall / difficulty;
        Rank r = SUB3;
        for (Rank k : values()) {
            if (adj >= k.threshold) r = k;
        }
        return r;
    }

    public boolean isLast() {
        return this == TRUE_ADAM;
    }

    public Rank next() {
        return isLast() ? this : values()[ordinal() + 1];
    }

    public double progress(double overall, double difficulty) {
        if (isLast()) return 1.0;
        Rank n = next();
        double adj = overall / difficulty;
        double p = (adj - threshold) / (n.threshold - threshold);
        return Math.max(0.0, Math.min(1.0, p));
    }

    public MutableComponent colored() {
        return Component.literal(tag).withStyle(Style.EMPTY.withColor(color).withBold(true));
    }

    public static String ratingLabel(double rating) {
        String[] names = {"Beginner", "Developing", "Improving", "Average",
                "Above Average", "High Tier", "Chad Tier", "True Adam"};
        int i = (int) Math.floor(rating) - 1;
        i = Math.max(0, Math.min(7, i));
        return names[i];
    }
}
