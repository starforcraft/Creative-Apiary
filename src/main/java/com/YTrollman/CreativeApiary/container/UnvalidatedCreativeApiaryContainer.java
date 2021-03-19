package com.YTrollman.CreativeApiary.container;

import javax.annotation.Nonnull;

import com.YTrollman.CreativeApiary.registry.ModContainers;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnvalidatedCreativeApiaryContainer extends Container {

    private final CreativeApiaryTileEntity apiaryTileEntity;
    private final BlockPos pos;

    public UnvalidatedCreativeApiaryContainer(int id, World world, BlockPos pos, PlayerInventory inv) {
        super(ModContainers.UNVALIDATED_CREATIVE_APIARY_CONTAINER.get(), id);
        this.pos = pos;
        this.apiaryTileEntity =(CreativeApiaryTileEntity)world.getBlockEntity(pos);
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity playerIn) {
        return true;
    }


    public CreativeApiaryTileEntity getApiaryTileEntity() {
        return apiaryTileEntity;
    }

    public BlockPos getPos() {
        return pos;
    }
}
