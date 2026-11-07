package com.looksmaxxing.client;

import com.looksmaxxing.Looksmaxxing;
import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.net.MogActionPacket;
import com.looksmaxxing.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Looksmaxxing.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (ClientHandler.synced && !ClientHandler.stats.created && mc.screen == null) {
            mc.setScreen(new CreationScreen());
        }
        while (ClientModEvents.MOG_KEY.consumeClick()) {
            if (mc.screen == null && ClientHandler.stats.created) {
                Net.CH.sendToServer(new MogActionPacket(MogActionPacket.MENU));
            }
        }
        while (ClientModEvents.PROFILE_KEY.consumeClick()) {
            if (mc.screen == null && ClientHandler.stats.created) {
                mc.setScreen(new ProfileScreen());
            }
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientHandler.synced = false;
        ClientHandler.stats = new PlayerStats();
        ClientHandler.look.clear();
        ClientHandler.battleState = new net.minecraft.nbt.CompoundTag();
    }

    // --- visible body changes: height, frame width and muscle bulk ---
    @SubscribeEvent
    public static void renderPre(RenderPlayerEvent.Pre e) {
        ClientHandler.Look a = ClientHandler.look.get(e.getEntity().getUUID());
        if (a == null) return;
        float h = 0.92f + a.height * 0.16f;
        float w = (0.94f + a.frame * 0.12f) * (1.0f + a.muscle * 0.10f);
        e.getPoseStack().pushPose();
        e.getPoseStack().scale(w, h, w);
    }

    @SubscribeEvent
    public static void renderPost(RenderPlayerEvent.Post e) {
        if (ClientHandler.look.containsKey(e.getEntity().getUUID())) {
            e.getPoseStack().popPose();
        }
    }
}
