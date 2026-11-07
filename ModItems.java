package com.looksmaxxing;

import com.looksmaxxing.item.GroomingItem;
import com.looksmaxxing.item.HammerItem;
import com.looksmaxxing.item.MogInvitationItem;
import com.looksmaxxing.item.ProteinMealItem;
import com.looksmaxxing.item.RankUpTokenItem;
import com.looksmaxxing.item.TrainingItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Looksmaxxing.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Looksmaxxing.MODID);

    public static final RegistryObject<Item> RANK_UP_TOKEN = ITEMS.register("rank_up_token",
            () -> new RankUpTokenItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DUMBBELLS = ITEMS.register("resistance_dumbbells",
            () -> new TrainingItem(new Item.Properties().stacksTo(1), 0.6, 8, 3, 0.0));

    public static final RegistryObject<Item> BARBELL = ITEMS.register("training_barbell",
            () -> new TrainingItem(new Item.Properties().stacksTo(1), 1.2, 15, 5, 0.1));

    public static final RegistryObject<Item> PROTEIN_MEAL = ITEMS.register("protein_meal",
            () -> new ProteinMealItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(8).saturationMod(0.8f).alwaysEat().build())));

    public static final RegistryObject<Item> PULL_UP_BAR = ITEMS.register("pull_up_bar",
            () -> new BlockItem(ModBlocks.PULL_UP_BAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> HAMMER = ITEMS.register("jawline_training_hammer",
            () -> new HammerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GROOMING_KIT = ITEMS.register("grooming_kit",
            () -> new GroomingItem(new Item.Properties().durability(64), 0.8, 0.0));

    public static final RegistryObject<Item> SKINCARE_KIT = ITEMS.register("skincare_kit",
            () -> new GroomingItem(new Item.Properties().durability(64), 0.5, 0.15));

    public static final RegistryObject<Item> DUMMY = ITEMS.register("enchanted_training_dummy",
            () -> new BlockItem(ModBlocks.TRAINING_DUMMY.get(), new Item.Properties()));

    public static final RegistryObject<Item> MIRROR = ITEMS.register("full_length_mirror",
            () -> new BlockItem(ModBlocks.MIRROR.get(), new Item.Properties()));

    public static final RegistryObject<Item> MOG_ARENA = ITEMS.register("mog_arena_platform",
            () -> new BlockItem(ModBlocks.MOG_ARENA.get(), new Item.Properties()));

    public static final RegistryObject<Item> INVITATION = ITEMS.register("mog_battle_invitation",
            () -> new MogInvitationItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.looksmaxxing"))
                    .icon(() -> new ItemStack(RANK_UP_TOKEN.get()))
                    .displayItems((params, output) -> {
                        output.accept(RANK_UP_TOKEN.get());
                        output.accept(DUMBBELLS.get());
                        output.accept(BARBELL.get());
                        output.accept(PULL_UP_BAR.get());
                        output.accept(PROTEIN_MEAL.get());
                        output.accept(HAMMER.get());
                        output.accept(DUMMY.get());
                        output.accept(MIRROR.get());
                        output.accept(GROOMING_KIT.get());
                        output.accept(SKINCARE_KIT.get());
                        output.accept(MOG_ARENA.get());
                        output.accept(INVITATION.get());
                    })
                    .build());
}
