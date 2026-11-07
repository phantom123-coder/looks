package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.net.Net;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Looksmaxxing.MODID)
public class Looksmaxxing {
    public static final String MODID = "looksmaxxing";

    public Looksmaxxing() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerCaps);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }

    private void registerCaps(RegisterCapabilitiesEvent e) {
        e.register(PlayerStats.class);
    }
}
