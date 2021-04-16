package com.YTrollman.CreativeApiary.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import com.YTrollman.CreativeApiary.network.CreativeLockBeeMessage;
import com.YTrollman.CreativeApiary.network.CreativeNetPacketHandler;
import com.YTrollman.CreativeApiary.registry.ModContainers;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;
import com.resourcefulbees.resourcefulbees.container.OutputSlot;
import com.resourcefulbees.resourcefulbees.container.SlotItemHandlerUnconditioned;

public class ValidatedCreativeApiaryContainer extends Container {

    private final IntReferenceHolder selectedBee = IntReferenceHolder.standalone();
    private final CreativeApiaryTileEntity apiaryTileEntity;
    private final BlockPos pos;
    private final PlayerEntity player;
    private int[] beeList;

    public ValidatedCreativeApiaryContainer(int id, World world, BlockPos pos, PlayerInventory inv) {
        super(ModContainers.VALIDATED_CREATIVE_APIARY_CONTAINER.get(), id);

        this.player = inv.player;
        this.pos = pos;
        this.apiaryTileEntity = (CreativeApiaryTileEntity) world.getBlockEntity(pos);

        if (getApiaryTileEntity() != null) {
            this.addSlot(new SlotItemHandlerUnconditioned(getApiaryTileEntity().getTileStackHandler(), CreativeApiaryTileEntity.IMPORT, 74, 37) {
                @Override
                public int getMaxStackSize() {
                    return getApiaryTileEntity().getTileStackHandler().getSlotLimit(CreativeApiaryTileEntity.IMPORT);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getApiaryTileEntity().getTileStackHandler().isItemValid(CreativeApiaryTileEntity.IMPORT, stack);
                }
            });
            this.addSlot(new SlotItemHandlerUnconditioned(getApiaryTileEntity().getTileStackHandler(), CreativeApiaryTileEntity.EMPTY_JAR, 128, 37) {
                @Override
                public int getMaxStackSize() {
                    return getApiaryTileEntity().getTileStackHandler().getSlotLimit(CreativeApiaryTileEntity.EMPTY_JAR);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getApiaryTileEntity().getTileStackHandler().isItemValid(CreativeApiaryTileEntity.EMPTY_JAR, stack);
                }
            });
            this.addSlot(new OutputSlot(getApiaryTileEntity().getTileStackHandler(), CreativeApiaryTileEntity.EXPORT, 182, 37));
            if (!world.isClientSide) {
                this.getApiaryTileEntity().setNumPlayersUsing(this.getApiaryTileEntity().getNumPlayersUsing() + 1);
                CreativeApiaryTileEntity.syncApiaryToPlayersUsing(world, pos, this.getApiaryTileEntity().saveToNBT(new CompoundNBT()));
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 56 + j * 18, 70 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 56 + k * 18, 128));
        }
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void removed(@Nonnull PlayerEntity playerIn) {
        World world = this.getApiaryTileEntity().getLevel();
        if (world != null && !world.isClientSide)
            this.getApiaryTileEntity().setNumPlayersUsing(this.getApiaryTileEntity().getNumPlayersUsing() - 1);
        super.removed(playerIn);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index <= 1) {
                if (!this.moveItemStackTo(itemstack1, 2, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
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

    public boolean selectBee(int id) {
        if (id >= -1 && id < getApiaryTileEntity().getBeeCount()) {
            this.selectedBee.set(id);
        }
        return true;
    }

    public boolean lockOrUnlockBee(int id) {
        if (id >= 0 && id < getApiaryTileEntity().getBeeCount()) {
            CreativeNetPacketHandler.sendToServer(new CreativeLockBeeMessage(getApiaryTileEntity().getBlockPos(), String.valueOf(getBeeList()[id])));
        }
        return true;
    }

    public int getSelectedBee() { return this.selectedBee.get(); }

    public CreativeApiaryTileEntity.CreativeApiaryBee getApiaryBee(int i) {
        return getApiaryTileEntity().bees.get(getBeeList()[i]);
    }

    public CreativeApiaryTileEntity getApiaryTileEntity() {
        return apiaryTileEntity;
    }

    public BlockPos getPos() {
        return pos;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public int[] getBeeList() {
        return beeList;
    }

    public void setBeeList(int[] beeList) {
        this.beeList = beeList;
    }
}
