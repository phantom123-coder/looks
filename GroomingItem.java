package com.looksmaxxing.item;

import com.looksmaxxing.Grooming;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Grooming kit / skincare kit: right-click to use, with durability. */
public class GroomingItem extends Item {
    private final double groomGain, facialGain;

    public GroomingItem(Properties props, double groomGain, double facialGain) {
        super(props);
        this.groomGain = groomGain;
        this.facialGain = facialGain;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (Grooming.useKit(sp, hand, groomGain, facialGain)) {
                stack.hurtAndBreak(1, sp, p -> p.broadcastBreakEvent(hand));
                sp.getCooldowns().addCooldown(this, 40);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to groom (daily limit applies)"));
    }
}
