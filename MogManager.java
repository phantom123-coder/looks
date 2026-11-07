package com.looksmaxxing.mog;

import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.net.MogDataPacket;
import com.looksmaxxing.net.Net;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Challenges, ranked queue, spectating and leaderboard. Everything is decided on the server. */
public class MogManager {
    static final Set<MogBattle> battles = new LinkedHashSet<>();
    static final Map<UUID, MogBattle> byPlayer = new HashMap<>();
    static final Map<UUID, MogBattle> spectating = new HashMap<>();

    static class Invite {
        UUID from;
        int type;
        long expire;
    }

    static class QueueEntry {
        UUID id;
        int type;
        long since;
    }

    static final Map<UUID, Invite> invites = new HashMap<>();
    static final List<QueueEntry> queue = new ArrayList<>();
    static long ticks = 0;

    public static void send(ServerPlayer sp, int kind, CompoundTag tag) {
        Net.CH.send(PacketDistributor.PLAYER.with(() -> sp), new MogDataPacket(kind, tag));
    }

    private static void msg(ServerPlayer sp, String text) {
        sp.displayClientMessage(Component.literal(text), true);
    }

    public static void tick(MinecraftServer server) {
        ticks++;
        invites.values().removeIf(i -> ticks > i.expire);
        for (MogBattle b : new ArrayList<>(battles)) b.tick();
        if (ticks % 20 == 0) matchmake(server);
    }

    static boolean inQueue(UUID id) {
        for (QueueEntry q : queue) if (q.id.equals(id)) return true;
        return false;
    }

    static void leaveQueue(UUID id) {
        queue.removeIf(q -> q.id.equals(id));
    }

