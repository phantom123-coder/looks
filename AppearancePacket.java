package com.looksmaxxing.net;

import com.looksmaxxing.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> clients tracking a player: everything needed to render that player's body, face and hair. */
public class AppearancePacket {
    public final UUID id;
    public final float height, frame, muscle, jaw, chin, cheek, nose;
    public final int hairStyle, hairColor;

    public AppearancePacket(UUID id, float height, float frame, float muscle, float jaw, float chin,
                            float cheek, float nose, int hairStyle, int hairColor) {
        this.id = id;
        this.height = height;
        this.frame = frame;
        this.muscle = muscle;
        this.jaw = jaw;
        this.chin = chin;
        this.cheek = cheek;
        this.nose = nose;
        this.hairStyle = hairStyle;
        this.hairColor = hairColor;
    }

    public static void encode(AppearancePacket m, FriendlyByteBuf b) {
        b.writeUUID(m.id);
        b.writeFloat(m.height);
        b.writeFloat(m.frame);
        b.writeFloat(m.muscle);
        b.writeFloat(m.jaw);
        b.writeFloat(m.chin);
        b.writeFloat(m.cheek);
        b.writeFloat(m.nose);
        b.writeInt(m.hairStyle);
        b.writeInt(m.hairColor);
    }

    public static AppearancePacket decode(FriendlyByteBuf b) {
        return new AppearancePacket(b.readUUID(), b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat(),
                b.readFloat(), b.readFloat(), b.readFloat(), b.readInt(), b.readInt());
    }

    public static void handle(AppearancePacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setLook(m.id,
                        new ClientHandler.Look(m.height, m.frame, m.muscle, m.jaw, m.chin, m.cheek, m.nose, m.hairStyle, m.hairColor))));
        ctx.get().setPacketHandled(true);
    }
}
