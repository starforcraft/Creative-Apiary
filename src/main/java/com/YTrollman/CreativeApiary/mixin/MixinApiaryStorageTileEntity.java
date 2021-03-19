package com.YTrollman.CreativeApiary.mixin;

import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.resourcefulbees.resourcefulbees.api.IBeeRegistry;
import com.resourcefulbees.resourcefulbees.api.ICustomBee;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;
import com.resourcefulbees.resourcefulbees.lib.BeeConstants;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.ApiaryStorageTileEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.jetbrains.annotations.NotNull;

@Mixin(value = ApiaryStorageTileEntity.class, remap = false)
public class MixinApiaryStorageTileEntity {

    private static final IBeeRegistry BEE_REGISTRY = BeeRegistry.getRegistry();
    private final AutomationSensitiveItemStackHandler itemStackHandler = new ApiaryStorageTileEntity.TileStackHandler(110);
    private int numberOfSlots = 9;

    @Overwrite
    public void deliverHoneycomb(BeeEntity entity, int apiaryTier) {
        String beeType;
        if (entity instanceof ICustomBee && ((ICustomBee) entity).getBeeData().hasHoneycomb()) {
            beeType = ((ICustomBee) entity).getBeeType();
        } else if (!(entity instanceof ICustomBee)) {
            beeType = BeeConstants.VANILLA_BEE_TYPE;
        } else {
            return;
        }

        ItemStack itemstack;
        ItemStack comb = beeType.equals(BeeConstants.VANILLA_BEE_TYPE) ? new ItemStack(Items.HONEYCOMB) : ((ICustomBee) entity).getBeeData().getCombStack();
        ItemStack combBlock = beeType.equals(BeeConstants.VANILLA_BEE_TYPE) ? new ItemStack(Items.HONEYCOMB_BLOCK) : ((ICustomBee) entity).getBeeData().getCombBlockItemStack();
        int[] outputAmounts = beeType.equals(BeeConstants.VANILLA_BEE_TYPE) ? null : ApiaryStorageTileEntity.BEE_REGISTRY.getBeeData(beeType).getApiaryOutputAmounts();

        switch (apiaryTier) {
            case 100:
                itemstack = (CreativeApiaryConfig.TCREATIVE_APIARY_OUTPUT.get() == ApiaryOutput.BLOCK) ? combBlock.copy() : comb.copy();
                itemstack.setCount(outputAmounts != null && outputAmounts[3] != -1 ? outputAmounts[3] : CreativeApiaryConfig.TCREATIVE_APIARY_QUANTITY.get());
            case 8:
                itemstack = (Config.T4_APIARY_OUTPUT.get() == ApiaryOutput.BLOCK) ? combBlock.copy() : comb.copy();
                itemstack.setCount(outputAmounts != null && outputAmounts[3] != -1 ? outputAmounts[3] : Config.T4_APIARY_QUANTITY.get());
                break;
            case 7:
                itemstack = (Config.T3_APIARY_OUTPUT.get() == ApiaryOutput.BLOCK) ? combBlock.copy() : comb.copy();
                itemstack.setCount(outputAmounts != null && outputAmounts[2] != -1 ? outputAmounts[2] : Config.T3_APIARY_QUANTITY.get());
                break;
            case 6:
                itemstack = (Config.T2_APIARY_OUTPUT.get() == ApiaryOutput.BLOCK) ? combBlock.copy() : comb.copy();
                itemstack.setCount(outputAmounts != null && outputAmounts[1] != -1 ? outputAmounts[1] : Config.T2_APIARY_QUANTITY.get());
                break;
            default:
                itemstack = (Config.T1_APIARY_OUTPUT.get() == ApiaryOutput.BLOCK) ? combBlock.copy() : comb.copy();
                itemstack.setCount(outputAmounts != null && outputAmounts[0] != -1 ? outputAmounts[0] : Config.T1_APIARY_QUANTITY.get());
                break;
        }
        depositItemStack(itemstack);
    }

    public boolean depositItemStack(ItemStack itemstack) {
        for(int slotIndex = 1; !itemstack.isEmpty() && slotIndex <= this.getNumberOfSlots(); ++slotIndex) {
            ItemStack slotStack = this.getItemStackHandler().getStackInSlot(slotIndex);
            int maxStackSize = this.getItemStackHandler().getSlotLimit(slotIndex);
            int j;
            if (slotStack.isEmpty()) {
                j = itemstack.getCount();
                slotStack = itemstack.copy();
                if (j > maxStackSize) {
                    slotStack.setCount(maxStackSize);
                    itemstack.setCount(j - maxStackSize);
                } else {
                    itemstack.setCount(0);
                }

                this.getItemStackHandler().setStackInSlot(slotIndex, slotStack);
            } else if (Container.consideredTheSameItem(itemstack, slotStack)) {
                j = itemstack.getCount() + slotStack.getCount();
                if (j <= maxStackSize) {
                    itemstack.setCount(0);
                    slotStack.setCount(j);
                    this.getItemStackHandler().setStackInSlot(slotIndex, slotStack);
                } else if (slotStack.getCount() < maxStackSize) {
                    itemstack.shrink(maxStackSize - slotStack.getCount());
                    slotStack.setCount(maxStackSize);
                    this.getItemStackHandler().setStackInSlot(slotIndex, slotStack);
                }
            }
        }

        return itemstack.isEmpty();
    }

    public int getNumberOfSlots() {
        return this.numberOfSlots;
    }

    @NotNull
    public AutomationSensitiveItemStackHandler getItemStackHandler() {
        return this.itemStackHandler;
    }

}