    static boolean ready(ServerPlayer sp) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) {
            msg(sp, "Create your character first.");
            return false;
        }
        if (byPlayer.containsKey(sp.getUUID())) {
            msg(sp, "You are already in a battle.");
            return false;
        }
        return true;
    }

    static void register(MogBattle b) {
        battles.add(b);
        for (MogBattle.Side s : b.side) {
            if (s.id != null) {
                byPlayer.put(s.id, b);
                leaveQueue(s.id);
                invites.remove(s.id);
            }
        }
        b.begin();
    }

    static void end(MogBattle b) {
        battles.remove(b);
        for (MogBattle.Side s : b.side) {
            if (s.id != null) byPlayer.remove(s.id);
        }
        for (UUID id : b.spectators) spectating.remove(id);
    }

    public static void startNpc(ServerPlayer sp, int type, int tier) {
        if (!ready(sp)) return;
        if (inQueue(sp.getUUID())) {
            msg(sp, "Leave the ranked queue first.");
            return;
        }
        type = Math.max(0, Math.min(4, type));
        tier = Math.max(1, Math.min(3, tier));
        MinecraftServer server = sp.server;
        MogBattle b = new MogBattle(server, type, false, MogBattle.Side.player(server, sp, type),
                MogBattle.Side.npc(Npc.pick(tier, sp.getRandom()), sp.getRandom()));
        register(b);
    }

    public static void challenge(ServerPlayer from, UUID targetId, int type) {
        if (!ready(from)) return;
        ServerPlayer target = from.server.getPlayerList().getPlayer(targetId);
        if (target == null || target == from) {
            msg(from, "That player is not available.");
            return;
        }
        PlayerStats ts = ServerEvents.get(target);
        if (ts == null || !ts.created || !ts.challengesOn) {
            msg(from, target.getName().getString() + " is not taking challenges right now.");
            return;
        }
        if (byPlayer.containsKey(targetId)) {
            msg(from, target.getName().getString() + " is busy in a battle.");
            return;
        }
        type = Math.max(0, Math.min(4, type));
        Invite inv = new Invite();
        inv.from = from.getUUID();
        inv.type = type;
        inv.expire = ticks + 1200;
        invites.put(targetId, inv);
        leaveQueue(from.getUUID());

        MutableComponent text = Component.literal(from.getName().getString() + " challenges you to a " + MogBattle.TYPES[type] + " mog battle!  ");
        text.append(Component.literal("[Accept]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mog accept"))));
        text.append(Component.literal(" "));
        text.append(Component.literal("[Decline]").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mog decline"))));
        target.sendSystemMessage(text);
        msg(from, "Challenge sent to " + target.getName().getString() + " (expires in 60s).");
    }

    public static void accept(ServerPlayer sp) {
        Invite inv = invites.remove(sp.getUUID());
        if (inv == null || ticks > inv.expire) {
            msg(sp, "No active challenge.");
            return;
        }
        ServerPlayer from = sp.server.getPlayerList().getPlayer(inv.from);
        if (from == null || byPlayer.containsKey(from.getUUID())) {
            msg(sp, "Your opponent is no longer available.");
            return;
        }
        if (!ready(sp)) return;
        MinecraftServer server = sp.server;
        MogBattle b = new MogBattle(server, inv.type, false, MogBattle.Side.player(server, from, inv.type),
                MogBattle.Side.player(server, sp, inv.type));
        register(b);
    }

    public static void decline(ServerPlayer sp) {
        Invite inv = invites.remove(sp.getUUID());
        if (inv == null) {
            msg(sp, "No active challenge.");
            return;
        }
        ServerPlayer from = sp.server.getPlayerList().getPlayer(inv.from);
        if (from != null) msg(from, sp.getName().getString() + " politely passed on the battle.");
        msg(sp, "Challenge declined.");
    }

    public static void toggle(ServerPlayer sp) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null) return;
        s.challengesOn = !s.challengesOn;
        if (!s.challengesOn) {
            leaveQueue(sp.getUUID());
            invites.remove(sp.getUUID());
        }
        ServerEvents.refresh(sp, s);
        msg(sp, "Challenges: " + (s.challengesOn ? "ON" : "OFF"));
        sendMenu(sp);
    }

    public static void queueJoin(ServerPlayer sp, int type) {
        if (!ready(sp)) return;
        PlayerStats s = ServerEvents.get(sp);
        if (!s.challengesOn) {
            msg(sp, "Turn challenges ON to play ranked.");
            return;
        }
        if (!inQueue(sp.getUUID())) {
            QueueEntry q = new QueueEntry();
            q.id = sp.getUUID();
            q.type = Math.max(0, Math.min(4, type));
            q.since = ticks;
            queue.add(q);
        }
        msg(sp, "Searching for a ranked opponent...");
        sendMenu(sp);
    }

    public static void queueLeave(ServerPlayer sp) {
        leaveQueue(sp.getUUID());
        msg(sp, "Left the ranked queue.");
        sendMenu(sp);
    }

    static void matchmake(MinecraftServer server) {
        for (int i = 0; i < queue.size(); i++) {
            QueueEntry a = queue.get(i);
            for (int j = i + 1; j < queue.size(); j++) {
                QueueEntry b = queue.get(j);
                if (a.type != b.type) continue;
                ServerPlayer pa = server.getPlayerList().getPlayer(a.id);
                ServerPlayer pb = server.getPlayerList().getPlayer(b.id);
                if (pa == null || pb == null) continue;
                int eloA = MogData.get(server).entry(a.id, null).elo;
                int eloB = MogData.get(server).entry(b.id, null).elo;
                long waited = (ticks - Math.min(a.since, b.since)) / 20;
                if (Math.abs(eloA - eloB) <= 200 + waited * 10) {
                    queue.remove(j);
                    queue.remove(i);
                    MogBattle battle = new MogBattle(server, a.type, true,
                            MogBattle.Side.player(server, pa, a.type), MogBattle.Side.player(server, pb, a.type));
                    register(battle);
                    return; // queue changed, continue next second
                }
            }
        }
    }

    public static void lock(ServerPlayer sp, int pose) {
        MogBattle b = byPlayer.get(sp.getUUID());
        if (b == null) return;
        int idx = b.indexOf(sp.getUUID());
        if (idx < 0) return;
        b.lock(idx, pose, sp.latency / 50);
    }

    public static void forfeit(ServerPlayer sp) {
        MogBattle b = byPlayer.get(sp.getUUID());
        if (b == null) return;
        int idx = b.indexOf(sp.getUUID());
        if (idx >= 0) b.forfeit(idx);
    }

    public static void spectate(ServerPlayer sp, ServerPlayer target) {
        MogBattle b = byPlayer.get(target.getUUID());
        if (b == null) {
            msg(sp, target.getName().getString() + " is not in a battle.");
            return;
        }
        if (byPlayer.containsKey(sp.getUUID())) {
            msg(sp, "You are in a battle yourself.");
            return;
        }
        b.spectators.add(sp.getUUID());
        spectating.put(sp.getUUID(), b);
        b.sendState();
    }

    public static void leaveSpectate(ServerPlayer sp) {
        MogBattle b = spectating.remove(sp.getUUID());
        if (b != null) b.spectators.remove(sp.getUUID());
    }

    public static void onLogout(ServerPlayer sp) {
        UUID id = sp.getUUID();
        leaveQueue(id);
        invites.remove(id);
        leaveSpectate(sp);
        MogBattle b = byPlayer.get(id);
        if (b != null) {
            int idx = b.indexOf(id);
            if (idx >= 0) b.forfeit(idx);
        }
    }

    public static void sendMenu(ServerPlayer sp) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) {
            msg(sp, "Create your character first.");
            return;
        }
        MogData data = MogData.get(sp.server);
        MogData.Entry me = data.entry(sp.getUUID(), sp.getName().getString());
        CompoundTag t = new CompoundTag();
        t.putInt("elo", me.elo);
        t.putInt("wins", me.wins);
        t.putInt("losses", me.losses);
        t.putInt("streak", me.streak);
        t.putBoolean("challengesOn", s.challengesOn);
        t.putBoolean("inQueue", inQueue(sp.getUUID()));
        ListTag list = new ListTag();
        for (ServerPlayer other : sp.server.getPlayerList().getPlayers()) {
            if (other == sp) continue;
            PlayerStats os = ServerEvents.get(other);
            if (os == null || !os.created || !os.challengesOn) continue;
            CompoundTag e = new CompoundTag();
            e.putUUID("id", other.getUUID());
            e.putString("name", other.getName().getString());
            e.putInt("elo", data.entry(other.getUUID(), other.getName().getString()).elo);
            e.putDouble("rating", os.rating());
            list.add(e);
        }
        t.put("players", list);
        send(sp, 0, t);
    }

    public static void sendLeaderboard(ServerPlayer sp) {
        MogData data = MogData.get(sp.server);
        List<Map.Entry<UUID, MogData.Entry>> all = new ArrayList<>(data.map.entrySet());
        all.sort((x, y) -> Integer.compare(y.getValue().elo, x.getValue().elo));
        ListTag list = new ListTag();
        int rank = 0;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getKey().equals(sp.getUUID())) rank = i + 1;
            if (i >= 10) continue;
            MogData.Entry e = all.get(i).getValue();
            CompoundTag c = new CompoundTag();
            c.putString("name", e.name);
            c.putInt("elo", e.elo);
            c.putInt("wins", e.wins);
            c.putInt("losses", e.losses);
            c.putDouble("rating", e.rating);
            list.add(c);
        }
        CompoundTag t = new CompoundTag();
        t.put("entries", list);
        t.putInt("yourRank", rank);
        t.putInt("total", all.size());
        send(sp, 2, t);
    }
}
