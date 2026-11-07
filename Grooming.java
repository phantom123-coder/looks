package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

/** Server-authoritative grooming, skincare and posture practice. */
public class Grooming {

    /** Grooming kit / skincare kit use. @return true if the kit was used. */
    public static boolean useKit(ServerPlayer sp, InteractionHand hand, double groomGain, double facialGain) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) {
            sp.displayClientMessage(Component.literal("Create your character first."), true);
            return false;
        }
        ServerEvents.rollDay(sp, s);
        double speed = Config.TRAINING_SPEED.get();
        double gain = groomGain * speed * Math.max(0.0, 1.0 - s.grooming / 110.0);
        gain = Math.min(gain, Math.max(0.0, Config.DAILY_GROOMING_CAP.get() - s.dailyGrooming));
        if (gain <= 0) {
            sp.displayClientMessage(Component.literal("You look great already - no more grooming progress today."), true);
            return false;
        }
        s.grooming = Math.min(100, s.grooming + gain);
        s.dailyGrooming += gain;
        if (facialGain > 0) {
            s.facial = Math.min(100, s.facial + facialGain * speed * Math.max(0.0, 1.0 - s.facial / 110.0));
        }
        sp.swing(hand, true);
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8f, 1.2f);
        if (sp.level() instanceof ServerLevel sl) {
            // perfume cloud
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.4, sp.getZ(), 14, 0.5, 0.6, 0.5, 0.02);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.6, sp.getZ(), 6, 0.4, 0.5, 0.4, 0.01);
        }
        ServerEvents.refresh(sp, s);
        return true;
    }

    /** Posture practice in front of a mirror (sneak + right-click). */
    public static boolean posturePractice(ServerPlayer sp, InteractionHand hand) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) {
            sp.displayClientMessage(Component.literal("Create your character first."), true);
            return false;
        }
        long now = sp.level().getGameTime();
        if (now - s.lastTrain < 40) return false;
        s.lastTrain = now;
        ServerEvents.rollDay(sp, s);
        if (s.fatigue >= 80) {
            sp.displayClientMessage(Component.literal("Overtrained! Sleep in a bed or eat a Protein Meal."), true);
            return false;
        }
        if (s.stamina < 6) {
            sp.displayClientMessage(Component.literal("Not enough stamina - rest a bit."), true);
            return false;
        }
        s.stamina -= 6;
        s.fatigue = Math.min(100, s.fatigue + 1);
        double gain = 0.5 * Config.TRAINING_SPEED.get() * Math.max(0.0, 1.0 - s.posture / 110.0);
        gain = Math.min(gain, Math.max(0.0, Config.DAILY_GROOMING_CAP.get() - s.dailyPosture));
        if (gain <= 0) {
            sp.displayClientMessage(Component.literal("Daily posture limit reached."), true);
        } else {
            s.posture = Math.min(100, s.posture + gain);
            s.dailyPosture += gain;
        }
        sp.swing(hand, true);
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8f, 1.0f);
        ServerEvents.refresh(sp, s);
        return true;
    }
}
