package com.YTrollman.CreativeApiary.registry;

import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryStorageTileEntity;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;
import com.resourcefulbees.resourcefulbees.ResourcefulBees;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntityTypes {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ResourcefulBees.MOD_ID);

    public static final RegistryObject<TileEntityType<?>> CREATIVE_APIARY_TILE_ENTITY = TILE_ENTITY_TYPES.register("creative_apiary", () -> TileEntityType.Builder
            .of(CreativeApiaryTileEntity::new, ModBlocks.TCREATIVE_APIARY_BLOCK.get())
            .build(null));
}
