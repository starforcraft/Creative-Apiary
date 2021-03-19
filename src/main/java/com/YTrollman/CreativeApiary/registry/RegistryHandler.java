package com.YTrollman.CreativeApiary.registry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RegistryHandler {

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModContainers.CONTAINER_TYPES.register(bus);
        ModTileEntityTypes.TILE_ENTITY_TYPES.register(bus);
	}
}
