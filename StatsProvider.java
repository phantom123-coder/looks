package com.looksmaxxing.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerStats stats = new PlayerStats();
    private final LazyOptional<PlayerStats> optional = LazyOptional.of(() -> stats);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == StatsCapability.STATS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return stats.save();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        stats.load(nbt);
    }
}
