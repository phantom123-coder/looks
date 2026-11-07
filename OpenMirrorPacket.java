package com.looksmaxxing.net;

import com.looksmaxxing.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: open the appearance editor (player used a mirror). */
public class OpenMirrorPacket {
    public static void encode(OpenMirrorPacket m, FriendlyByteBuf b) {
    }

    public static OpenMirrorPacket decode(FriendlyByteBuf b) {
        return new OpenMirrorPacket();
    }

    public static void handle(OpenMirrorPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.openMirror()));
        ctx.get().setPacketHandled(true);
    }
}
