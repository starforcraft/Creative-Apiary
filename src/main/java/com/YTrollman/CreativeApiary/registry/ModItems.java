package com.YTrollman.CreativeApiary.registry;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.resourcefulbees.resourcefulbees.registry.ItemGroupResourcefulBees;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeApiary.MOD_ID);

    public static final RegistryObject<Item> TCREATIVE_APIARY_ITEM = ITEMS.register("tcreative_apiary", () -> new BlockItem(ModBlocks.TCREATIVE_APIARY_BLOCK.get(), new Item.Properties().tab(ItemGroupResourcefulBees.RESOURCEFUL_BEES)));
    public static final RegistryObject<Item> CREATIVE_APIARY_STORAGE_ITEM = ITEMS.register("creative_apiary_storage", () -> new BlockItem(ModBlocks.CREATIVE_APIARY_STORAGE_BLOCK.get(), new Item.Properties().tab(ItemGroupResourcefulBees.RESOURCEFUL_BEES)));
    public static final RegistryObject<Item> CREATIVE_APIARY_BREEDER_ITEM = ITEMS.register("creative_apiary_breeder", () -> new BlockItem(ModBlocks.CREATIVE_APIARY_BREEDER_BLOCK.get(), new Item.Properties().tab(ItemGroupResourcefulBees.RESOURCEFUL_BEES)));
}