package com.looksmaxxing.client;

import com.looksmaxxing.Looksmaxxing;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Looksmaxxing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final KeyMapping PROFILE_KEY =
            new KeyMapping("key.looksmaxxing.profile", InputConstants.KEY_K, "key.categories.looksmaxxing");

    public static final KeyMapping MOG_KEY =
            new KeyMapping("key.looksmaxxing.mog", InputConstants.KEY_M, "key.categories.looksmaxxing");

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(PROFILE_KEY);
        e.register(MOG_KEY);
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(CosmeticLayer.LOCATION, CosmeticLayer::createLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (String type : new String[]{"default", "slim"}) {
            PlayerRenderer r = e.getSkin(type);
            if (r != null) {
                r.addLayer(new CosmeticLayer(r, e.getEntityModels().bakeLayer(CosmeticLayer.LOCATION)));
            }
        }
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("hud", Hud::render);
    }
}
