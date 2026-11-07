package com.looksmaxxing.item;

import com.looksmaxxing.FacialTraining;
import com.looksmaxxing.ModBlocks;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Jawline Training Hammer: hold right-click while aiming at an Enchanted Training Dummy. */
public class HammerItem extends Item {
    public HammerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (!level.getBlockState(hit.getBlockPos()).is(ModBlocks.TRAINING_DUMMY.get())) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Aim at an Enchanted Training Dummy."), true);
            }
            return InteractionResultHolder.fail(stack);
        }
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
            entity.swing(entity.getUsedItemHand(), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            if (FacialTraining.perform(sp, sp.getUsedItemHand())) {
                sp.getCooldowns().addCooldown(this, 40);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Magical training ritual: hit the Enchanted Training Dummy"));
        tooltip.add(Component.literal("Never harms anyone. Limited sessions per day."));
    }
}
