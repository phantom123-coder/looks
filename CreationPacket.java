package com.looksmaxxing.net;

import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: character creation. Validated on the server; accepted only once. */
public class CreationPacket {
    public final float[] sliders;
    public final int hairStyle;
    public final int hairColor;
    public final float skin;

    public CreationPacket(float[] sliders, int hairStyle, int hairColor, float skin) {
        this.sliders = sliders;
        this.hairStyle = hairStyle;
        this.hairColor = hairColor;
        this.skin = skin;
    }

    public static void encode(CreationPacket m, FriendlyByteBuf b) {
        for (int i = 0; i < PlayerStats.SLIDER_COUNT; i++) b.writeFloat(m.sliders[i]);
        b.writeInt(m.hairStyle);
        b.writeInt(m.hairColor);
        b.writeFloat(m.skin);
    }

    public static CreationPacket decode(FriendlyByteBuf b) {
        float[] s = new float[PlayerStats.SLIDER_COUNT];
        for (int i = 0; i < s.length; i++) s[i] = b.readFloat();
        return new CreationPacket(s, b.readInt(), b.readInt(), b.readFloat());
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0.5f;
        return Math.max(0f, Math.min(1f, v));
    }

    public static void handle(CreationPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            PlayerStats s = ServerEvents.get(sp);
            if (s == null) return;
            long now = sp.level().getGameTime();
            if (s.created && now > s.mirrorUntil) return; // edits only right after using a mirror
            s.mirrorUntil = -1;
            for (int i = 0; i < PlayerStats.SLIDER_COUNT; i++) s.sliders[i] = clamp01(m.sliders[i]);
            s.hairStyle = Math.max(0, Math.min(5, m.hairStyle));
            s.hairColor = m.hairColor & 0xFFFFFF;
            s.skinTone = clamp01(m.skin);
            s.created = true;
            ServerEvents.refresh(sp, s);
        });
        c.setPacketHandled(true);
    }
}
