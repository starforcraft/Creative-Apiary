package com.YTrollman.CreativeApiary.tileentity;

import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.YTrollman.CreativeApiary.container.CreativeApiaryBreederContainer;
import com.YTrollman.CreativeApiary.registry.ModTileEntityTypes;
import com.resourcefulbees.resourcefulbees.api.ICustomBee;
import com.resourcefulbees.resourcefulbees.api.beedata.BreedData;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.resourcefulbees.resourcefulbees.entity.passive.CustomBeeEntity;
import com.resourcefulbees.resourcefulbees.item.BeeJar;
import com.resourcefulbees.resourcefulbees.item.UpgradeItem;
import com.resourcefulbees.resourcefulbees.lib.ApiaryTabs;
import com.resourcefulbees.resourcefulbees.lib.NBTConstants;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.IApiaryMultiblock;
import com.resourcefulbees.resourcefulbees.utils.BeeInfoUtils;
import com.resourcefulbees.resourcefulbees.utils.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CreativeApiaryBreederTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IApiaryMultiblock {

    private static final int[] PARENT_1_SLOTS =  new int[]{4, 9, 14, 19, 24};
    private static final int[] FEED_1_SLOTS =    new int[]{5, 10, 15, 20, 25};
    private static final int[] PARENT_2_SLOTS =  new int[]{6, 11, 16, 21, 26};
    private static final int[] FEED_2_SLOTS =    new int[]{7, 12, 17, 22, 27};
    private static final int[] EMPTY_JAR_SLOTS = new int[]{8, 13, 18, 23, 28};

    private final CreativeApiaryBreederTileEntity.TileStackHandler tileStackHandler = new CreativeApiaryBreederTileEntity.TileStackHandler(29);
    private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(this::getTileStackHandler);
    private int[] time = new int[]{0, 0, 0, 0, 0};
    private int totalTime = (int) (Config.APIARY_MAX_BREED_TIME.get() * (1 - CreativeApiaryConfig.CREATIVE_APIARY_BREEDER_SPEED.get()));
    private int numberOfBreeders = 5;

    private BlockPos apiaryPos;
    private CreativeApiaryTileEntity apiary;

    protected final IIntArray times = new IIntArray() {
        @Override
        public int get(int index) {
            return MathUtils.inRangeExclusive(index, -1, 5)
                    ? CreativeApiaryBreederTileEntity.this.getTime()[index]
                    : 0;
        }

        @Override
        public void set(int index, int value) {
            if (!MathUtils.inRangeExclusive(index, -1, 5)) return;
            CreativeApiaryBreederTileEntity.this.getTime()[index] = value;
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public CreativeApiaryBreederTileEntity() {
        super(ModTileEntityTypes.CREATIVE_APIARY_BREEDER_TILE_ENTITY.get());
    }

    public static int[] getParent1Slots() {
        return PARENT_1_SLOTS;
    }

    public static int[] getFeed1Slots() {
        return FEED_1_SLOTS;
    }

    public static int[] getParent2Slots() {
        return PARENT_2_SLOTS;
    }

    public static int[] getFeed2Slots() {
        return FEED_2_SLOTS;
    }

    public static int[] getEmptyJarSlots() {
        return EMPTY_JAR_SLOTS;
    }

    public BlockPos getApiaryPos() {
        return this.apiaryPos;
    }

    public void setApiaryPos(BlockPos apiaryPos) {
        this.apiaryPos = apiaryPos;
    }

    public CreativeApiaryTileEntity getApiary() {
        if (apiaryPos != null && level != null) {
            TileEntity tile = level.getBlockEntity(apiaryPos);
            if (tile instanceof CreativeApiaryTileEntity) {
                return (CreativeApiaryTileEntity) tile;
            }
        }
        return null;
    }

    public boolean validateApiaryLink() {
        apiary = getApiary();
        if (apiary == null || apiary.getBreederPos() == null || !apiary.getBreederPos().equals(this.getBlockPos()) || !apiary.isValidApiary(false)) {
            apiaryPos = null;
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide) {
            validateApiaryLink();
            boolean dirty = false;
            for (int i = 0; i < getNumberOfBreeders(); i++) {
                if (!getTileStackHandler().getStackInSlot(getParent1Slots()[i]).isEmpty() && !getTileStackHandler().getStackInSlot(getFeed1Slots()[i]).isEmpty()
                        && !getTileStackHandler().getStackInSlot(getParent2Slots()[i]).isEmpty() && !getTileStackHandler().getStackInSlot(getFeed2Slots()[i]).isEmpty()
                        && !getTileStackHandler().getStackInSlot(getEmptyJarSlots()[i]).isEmpty()) {
                    if (canProcess(i)) {
                        ++this.getTime()[i];
                        if (this.getTime()[i] >= this.getTotalTime()) {
                            this.getTime()[i] = 0;
                            this.processBreed(i);
                            dirty = true;
                        }
                    }
                } else {
                    this.getTime()[i] = 0;
                }
                if (dirty) {
                    this.setChanged();
                }
            }
        }
    }

    protected boolean canProcess(int slot) {
        ItemStack p1Stack = getTileStackHandler().getStackInSlot(getParent1Slots()[slot]);
        ItemStack p2Stack = getTileStackHandler().getStackInSlot(getParent2Slots()[slot]);
        if (p1Stack.getItem() instanceof BeeJar && p2Stack.getItem() instanceof BeeJar) {
            BeeJar p1Jar = (BeeJar) p1Stack.getItem();
            BeeJar p2Jar = (BeeJar) p2Stack.getItem();

            Entity p1Entity = p1Jar.getEntityFromStack(p1Stack, level, true);
            Entity p2Entity = p2Jar.getEntityFromStack(p2Stack, level, true);

            if (p1Entity instanceof CustomBeeEntity && p2Entity instanceof CustomBeeEntity) {
                String p1Type = ((CustomBeeEntity) p1Entity).getBeeData().getName();
                String p2Type = ((CustomBeeEntity) p2Entity).getBeeData().getName();

                boolean canBreed = BeeRegistry.getRegistry().canParentsBreed(p1Type, p2Type);

                ItemStack f1Stack = getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]);
                ItemStack f2Stack = getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]);

                BreedData p1BreedData = ((CustomBeeEntity) p1Entity).getBeeData().getBreedData();
                BreedData p2BreedData = ((CustomBeeEntity) p2Entity).getBeeData().getBreedData();

                int f1StackCount = getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]).getCount();
                int f2StackCount = getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]).getCount();

                int p1FeedAmount = ((CustomBeeEntity) p1Entity).getBeeData().getBreedData().getFeedAmount();
                int p2FeedAmount = ((CustomBeeEntity) p2Entity).getBeeData().getBreedData().getFeedAmount();

                return (canBreed && BeeInfoUtils.isValidBreedItem(f1Stack, p1BreedData) && BeeInfoUtils.isValidBreedItem(f2Stack, p2BreedData)
                        && f1StackCount >= p1FeedAmount && f2StackCount >= p2FeedAmount && !getTileStackHandler().getStackInSlot(getEmptyJarSlots()[slot]).isEmpty());
            }
        }
        return false;
    }

    private void processBreed(int slot) {
        if (canProcess(slot)) {
            ItemStack p1Stack = getTileStackHandler().getStackInSlot(getParent1Slots()[slot]);
            ItemStack p2Stack = getTileStackHandler().getStackInSlot(getParent2Slots()[slot]);
            if (p1Stack.getItem() instanceof BeeJar && p2Stack.getItem() instanceof BeeJar) {
                BeeJar p1Jar = (BeeJar) p1Stack.getItem();
                BeeJar p2Jar = (BeeJar) p2Stack.getItem();

                Entity p1Entity = p1Jar.getEntityFromStack(p1Stack, level, true);
                Entity p2Entity = p2Jar.getEntityFromStack(p2Stack, level, true);

                if (p1Entity instanceof ICustomBee && p2Entity instanceof ICustomBee) {
                    ICustomBee bee1 = (ICustomBee) p1Entity;
                    ICustomBee bee2 = (ICustomBee) p2Entity;

                    String p1Type = bee1.getBeeData().getName();
                    String p2Type = bee2.getBeeData().getName();

                    if (level != null && validateApiaryLink()) {
                        TileEntity tile = level.getBlockEntity(apiary.getStoragePos());
                        if (tile instanceof CreativeApiaryStorageTileEntity) {
                            CreativeApiaryStorageTileEntity apiaryStorage = (CreativeApiaryStorageTileEntity) tile;
                            if (apiaryStorage.breedComplete(p1Type, p2Type)) {
                                getTileStackHandler().getStackInSlot(getEmptyJarSlots()[slot]).shrink(1);
                                getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]).shrink(bee1.getBeeData().getBreedData().getFeedAmount());
                                getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]).shrink(bee2.getBeeData().getBreedData().getFeedAmount());
                            }
                        }
                    }
                }
            }
        }
        this.getTime()[slot] = 0;
    }

    @NotNull
    public CompoundNBT save(@NotNull CompoundNBT nbt) {
        super.save(nbt);
        return this.saveToNBT(nbt);
    }

    public CompoundNBT saveToNBT(CompoundNBT nbt) {
        CompoundNBT inv = this.getTileStackHandler().serializeNBT();
        nbt.put(NBTConstants.NBT_INVENTORY, inv);
        nbt.putIntArray("time", getTime());
        nbt.putInt("totalTime", getTotalTime());
        if (apiaryPos != null)
            nbt.put(NBTConstants.NBT_APIARY_POS, NBTUtil.writeBlockPos(apiaryPos));
        if (getNumberOfBreeders() != 1) {
            nbt.putInt(NBTConstants.NBT_BREEDER_COUNT, getNumberOfBreeders());
        }
        return nbt;
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        this.save(nbtTagCompound);
        return nbtTagCompound;
    }

    public void load(@NotNull BlockState state, @NotNull CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromNBT(nbt);
    }

    public void loadFromNBT(CompoundNBT nbt) {
        CompoundNBT invTag = nbt.getCompound(NBTConstants.NBT_INVENTORY);
        getTileStackHandler().deserializeNBT(invTag);
        setTime(nbt.getIntArray("time"));
        setTotalTime(nbt.getInt("totalTime"));
        if (nbt.contains(NBTConstants.NBT_APIARY_POS))
            apiaryPos = NBTUtil.readBlockPos(nbt.getCompound(NBTConstants.NBT_APIARY_POS));
        if (nbt.contains(NBTConstants.NBT_BREEDER_COUNT))
            this.setNumberOfBreeders(nbt.getInt(NBTConstants.NBT_BREEDER_COUNT));
    }

    public void handleUpdateTag(@NotNull BlockState state, CompoundNBT tag) {
        this.load(state, tag);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();
        return new SUpdateTileEntityPacket(worldPosition, 0, saveToNBT(nbt));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbt = pkt.getTag();
        this.loadFromNBT(nbt);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return lazyOptional.cast();
        return super.getCapability(cap, side);
    }

    public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation || slot > 3;
    }

    public AutomationSensitiveItemStackHandler.IRemover getRemover() {
        return (slot, automation) -> !automation || slot > 3;
    }

    @Nullable
    public Container createMenu(int id, @NotNull PlayerInventory playerInventory, @NotNull PlayerEntity playerEntity) {
        return new CreativeApiaryBreederContainer(id, level, worldPosition, playerInventory, times);
    }

    @NotNull
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.creativeapiary.creative_apiary_breeder");
    }

    @Override
    public void switchTab(ServerPlayerEntity player, ApiaryTabs tab) {
        if (level != null && apiaryPos != null) {
            if (tab == ApiaryTabs.MAIN) {
                TileEntity tile = level.getBlockEntity(apiaryPos);
                NetworkHooks.openGui(player, (INamedContainerProvider) tile, apiaryPos);
            }
            if (tab == ApiaryTabs.STORAGE) {
                TileEntity tile = level.getBlockEntity(apiary.getStoragePos());
                NetworkHooks.openGui(player, (INamedContainerProvider) tile, apiary.getStoragePos());
            }
        }
    }

    public int[] getTime() {
        return this.time;
    }

    public void setTime(int[] time) {
        this.time = time;
    }

    public int getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getNumberOfBreeders() {
        return numberOfBreeders;
    }

    public void setNumberOfBreeders(int numberOfBreeders) {
        this.numberOfBreeders = numberOfBreeders;
    }

    public @NotNull TileStackHandler getTileStackHandler() {
        return tileStackHandler;
    }

    public class TileStackHandler extends AutomationSensitiveItemStackHandler {

        private int maxSlots = 28;

        public void setMaxSlots(int maxSlots) {
            this.maxSlots = maxSlots;
        }

        protected TileStackHandler(int slots) {
            super(slots);
        }

        @Override
        public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
            return CreativeApiaryBreederTileEntity.this.getAcceptor();
        }

        @Override
        public AutomationSensitiveItemStackHandler.IRemover getRemover() {
            return CreativeApiaryBreederTileEntity.this.getRemover();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            switch (slot) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 9:
                case 14:
                case 19:
                case 24:
                case 6:
                case 11:
                case 16:
                case 21:
                case 26:
                    //parent slots
                    return 1;
                default:
                    return 64;
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            switch (slot) {
                case 0:
                case 1:
                case 2:
                case 3:
                    // upgrade slots
                    return UpgradeItem.hasUpgradeData(stack) && (UpgradeItem.getUpgradeType(stack).contains(NBTConstants.NBT_BREEDER_UPGRADE));
                //Parent 1 Bee jars
                case 4:
                case 9:
                case 14:
                case 19:
                case 24:
                    //Parent 2 Bee jars
                case 6:
                case 11:
                case 16:
                case 21:
                case 26:
                    //parent slots
                    return isSlotVisible(slot) && BeeInfoUtils.isBeeInJarOurs(stack);
                //Parent 1 Feed Items
                case 5:
                case 10:
                case 15:
                case 20:
                case 25:
                    //Parent 2 Feed Items
                case 7:
                case 12:
                case 17:
                case 22:
                case 27:
                    // feed slots
                    return isSlotVisible(slot) && !(stack.getItem() instanceof BeeJar);
                case 8:
                case 13:
                case 18:
                case 23:
                case 28:
                    // jar slots
                    return isSlotVisible(slot) && stack.getItem() instanceof BeeJar && !BeeJar.isFilled(stack);
                default:
                    //do nothing
                    return false;
            }
        }

        private boolean isSlotVisible(int slot) {
            return slot <= maxSlots;
        }
    }
}
