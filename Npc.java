package com.looksmaxxing.mog;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** Virtual NPC opponents with different looks, personalities, stats and difficulty. */
public class Npc {
    public final String name, blurb;
    public final int tier;
    public final double baseLo, baseHi, skillLo, skillHi;
    public final double[] poseBias; // Power, Charm, Calm
    public final int hair, skin;

    private Npc(String name, String blurb, int tier, double baseLo, double baseHi, double skillLo, double skillHi,
                double[] poseBias, int hair, int skin) {
        this.name = name;
        this.blurb = blurb;
        this.tier = tier;
        this.baseLo = baseLo;
        this.baseHi = baseHi;
        this.skillLo = skillLo;
        this.skillHi = skillHi;
        this.poseBias = poseBias;
        this.hair = hair;
        this.skin = skin;
    }

    public static final List<Npc> ALL = new ArrayList<>();

    static {
        ALL.add(new Npc("Timmy the Sub-5", "Nervous, but trying his best.", 1, 0.20, 0.38, 0.35, 0.55,
                new double[]{0.15, 0.25, 0.60}, 0x5A3A1A, 0xE8C9A8));
        ALL.add(new Npc("Gary Gymbro", "Never skips arm day. Always skips leg day.", 1, 0.25, 0.40, 0.35, 0.55,
                new double[]{0.65, 0.10, 0.25}, 0x1A1110, 0xD9A877));
        ALL.add(new Npc("Stacy Stylist", "Always has a fresh fade.", 2, 0.45, 0.62, 0.55, 0.72,
                new double[]{0.20, 0.60, 0.20}, 0xB8860B, 0xF0D0B0));
        ALL.add(new Npc("Marcus the Mid", "Mid-tier, but proud of it.", 2, 0.50, 0.65, 0.55, 0.72,
                new double[]{0.34, 0.33, 0.33}, 0x3B2A1A, 0xC99A6B));
        ALL.add(new Npc("Chadwick Prime", "Jaw like a granite slab.", 3, 0.72, 0.88, 0.75, 0.90,
                new double[]{0.45, 0.30, 0.25}, 0x1A1110, 0xD2A679));
        ALL.add(new Npc("The Last Adam", "A legend of the arena.", 3, 0.78, 0.92, 0.78, 0.92,
                new double[]{0.30, 0.30, 0.40}, 0xD8C28A, 0xF5D5B5));
    }

    public static Npc pick(int tier, RandomSource rnd) {
        List<Npc> pool = new ArrayList<>();
        for (Npc n : ALL) if (n.tier == tier) pool.add(n);
        if (pool.isEmpty()) pool.addAll(ALL);
        return pool.get(rnd.nextInt(pool.size()));
    }

    public int elo() {
        return tier == 1 ? 900 : tier == 2 ? 1100 : 1300;
    }
}
