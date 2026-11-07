package com.looksmaxxing.mog;

import com.looksmaxxing.Looksmaxxing;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Looksmaxxing.MODID)
public class MogCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("mog")
                .then(Commands.literal("accept").executes(c -> {
                    MogManager.accept(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("decline").executes(c -> {
                    MogManager.decline(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("toggle").executes(c -> {
                    MogManager.toggle(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("leaderboard").executes(c -> {
                    MogManager.sendLeaderboard(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("menu").executes(c -> {
                    MogManager.sendMenu(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("spectate").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    MogManager.spectate(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "player"));
                    return 1;
                }))));
    }
}
