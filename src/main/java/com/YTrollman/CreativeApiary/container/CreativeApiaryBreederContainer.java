package com.YTrollman.CreativeApiary.container;

import com.YTrollman.CreativeApiary.registry.ModContainers;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryBreederTileEntity;
import com.resourcefulbees.resourcefulbees.container.ContainerWithStackMove;
import com.resourcefulbees.resourcefulbees.container.SlotItemHandlerUnconditioned;
import com.resourcefulbees.resourcefulbees.item.UpgradeItem;
import com.resourcefulbees.resourcefulbees.lib.NBTConstants;
import com.resourcefulbees.resourcefulbees.utils.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static com.YTrollman.CreativeApiary.tileentity.CreativeApiaryBreederTileEntity.*;

public class CreativeApiaryBreederContainer extends ContainerWithStackMove {

    private final CreativeApiaryBreederTileEntity apiaryBreederTileEntity;
    private final PlayerInventory playerInventory;
    private int numberOfBreeders;
    private boolean rebuild;

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
            numberOfBreeders = getApiaryBreederTileEntity().getNumberOfBreeders();

            for (int i = 0; i< getNumberOfBreeders(); i++) {
                int finalI = i;
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), getParent1Slots()[finalI], 33, 18 +(finalI *20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(getParent1Slots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(getParent1Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), getFeed1Slots()[i], 69, 18 +(i*20)){

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(getFeed1Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), getParent2Slots()[i], 105, 18 +(i*20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(getParent2Slots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(getParent2Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), getFeed2Slots()[i], 141, 18 +(i*20)){

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(getFeed2Slots()[finalI], stack);
                    }
                });
                this.addSlot(new SlotItemHandlerUnconditioned(getApiaryBreederTileEntity().getTileStackHandler(), getEmptyJarSlots()[i], 177, 18 +(i*20)){

                    @Override
                    public int getMaxStackSize() {
                        return getApiaryBreederTileEntity().getTileStackHandler().getSlotLimit(getEmptyJarSlots()[finalI]);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return getApiaryBreederTileEntity().getTileStackHandler().isItemValid(getEmptyJarSlots()[finalI], stack);
                    }
                });
            }

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(getPlayerInventory(), j + i * 9 + 9, 33 + j * 18, 28 + (getNumberOfBreeders() * 20) + (i * 18)));
                }
            }

            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(getPlayerInventory(), k, 33 + k * 18, 86 + getNumberOfBreeders() * 20));
            }

            this.setRebuild(rebuild);
        }
    }

    public int getContainerInputEnd() {
        return 4 + getNumberOfBreeders() * 5;
    }

    @Override
    public int getInventoryStart() {
        return 4 + getNumberOfBreeders() * 5;
    }

    public CreativeApiaryBreederTileEntity getApiaryBreederTileEntity() {
        return apiaryBreederTileEntity;
    }

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    public int getNumberOfBreeders() {
        return numberOfBreeders;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }
}
