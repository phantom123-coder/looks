package com.looksmaxxing.mog;

import com.looksmaxxing.Config;
import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One server-authoritative Mog Arena battle: 5 rounds, pose + timing + stamina + counters. */
public class MogBattle {
    public static final int ROUNDS = 5;
    public static final String[] TYPES = {"Face-off", "Physique", "Style", "Posture", "Overall"};
    public static final String[] POSES = {"Power", "Charm", "Calm"};
    public static final int[] POSE_COST = {30, 20, 10};
    static final int INTRO = 0, CHOOSE = 1, RESULT = 2, DONE = 3;

    public static class Side {
        UUID id;
        String name;
        Npc npc;
        double base;
        int stamina = 100;
        int pose = -1;
        double acc;
        boolean locked;
        int lockTick;
        int hair, skin;
        int elo = 1000;
        final int[] poses = new int[ROUNDS];
        final int[] scores = new int[ROUNDS];
        double total, accSum, gain;
        int eloDelta;

        Side() {
            Arrays.fill(poses, -1);
        }

        static Side player(MinecraftServer server, ServerPlayer sp, int type) {
            Side s = new Side();
            PlayerStats st = ServerEvents.get(sp);
            s.id = sp.getUUID();
            s.name = sp.getName().getString();
            s.base = statFor(st, type);
            s.hair = st.hairColor;
            s.skin = skinColor(st.skinTone);
            s.elo = MogData.get(server).entry(sp.getUUID(), s.name).elo;
            return s;
        }

        static Side npc(Npc npc, RandomSource rnd) {
            Side s = new Side();
            s.npc = npc;
            s.name = npc.name;
            s.base = npc.baseLo + rnd.nextDouble() * (npc.baseHi - npc.baseLo);
            s.hair = npc.hair;
            s.skin = npc.skin;
            s.elo = npc.elo();
            return s;
        }
    }

    public static double statFor(PlayerStats s, int type) {
        double v = switch (type) {
            case 0 -> s.facial;
            case 1 -> s.physique();
            case 2 -> s.grooming;
            case 3 -> s.posture;
            default -> s.overall;
        };
        return Math.max(0.0, Math.min(1.0, v / 100.0));
    }

    static int skinColor(float tone) {
        int r = (int) (0xF5 + (0x7A - 0xF5) * tone);
        int g = (int) (0xD5 + (0x4A - 0xD5) * tone);
        int b = (int) (0xB5 + (0x2A - 0xB5) * tone);
        return (r << 16) | (g << 8) | b;
    }

    static double accuracy(double elapsed, double target) {
        double p = (Math.max(0.0, elapsed) % 60.0) / 30.0;
        double frac = p <= 1.0 ? p : 2.0 - p;
        return Math.max(0.0, 1.0 - Math.abs(frac - target) / 0.25);
    }

    static boolean beats(int p, int q) {
        return (p == 0 && q == 2) || (p == 2 && q == 1) || (p == 1 && q == 0);
    }

    final MinecraftServer server;
    final int type;
    final boolean ranked;
    final Side[] side = new Side[2];
    public final Set<UUID> spectators = new HashSet<>();
    final RandomSource rnd = RandomSource.create();
    int phase = INTRO;
    int round = -1;
    int roundsDone = 0;
    int tick = 0;
    float target = 0.5f;
    int winner = -1;

    public MogBattle(MinecraftServer server, int type, boolean ranked, Side a, Side b) {
        this.server = server;
        this.type = type;
        this.ranked = ranked;
        side[0] = a;
        side[1] = b;
    }

    ServerPlayer player(Side s) {
        return s.id == null ? null : server.getPlayerList().getPlayer(s.id);
    }

    public int indexOf(UUID id) {
        for (int i = 0; i < 2; i++) if (id.equals(side[i].id)) return i;
        return -1;
    }

    public void begin() {
        sendState();
    }

    public void tick() {
        if (phase == DONE) return;
        for (int i = 0; i < 2; i++) {
            if (side[i].id != null && player(side[i]) == null) {
                finish(true, i);
                return;
            }
        }
        tick++;
        switch (phase) {
            case INTRO -> {
                if (tick >= 60) startRound();
            }
            case CHOOSE -> {
                for (Side s : side) {
                    if (s.npc != null && !s.locked && tick >= s.lockTick) {
                        s.locked = true;
                        s.acc = accuracy(s.lockTick, target);
                        sendState();
                    }
                }
                if ((side[0].locked && side[1].locked) || tick >= 300) resolveRound();
            }
            case RESULT -> {
                if (tick >= 80) {
                    if (round + 1 >= ROUNDS) finish(false, -1);
                    else startRound();
                }
            }
            default -> {
            }
        }
    }

