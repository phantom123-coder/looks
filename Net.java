package com.looksmaxxing.net;

import com.looksmaxxing.Looksmaxxing;
import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Net {
    private static final String VERSION = "4";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Looksmaxxing.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CH.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
        CH.registerMessage(id++, CreationPacket.class, CreationPacket::encode, CreationPacket::decode, CreationPacket::handle);
        CH.registerMessage(id++, AppearancePacket.class, AppearancePacket::encode, AppearancePacket::decode, AppearancePacket::handle);
        CH.registerMessage(id++, OpenMirrorPacket.class, OpenMirrorPacket::encode, OpenMirrorPacket::decode, OpenMirrorPacket::handle);
        CH.registerMessage(id++, MogActionPacket.class, MogActionPacket::encode, MogActionPacket::decode, MogActionPacket::handle);
        CH.registerMessage(id++, MogDataPacket.class, MogDataPacket::encode, MogDataPacket::decode, MogDataPacket::handle);
    }

    public static void sync(ServerPlayer sp) {
        PlayerStats s = ServerEvents.get(sp);
        if (s != null) {
            CH.send(PacketDistributor.PLAYER.with(() -> sp), new SyncPacket(s.save()));
        }
    }

    private static AppearancePacket appearanceOf(ServerPlayer sp) {
        PlayerStats s = ServerEvents.get(sp);
        if (s == null || !s.created) return null;
        float jawVis = Math.min(1f, s.sliders[0] * 0.7f + (float) (s.jaw / 100.0) * 0.3f);
        float chinVis = Math.min(1f, s.sliders[1] * 0.7f + (float) (s.chin / 100.0) * 0.3f);
        return new AppearancePacket(sp.getUUID(), s.sliders[6], s.sliders[5], (float) (s.strength / 100.0),
                jawVis, chinVis, s.sliders[2], s.sliders[4], s.hairStyle, s.hairColor);
    }

    /** Sends the player's body values to themselves and everyone who can see them. */
    public static void syncAppearance(ServerPlayer sp) {
        AppearancePacket p = appearanceOf(sp);
        if (p != null) {
            CH.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sp), p);
        }
    }

    public static void sendAppearanceTo(ServerPlayer target, ServerPlayer viewer) {
        AppearancePacket p = appearanceOf(target);
        if (p != null) {
            CH.send(PacketDistributor.PLAYER.with(() -> viewer), p);
        }
    }
}
