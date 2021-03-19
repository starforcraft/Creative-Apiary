package com.YTrollman.CreativeApiary.registry;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.YTrollman.CreativeApiary.container.CreativeApiaryStorageContainer;
import com.YTrollman.CreativeApiary.container.UnvalidatedCreativeApiaryContainer;
import com.YTrollman.CreativeApiary.container.ValidatedCreativeApiaryContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, CreativeApiary.MOD_ID);

    public static final RegistryObject<ContainerType<UnvalidatedCreativeApiaryContainer>> UNVALIDATED_CREATIVE_APIARY_CONTAINER = CONTAINER_TYPES.register("unvalidated_creative_apiary", () -> IForgeContainerType
            .create((id, inv, c) -> new UnvalidatedCreativeApiaryContainer(id, inv.player.level, c.readBlockPos(), inv)));
    public static final RegistryObject<ContainerType<ValidatedCreativeApiaryContainer>> VALIDATED_CREATIVE_APIARY_CONTAINER = CONTAINER_TYPES.register("validated_creative_apiary", () -> IForgeContainerType
            .create((id, inv, c) -> new ValidatedCreativeApiaryContainer(id, inv.player.level, c.readBlockPos(), inv)));
    public static final RegistryObject<ContainerType<CreativeApiaryStorageContainer>> CREATIVE_APIARY_STORAGE_CONTAINER = CONTAINER_TYPES.register("creative_apiary_storage", () -> IForgeContainerType
            .create((id, inv, c) -> new CreativeApiaryStorageContainer(id, inv.player.level, c.readBlockPos(), inv)));
}
