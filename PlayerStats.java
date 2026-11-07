package com.looksmaxxing.data;

import net.minecraft.nbt.CompoundTag;

public class PlayerStats {
    public static final int SLIDER_COUNT = 7;

    // appearance (sliders 0..1: jawline, chin, cheekbones, eye spacing, nose size, frame, height)
    public boolean created;
    public final float[] sliders = {0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
    public int hairStyle;
    public int hairColor = 0x3B2A1A;
    public float skinTone = 0.5f;

    // attributes 0..100
    public double facial = 5, grooming, strength, endurance, posture, battle;

    // progress
    public double totalDistance;
    public double overall;
    public double difficulty = 1.0;
    public int highestRank;
    public long dayIndex;
    public double dailyEndurance;
    public double dailyStrength;
    public double dailyGrooming, dailyPosture, dailyHammer;

    // facial training (Stage 3)
    public double jaw, chin, confidence;

    // Mog Arena (Stage 4)
    public double dailyBattle;
    public boolean challengesOn = true;

    // body / recovery (Stage 2)
    public double stamina = 100;
    public double fatigue = 0;
    public double bodyFat = 25;

    // server-only runtime state (not saved)
    public transient long lastTrain = -100;
    public transient long mirrorUntil = -1;
    public transient double accum, lastX, lastZ;
    public transient boolean hasLast;

    public double physique() {
        return (strength + endurance) / 2.0;
    }

    public double rating() {
        return 1.0 + overall * 0.07;
    }

    public void recompute(double wf, double wg, double wph, double wpo, double wb, double diff) {
        double sum = wf + wg + wph + wpo + wb;
        if (sum <= 0) sum = 1;
        double v = (facial * wf + grooming * wg + physique() * wph + posture * wpo + battle * wb) / sum;
        overall = Math.max(0.0, Math.min(100.0, v));
        difficulty = diff;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("created", created);
        for (int i = 0; i < SLIDER_COUNT; i++) t.putFloat("s" + i, sliders[i]);
        t.putInt("hairStyle", hairStyle);
        t.putInt("hairColor", hairColor);
        t.putFloat("skin", skinTone);
        t.putDouble("facial", facial);
        t.putDouble("grooming", grooming);
        t.putDouble("strength", strength);
        t.putDouble("endurance", endurance);
        t.putDouble("posture", posture);
        t.putDouble("battle", battle);
        t.putDouble("distance", totalDistance);
        t.putDouble("overall", overall);
        t.putDouble("difficulty", difficulty);
        t.putInt("highestRank", highestRank);
        t.putLong("dayIndex", dayIndex);
        t.putDouble("dailyEndurance", dailyEndurance);
        t.putDouble("dailyStrength", dailyStrength);
        t.putDouble("dailyGrooming", dailyGrooming);
        t.putDouble("dailyPosture", dailyPosture);
        t.putDouble("dailyHammer", dailyHammer);
        t.putDouble("jaw", jaw);
        t.putDouble("chin", chin);
        t.putDouble("confidence", confidence);
        t.putDouble("dailyBattle", dailyBattle);
        t.putBoolean("challengesOn", challengesOn);
        t.putDouble("stamina", stamina);
        t.putDouble("fatigue", fatigue);
        t.putDouble("bodyFat", bodyFat);
        return t;
    }

    public void load(CompoundTag t) {
        created = t.getBoolean("created");
        for (int i = 0; i < SLIDER_COUNT; i++) {
            sliders[i] = t.contains("s" + i) ? t.getFloat("s" + i) : 0.5f;
        }
        hairStyle = t.getInt("hairStyle");
        hairColor = t.contains("hairColor") ? t.getInt("hairColor") : 0x3B2A1A;
        skinTone = t.contains("skin") ? t.getFloat("skin") : 0.5f;
        facial = t.contains("facial") ? t.getDouble("facial") : 5;
        grooming = t.getDouble("grooming");
        strength = t.getDouble("strength");
        endurance = t.getDouble("endurance");
        posture = t.getDouble("posture");
        battle = t.getDouble("battle");
        totalDistance = t.getDouble("distance");
        overall = t.getDouble("overall");
        difficulty = t.contains("difficulty") ? t.getDouble("difficulty") : 1.0;
        highestRank = t.getInt("highestRank");
        dayIndex = t.getLong("dayIndex");
        dailyEndurance = t.getDouble("dailyEndurance");
        dailyStrength = t.getDouble("dailyStrength");
        dailyGrooming = t.getDouble("dailyGrooming");
        dailyPosture = t.getDouble("dailyPosture");
        dailyHammer = t.getDouble("dailyHammer");
        jaw = t.getDouble("jaw");
        chin = t.getDouble("chin");
        confidence = t.getDouble("confidence");
        dailyBattle = t.getDouble("dailyBattle");
        challengesOn = !t.contains("challengesOn") || t.getBoolean("challengesOn");
        stamina = t.contains("stamina") ? t.getDouble("stamina") : 100;
        fatigue = t.getDouble("fatigue");
        bodyFat = t.contains("bodyFat") ? t.getDouble("bodyFat") : 25;
    }

    public void copyFrom(PlayerStats o) {
        load(o.save());
    }
}
