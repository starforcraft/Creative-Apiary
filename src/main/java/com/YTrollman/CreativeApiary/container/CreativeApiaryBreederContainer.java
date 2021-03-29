package com.YTrollman.CreativeApiary.container;

import com.YTrollman.CreativeApiary.registry.ModContainers;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryBreederTileEntity;
import com.resourcefulbees.resourcefulbees.container.SlotItemHandlerUnconditioned;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.ApiaryBreederTileEntity.*;

public class CreativeApiaryBreederContainer extends Container {

    private final CreativeApiaryBreederTileEntity apiaryBreederTileEntity;
    private final PlayerInventory playerInventory;
    private boolean rebuild;
    private final int breedersize = 5;

    public final IIntArray times;

    public CreativeApiaryBreederContainer(int id, World world, BlockPos pos, PlayerInventory inv) {
        this(id, world, pos, inv, new IntArray(5));
    }

    public CreativeApiaryBreederContainer(int id, World world, BlockPos pos, PlayerInventory inv, IIntArray times) {
        super(ModContainers.CREATIVE_APIARY_BREEDER_CONTAINER.get(), id);
        this.playerInventory = inv;
        apiaryBreederTileEntity = (CreativeApiaryBreederTileEntity) world.getBlockEntity(pos);
        this.times = times;
        this.addDataSlots(times);
        setupSlots(false);
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        return true;
    }


    public void setupSlots(boolean rebuild) {
        if (getApiaryBreederTileEntity() != null) {
            this.slots.clear();

            for (int i = 0; i< this.breedersize; i++) {
                int finalI = i;
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), apiaryBreederTileEntity.getParent1Slots()[finalI], 33, 18 +(finalI *20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(apiaryBreederTileEntity.getParent1Slots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(apiaryBreederTileEntity.getParent1Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), apiaryBreederTileEntity.getFeed1Slots()[i], 69, 18 +(i*20)){

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(apiaryBreederTileEntity.getFeed1Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), apiaryBreederTileEntity.getParent2Slots()[i], 105, 18 +(i*20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(apiaryBreederTileEntity.getParent2Slots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(apiaryBreederTileEntity.getParent2Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), apiaryBreederTileEntity.getFeed2Slots()[i], 141, 18 +(i*20)){

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(apiaryBreederTileEntity.getFeed2Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), apiaryBreederTileEntity.getEmptyJarSlots()[i], 177, 18 +(i*20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(apiaryBreederTileEntity.getEmptyJarSlots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(apiaryBreederTileEntity.getEmptyJarSlots()[finalI], stack);
                    }
                });
            }

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(getPlayerInventory(), j + i * 9 + 9, 33 + j * 18, 28 + (this.breedersize * 20) + (i * 18)));
                }
            }

            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(getPlayerInventory(), k, 33 + k * 18, 86 + this.breedersize * 20));
            }

            this.setRebuild(rebuild);
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 4 + this.breedersize * 5) {
                if (!this.moveItemStackTo(itemstack1, 4 + this.breedersize * 5, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 4 + this.breedersize * 5, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public CreativeApiaryBreederTileEntity getApiaryBreederTileEntity() {
        return apiaryBreederTileEntity;
    }

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }
}