    void startRound() {
        round++;
        phase = CHOOSE;
        tick = 0;
        target = 0.2f + rnd.nextFloat() * 0.6f;
        for (Side s : side) {
            s.pose = -1;
            s.locked = false;
            s.acc = 0;
            if (round > 0) s.stamina = Math.min(100, s.stamina + 15);
            if (s.npc != null) planNpc(s);
        }
        sendState();
    }

    void planNpc(Side s) {
        double[] w = s.npc.poseBias.clone();
        for (int i = 0; i < 3; i++) if (POSE_COST[i] > s.stamina && i != 2) w[i] = 0;
        double sum = w[0] + w[1] + w[2];
        double r = rnd.nextDouble() * sum;
        s.pose = r < w[0] ? 0 : (r < w[0] + w[1] ? 1 : 2);
        double skill = s.npc.skillLo + rnd.nextDouble() * (s.npc.skillHi - s.npc.skillLo);
        s.lockTick = Math.max(15, (int) Math.round(target * 30 + rnd.nextGaussian() * (1 - skill) * 10));
    }

    /** Called by the manager when a participant locks in. */
    public void lock(int idx, int pose, int latencyTicks) {
        if (phase != CHOOSE) return;
        Side s = side[idx];
        if (s.locked) return;
        pose = Math.max(0, Math.min(2, pose));
        if (POSE_COST[pose] > s.stamina) pose = 2;
        s.acc = accuracy(tick - latencyTicks, target);
        s.pose = pose;
        s.locked = true;
        sendState();
    }

    void resolveRound() {
        for (Side s : side) {
            if (!s.locked) {
                s.pose = 2;
                s.acc = 0;
            }
        }
        double[] sc = new double[2];
        for (int i = 0; i < 2; i++) {
            Side me = side[i], opp = side[1 - i];
            double mult = beats(me.pose, opp.pose) ? 1.25 : (beats(opp.pose, me.pose) ? 0.8 : 1.0);
            sc[i] = (me.base * 0.5 + me.acc * 0.5) * 100.0 * mult;
        }
        for (int i = 0; i < 2; i++) {
            Side me = side[i];
            me.scores[round] = (int) Math.round(sc[i]);
            me.poses[round] = me.pose;
            me.total += sc[i];
            me.accSum += me.acc;
            me.stamina = Math.max(0, me.stamina - POSE_COST[me.pose]);
            ServerPlayer sp = player(me);
            if (sp != null) {
                sp.swing(InteractionHand.MAIN_HAND, true);
                ParticleOptions pt = me.pose == 0 ? ParticleTypes.CRIT : (me.pose == 1 ? ParticleTypes.HEART : ParticleTypes.CLOUD);
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(pt, sp.getX(), sp.getY() + 1.4, sp.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
                }
                sp.level().playSound(null, sp.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 0.8f + me.pose * 0.3f);
            }
        }
        roundsDone = round + 1;
        phase = RESULT;
        tick = 0;
        sendState();
    }

