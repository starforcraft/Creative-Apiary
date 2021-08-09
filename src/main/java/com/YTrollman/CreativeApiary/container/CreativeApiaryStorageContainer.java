package com.YTrollman.CreativeApiary.container;

import com.YTrollman.CreativeApiary.registry.ModContainers;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryStorageTileEntity;
import com.resourcefulbees.resourcefulbees.container.ContainerWithStackMove;
import com.resourcefulbees.resourcefulbees.container.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CreativeApiaryStorageContainer extends ContainerWithStackMove {

    private final CreativeApiaryStorageTileEntity apiaryStorageTileEntity;
    private final PlayerInventory playerInventory;
    private int numberOfSlots;
    private boolean rebuild;

    public CreativeApiaryStorageContainer(int id, World world, BlockPos pos, PlayerInventory inv) {
        super(ModContainers.CREATIVE_APIARY_STORAGE_CONTAINER.get(), id);
        this.playerInventory = inv;
        this.apiaryStorageTileEntity = (CreativeApiaryStorageTileEntity) world.getBlockEntity(pos);
        setupSlots(false);
    }

    /**
     * Determines whether supplied player can use this container
     *
     * @param player the player
     */
    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        return true;
    }


    public void setupSlots(boolean rebuild) {
        if (getApiaryStorageTileEntity() != null) {
            this.slots.clear();
            numberOfSlots = getApiaryStorageTileEntity().getNumberOfSlots();

            int rows;
            if (getNumberOfSlots() != 108) {
                rows = getNumberOfSlots() / 9;
                for (int r = 0; r < rows; ++r) {
                    for (int c = 0; c < 9; ++c) {
                        this.addSlot(new OutputSlot(getApiaryStorageTileEntity().getItemStackHandler(), c + r * 9 + 1, 26 + 8 + c * 18, 18 + r * 18));
                    }
                }
            } else {
                rows = 9;
                for (int r = 0; r < 9; ++r) {
                    for (int c = 0; c < 12; ++c) {
                        this.addSlot(new OutputSlot(getApiaryStorageTileEntity().getItemStackHandler(), c + r * 12 + 1, 26 + 8 + c * 18, 18 + r * 18));
                    }
                }
            }

            int invX = getNumberOfSlots() == 108 ? 35 : 8;

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(getPlayerInventory(), j + i * 9 + 9, 26 + invX + j * 18, 32 + (rows * 18) + i * 18));
                }
            }

            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(getPlayerInventory(), k, 26 + invX + k * 18, 90 + rows * 18));
            }

            this.setRebuild(rebuild);
        }
    }

    @Override
    public int getContainerInputEnd() {
        return 1;
    }

    @Override
    public int getInventoryStart() {
        return 1 + getNumberOfSlots();
    }

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public CreativeApiaryStorageTileEntity getApiaryStorageTileEntity() {
        return apiaryStorageTileEntity;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }
}
