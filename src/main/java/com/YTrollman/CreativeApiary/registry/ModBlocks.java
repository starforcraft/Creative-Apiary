package com.YTrollman.CreativeApiary.registry;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.YTrollman.CreativeApiary.block.CreativeApiaryBlock;
import com.YTrollman.CreativeApiary.block.CreativeApiaryBreederBlock;
import com.YTrollman.CreativeApiary.block.CreativeApiaryStorageBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeApiary.MOD_ID);
    
    private static final AbstractBlock.Properties NEST_PROPERTIES = AbstractBlock.Properties.of(Material.WOOD).strength(1F).sound(SoundType.WOOD);
    
    public static final RegistryObject<Block> TCREATIVE_APIARY_BLOCK = BLOCKS.register("tcreative_apiary", () -> new CreativeApiaryBlock(100, 15, 30));
    public static final RegistryObject<Block> CREATIVE_APIARY_STORAGE_BLOCK = BLOCKS.register("creative_apiary_storage", () -> new CreativeApiaryStorageBlock(NEST_PROPERTIES));
    public static final RegistryObject<Block> CREATIVE_APIARY_BREEDER_BLOCK = BLOCKS.register("creative_apiary_breeder", () -> new CreativeApiaryBreederBlock(NEST_PROPERTIES));
}