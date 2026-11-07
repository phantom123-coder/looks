package com.looksmaxxing.item;

import com.looksmaxxing.Training;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Hold right-click to do a set of reps (arm swings), then it trains strength. */
public class TrainingItem extends Item {
    private final double baseGain, staminaCost, fatigueCost, postureGain;

    public TrainingItem(Properties props, double baseGain, double staminaCost, double fatigueCost, double postureGain) {
        super(props);
        this.baseGain = baseGain;
        this.staminaCost = staminaCost;
        this.fatigueCost = fatigueCost;
        this.postureGain = postureGain;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide) return;
        int elapsed = getUseDuration(stack) - remaining;
        if (elapsed > 0 && elapsed % 10 == 0) {
            entity.swing(entity.getUsedItemHand(), true); // visible rep for everyone nearby
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            if (Training.perform(sp, sp.getUsedItemHand(), baseGain, staminaCost, fatigueCost, postureGain)) {
                sp.getCooldowns().addCooldown(this, 20);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Hold right-click to train strength"));
        tooltip.add(Component.literal("Stamina cost: " + (int) staminaCost));
    }
}
