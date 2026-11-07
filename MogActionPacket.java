package com.looksmaxxing.net;

import com.looksmaxxing.mog.MogManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client -> server: every Mog Arena action. The server validates everything. */
public class MogActionPacket {
    public static final int MENU = 1, CHALLENGE = 2, NPC = 3, QUEUE_JOIN = 4, QUEUE_LEAVE = 5, LOCK = 6,
            FORFEIT = 7, ACCEPT = 8, DECLINE = 9, TOGGLE = 10, LEADERBOARD = 11, LEAVE_SPECTATE = 13;

    public final int action, a, b;
    public final UUID id;

    public MogActionPacket(int action, int a, int b, UUID id) {
        this.action = action;
        this.a = a;
        this.b = b;
        this.id = id == null ? new UUID(0L, 0L) : id;
    }

    public MogActionPacket(int action) {
        this(action, 0, 0, null);
    }

    public static void encode(MogActionPacket m, FriendlyByteBuf buf) {
        buf.writeInt(m.action);
        buf.writeInt(m.a);
        buf.writeInt(m.b);
        buf.writeUUID(m.id);
    }

    public static MogActionPacket decode(FriendlyByteBuf buf) {
        return new MogActionPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readUUID());
    }

    public static void handle(MogActionPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            switch (m.action) {
                case MENU -> MogManager.sendMenu(sp);
                case CHALLENGE -> MogManager.challenge(sp, m.id, m.a);
                case NPC -> MogManager.startNpc(sp, m.a, m.b);
                case QUEUE_JOIN -> MogManager.queueJoin(sp, m.a);
                case QUEUE_LEAVE -> MogManager.queueLeave(sp);
                case LOCK -> MogManager.lock(sp, m.a);
                case FORFEIT -> MogManager.forfeit(sp);
                case ACCEPT -> MogManager.accept(sp);
                case DECLINE -> MogManager.decline(sp);
                case TOGGLE -> MogManager.toggle(sp);
                case LEADERBOARD -> MogManager.sendLeaderboard(sp);
                case LEAVE_SPECTATE -> MogManager.leaveSpectate(sp);
                default -> {
                }
            }
        });
        c.setPacketHandled(true);
    }
}
