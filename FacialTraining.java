package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

/** Satirical magic ritual: the hammer hits an Enchanted Training Dummy, never the player. */
public class FacialTraining {

    public static boolean perform(ServerPlayer sp, InteractionHand hand) {
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
        if (s.stamina < 20) {
            sp.displayClientMessage(Component.literal("Not enough stamina for the ritual."), true);
            return false;
        }
        s.stamina -= 20;
        s.fatigue = Math.min(100, s.fatigue + 4);

        sp.swing(hand, true);
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.3f);
        ServerLevel sl = sp.level() instanceof ServerLevel l ? l : null;
        if (sl != null) {
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.8, sp.getZ(), 30, 0.4, 0.4, 0.4, 0.5);
            sl.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 1.2, sp.getZ(), 10, 0.5, 0.3, 0.5, 0.2);
        }

        if (s.dailyHammer >= Config.HAMMER_DAILY_LIMIT.get()) {
            // excessive training: no benefit, extra stamina drain
            s.stamina = Math.max(0, s.stamina - 15);
            sp.displayClientMessage(Component.literal("Excessive training! No benefit, and your stamina drops."), true);
            sp.level().playSound(null, sp.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8f, 1.0f);
            ServerEvents.refresh(sp, s);
            return true;
        }
        s.dailyHammer += 1;

        double g = Config.TRAINING_SPEED.get() * Math.max(0.0, 1.0 - s.facial / 110.0) * (1.0 - s.fatigue / 160.0);
        s.jaw = Math.min(100, s.jaw + 1.2 * g);
        s.chin = Math.min(100, s.chin + 0.9 * g);
        s.confidence = Math.min(100, s.confidence + 0.8 * g);
        s.facial = Math.min(100, s.facial + 0.7 * g);

        if (sp.getRandom().nextFloat() < 0.05f) {
            s.facial = Math.min(100, s.facial + 0.5);
            s.jaw = Math.min(100, s.jaw + 0.5);
            sp.level().playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.6f);
            if (sl != null) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.6, sp.getZ(), 40, 0.4, 0.6, 0.4, 0.3);
            }
            sp.displayClientMessage(Component.literal("RARE TRANSFORMATION! Jawline flash!"), true);
        }
        ServerEvents.refresh(sp, s);
        return true;
    }
}
