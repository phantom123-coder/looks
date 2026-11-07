package com.looksmaxxing.item;

import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProteinMealItem extends Item {
    public ProteinMealItem(Properties props) {
        super(props);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            PlayerStats s = ServerEvents.get(sp);
            if (s != null && s.created) {
                s.stamina = Math.min(100, s.stamina + 30);
                s.fatigue = Math.max(0, s.fatigue - 10);
                ServerEvents.refresh(sp, s);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+30 stamina, -10 fatigue"));
    }
}
