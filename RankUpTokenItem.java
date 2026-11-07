package com.looksmaxxing.item;

import com.looksmaxxing.ServerEvents;
import com.looksmaxxing.data.PlayerStats;
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

public class RankUpTokenItem extends Item {
    public RankUpTokenItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerStats s = ServerEvents.get(sp);
            if (s == null || !s.created) {
                sp.displayClientMessage(Component.literal("Create your character first."), true);
                return InteractionResultHolder.fail(stack);
            }
            s.facial = Math.min(100, s.facial + 3);
            s.grooming = Math.min(100, s.grooming + 3);
            s.strength = Math.min(100, s.strength + 3);
            s.endurance = Math.min(100, s.endurance + 3);
            s.posture = Math.min(100, s.posture + 3);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            ServerEvents.refresh(sp, s);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+3 to every attribute (testing item in Stage 1)"));
    }
}