    void finish(boolean forfeit, int forfeiter) {
        phase = DONE;
        if (forfeit) {
            winner = 1 - forfeiter;
        } else if (Math.abs(side[0].total - side[1].total) >= 0.5) {
            winner = side[0].total > side[1].total ? 0 : 1;
        } else if (Math.abs(side[0].accSum - side[1].accSum) >= 0.05) {
            winner = side[0].accSum > side[1].accSum ? 0 : 1;
        } else {
            winner = 2;
        }

        MogData data = MogData.get(server);
        boolean pvp = side[0].id != null && side[1].id != null;
        if (ranked && pvp) {
            double ea = 1.0 / (1.0 + Math.pow(10.0, (side[1].elo - side[0].elo) / 400.0));
            double score = winner == 0 ? 1.0 : (winner == 1 ? 0.0 : 0.5);
            int d = (int) Math.round(32.0 * (score - ea));
            side[0].eloDelta = d;
            side[1].eloDelta = -d;
        }

        for (int i = 0; i < 2; i++) {
            Side me = side[i], opp = side[1 - i];
            ServerPlayer sp = player(me);
            if (sp == null) continue;
            PlayerStats st = ServerEvents.get(sp);
            if (st == null) continue;
            int result = winner == 2 ? 2 : (winner == i ? 0 : 1);

            ServerEvents.rollDay(sp, st);
            double tierGain = opp.npc != null ? (opp.npc.tier == 1 ? 1.0 : opp.npc.tier == 2 ? 1.8 : 3.0) : 2.0;
            double base = result == 0 ? tierGain : (result == 2 ? tierGain * 0.5 : 0.4);
            if (forfeit && forfeiter == i) base = 0.0;
            double g = base * Config.TRAINING_SPEED.get() * Math.max(0.0, 1.0 - st.battle / 110.0);
            g = Math.min(g, Math.max(0.0, Config.DAILY_BATTLE_CAP.get() - st.dailyBattle));
            st.battle = Math.min(100.0, st.battle + g);
            st.dailyBattle += g;
            me.gain = g;
            ServerEvents.refresh(sp, st);

            MogData.Entry e = data.entry(sp.getUUID(), me.name);
            e.elo = me.elo + me.eloDelta;
            me.elo = e.elo;
            if (result == 0) {
                e.wins++;
                e.streak++;
                e.bestStreak = Math.max(e.bestStreak, e.streak);
            } else if (result == 1) {
                e.losses++;
                e.streak = 0;
            }
            e.rating = st.rating();

            if (sp.level() instanceof ServerLevel sl) {
                if (result == 0) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2, sp.getZ(), 50, 0.5, 0.8, 0.5, 0.3);
                    sp.level().playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9f, 1.0f);
                } else if (result == 1) {
                    sl.sendParticles(ParticleTypes.SMOKE, sp.getX(), sp.getY() + 1.2, sp.getZ(), 20, 0.4, 0.5, 0.4, 0.02);
                    sp.level().playSound(null, sp.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8f, 1.0f);
                }
            }
        }
        data.setDirty();

        sendState();
        Component msg = winner == 2
                ? Component.literal("Mog Arena: " + side[0].name + " and " + side[1].name + " drew the " + TYPES[type] + ". GG!")
                : Component.literal("Mog Arena: " + side[winner].name + " won the " + TYPES[type] + " vs "
                + side[1 - winner].name + " (" + Math.round(side[winner].total) + " - " + Math.round(side[1 - winner].total) + "). GG!");
        for (Side s : side) {
            ServerPlayer sp = player(s);
            if (sp != null) sp.sendSystemMessage(msg);
        }
        MogManager.end(this);
    }

    public void forfeit(int idx) {
        if (phase != DONE) finish(true, idx);
    }

    CompoundTag buildState() {
        CompoundTag t = new CompoundTag();
        t.putInt("phase", phase);
        t.putInt("type", type);
        t.putInt("round", round);
        t.putInt("rounds", ROUNDS);
        t.putInt("roundsDone", roundsDone);
        t.putFloat("target", target);
        t.putBoolean("ranked", ranked);
        t.putInt("winner", winner);
        for (int i = 0; i < 2; i++) {
            String c = i == 0 ? "A" : "B";
            Side s = side[i];
            t.putString("name" + c, s.name);
            t.putInt("hair" + c, s.hair);
            t.putInt("skin" + c, s.skin);
            if (s.id != null) t.putUUID("id" + c, s.id);
            t.putInt("stam" + c, s.stamina);
            t.putBoolean("lock" + c, s.locked);
            t.putInt("total" + c, (int) Math.round(s.total));
            t.putIntArray("poses" + c, s.poses);
            t.putIntArray("scores" + c, s.scores);
            t.putDouble("gain" + c, s.gain);
            t.putInt("eloDelta" + c, s.eloDelta);
            t.putInt("elo" + c, s.elo);
            if (s.npc != null) t.putString("blurb" + c, s.npc.blurb);
        }
        return t;
    }

    public void sendState() {
        CompoundTag base = buildState();
        for (int i = 0; i < 2; i++) {
            ServerPlayer sp = player(side[i]);
            if (sp != null) {
                CompoundTag t = base.copy();
                t.putInt("you", i);
                MogManager.send(sp, 1, t);
            }
        }
        for (UUID id : spectators) {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp != null) {
                CompoundTag t = base.copy();
                t.putInt("you", -1);
                MogManager.send(sp, 1, t);
            }
        }
    }
}
