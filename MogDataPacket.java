package com.looksmaxxing.net;

import com.looksmaxxing.client.ClientHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client. kind 0 = menu, 1 = battle state, 2 = leaderboard. */
public class MogDataPacket {
    public final int kind;
    public final CompoundTag tag;

    public MogDataPacket(int kind, CompoundTag tag) {
        this.kind = kind;
        this.tag = tag;
    }

    public static void encode(MogDataPacket m, FriendlyByteBuf buf) {
        buf.writeInt(m.kind);
        buf.writeNbt(m.tag);
    }

    public static MogDataPacket decode(FriendlyByteBuf buf) {
        int k = buf.readInt();
        CompoundTag t = buf.readNbt();
        return new MogDataPacket(k, t == null ? new CompoundTag() : t);
    }

    public static void handle(MogDataPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.onMogData(m.kind, m.tag)));
        ctx.get().setPacketHandled(true);
    }
}
