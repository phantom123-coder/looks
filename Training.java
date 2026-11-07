package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

/** Server-authoritative gym training. */
public class Training {

    /** @return true if the exercise was actually performed (even if the daily cap gave no gain). */
    public static boolean perform(ServerPlayer sp, InteractionHand hand, double baseGain,
                                  double staminaCost, double fatigueCost, double postureGain) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) {
            sp.displayClientMessage(Component.literal("Create your character first."), true);
            return false;
        }
        long now = sp.level().getGameTime();
        if (now - s.lastTrain < 20) return false; // cooldown, blocks spam
        s.lastTrain = now;

        ServerEvents.rollDay(sp, s);
        if (s.fatigue >= 80) {
            sp.displayClientMessage(Component.literal("Overtrained! Sleep in a bed or eat a Protein Meal."), true);
            return false;
        }
        if (s.stamina < staminaCost) {
            sp.displayClientMessage(Component.literal("Not enough stamina - rest a bit."), true);
            return false;
        }

        double speed = Config.TRAINING_SPEED.get();
        double gain = baseGain * speed * Math.max(0.0, 1.0 - s.strength / 110.0) * (1.0 - s.fatigue / 160.0);
        gain = Math.min(gain, Math.max(0.0, Config.DAILY_STRENGTH_CAP.get() - s.dailyStrength));

        s.stamina -= staminaCost;
        s.fatigue = Math.min(100, s.fatigue + fatigueCost);

        if (gain <= 0) {
            sp.displayClientMessage(Component.literal("Daily limit reached - no more progress today."), true);
        } else {
            s.strength = Math.min(100, s.strength + gain);
            s.dailyStrength += gain;
            s.posture = Math.min(100, s.posture + postureGain * speed * Math.max(0.0, 1.0 - s.posture / 110.0));
            s.bodyFat = Math.max(8, s.bodyFat - 0.05);
        }

        sp.swing(hand, true);
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.25f, 1.7f);
        if (sp.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 1.2, sp.getZ(), 8, 0.4, 0.4, 0.4, 0.1);
        }
        ServerEvents.refresh(sp, s);
        return true;
    }
}
