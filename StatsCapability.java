package com.looksmaxxing.data;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class StatsCapability {
    public static final Capability<PlayerStats> STATS = CapabilityManager.get(new CapabilityToken<PlayerStats>() {});
}
