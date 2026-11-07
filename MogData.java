package com.looksmaxxing.mog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent per-world Mog Arena data: ELO, wins/losses, streaks. Used for leaderboards. */
public class MogData extends SavedData {
    public static class Entry {
        public String name = "?";
        public int elo = 1000;
        public int wins, losses, streak, bestStreak;
        public double rating = 1.0;
    }

    public final Map<UUID, Entry> map = new HashMap<>();

    public static MogData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(MogData::load, MogData::new, "looksmaxxing_mog");
    }

    public Entry entry(UUID id, String name) {
        Entry e = map.computeIfAbsent(id, k -> new Entry());
        if (name != null) e.name = name;
        return e;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Entry> en : map.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", en.getKey());
            Entry e = en.getValue();
            t.putString("name", e.name);
            t.putInt("elo", e.elo);
            t.putInt("wins", e.wins);
            t.putInt("losses", e.losses);
            t.putInt("streak", e.streak);
            t.putInt("bestStreak", e.bestStreak);
            t.putDouble("rating", e.rating);
            list.add(t);
        }
        tag.put("entries", list);
        return tag;
    }

    public static MogData load(CompoundTag tag) {
        MogData d = new MogData();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Entry e = new Entry();
            e.name = t.getString("name");
            e.elo = t.getInt("elo");
            e.wins = t.getInt("wins");
            e.losses = t.getInt("losses");
            e.streak = t.getInt("streak");
            e.bestStreak = t.getInt("bestStreak");
            e.rating = t.getDouble("rating");
            d.map.put(t.getUUID("id"), e);
        }
        return d;
    }
}
