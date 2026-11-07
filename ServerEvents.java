package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.data.StatsCapability;
import com.looksmaxxing.data.StatsProvider;
import com.looksmaxxing.net.Net;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Looksmaxxing.MODID)
public class ServerEvents {

    public static PlayerStats get(Player p) {
        return p.getCapability(StatsCapability.STATS).orElse(null);
    }

    public static void rollDay(ServerPlayer sp, PlayerStats s) {
        long day = sp.level().getGameTime() / 24000L;
        if (day != s.dayIndex) {
            s.dayIndex = day;
            s.dailyEndurance = 0;
            s.dailyStrength = 0;
            s.dailyGrooming = 0;
            s.dailyPosture = 0;
            s.dailyHammer = 0;
            s.dailyBattle = 0;
        }
    }

    /** Recompute score, handle rank-ups and sync to the owning client + nearby players. Server only. */
    public static void refresh(ServerPlayer sp, PlayerStats s) {
        s.recompute(Config.W_FACIAL.get(), Config.W_GROOMING.get(), Config.W_PHYSIQUE.get(),
                Config.W_POSTURE.get(), Config.BATTLE_COUNTS.get() ? Config.W_BATTLE.get() : 0.0,
                Config.RANK_DIFFICULTY.get());
        Rank now = Rank.fromScore(s.overall, s.difficulty);
        if (s.created && now.ordinal() > s.highestRank) {
            s.highestRank = now.ordinal();
            if (now != Rank.SUB3) announce(sp, now);
        }
        Net.sync(sp);
        Net.syncAppearance(sp);
    }

    private static void announce(ServerPlayer sp, Rank rank) {
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        sp.connection.send(new ClientboundSetTitleTextPacket(rank.colored()));
        sp.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("RANK UP: " + rank.title)));
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (sp.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0, sp.getZ(), 80, 0.5, 1.0, 0.5, 0.4);
        }
        sp.server.getPlayerList().broadcastSystemMessage(
                Component.literal("").append(sp.getDisplayName()).append(Component.literal(" ranked up to ")).append(rank.colored()), false);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            e.addCapability(new ResourceLocation(Looksmaxxing.MODID, "stats"), new StatsProvider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        e.getOriginal().reviveCaps();
        PlayerStats o = get(e.getOriginal());
        PlayerStats n = get(e.getEntity());
        if (o != null && n != null) n.copyFrom(o);
        e.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            PlayerStats s = get(sp);
            if (s != null) refresh(sp, s);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Net.sync(sp);
            Net.syncAppearance(sp);
        }
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Net.sync(sp);
            Net.syncAppearance(sp);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking e) {
        if (e.getTarget() instanceof ServerPlayer target && e.getEntity() instanceof ServerPlayer viewer) {
            Net.sendAppearanceTo(target, viewer);
        }
    }

    @SubscribeEvent
    public static void wake(PlayerWakeUpEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (e.wakeImmediately()) return;
        PlayerStats s = get(sp);
        if (s == null || !s.created) return;
        s.fatigue = 0;
        s.stamina = 100;
        refresh(sp, s);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) com.looksmaxxing.mog.MogManager.tick(server);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) com.looksmaxxing.mog.MogManager.onLogout(sp);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER) return;
        if (!(e.player instanceof ServerPlayer sp)) return;
        PlayerStats s = get(sp);
        if (s == null || !s.created) return;

        // stamina / fatigue recovery once per second
        if (sp.tickCount % 20 == 0) {
            double st = s.stamina, f = s.fatigue;
            s.stamina = Math.min(100, s.stamina + 2.0);
            s.fatigue = Math.max(0, s.fatigue - 0.1);
            if (st != s.stamina || f != s.fatigue) Net.sync(sp);
        }

        double dx = sp.getX() - s.lastX;
        double dz = sp.getZ() - s.lastZ;
        boolean had = s.hasLast;
        s.lastX = sp.getX();
        s.lastZ = sp.getZ();
        s.hasLast = true;
        if (!had) return;

        double d = Math.sqrt(dx * dx + dz * dz);
        // server-side validation: only real on-foot sprinting, no vehicles/flying/teleports
        if (d <= 0 || d > 2.0 || !sp.isSprinting() || !sp.onGround() || sp.isPassenger() || sp.isFallFlying()) return;

        s.accum += d;
        s.totalDistance += d;
        if (s.accum < 25) return;

        rollDay(sp, s);
        double gain = s.accum * 0.02 * Config.TRAINING_SPEED.get() * Math.max(0.0, 1.0 - s.endurance / 110.0);
        gain = Math.min(gain, Math.max(0.0, Config.DAILY_ENDURANCE_CAP.get() - s.dailyEndurance));
        s.accum = 0;
        if (gain > 0) {
            s.endurance = Math.min(100, s.endurance + gain);
            s.dailyEndurance += gain;
            s.bodyFat = Math.max(8, s.bodyFat - gain * 0.1);
        }
        refresh(sp, s);
    }
}
