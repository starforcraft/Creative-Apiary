package com.YTrollman.CreativeApiary.tileentity;

import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.YTrollman.CreativeApiary.container.CreativeApiaryBreederContainer;
import com.YTrollman.CreativeApiary.registry.ModTileEntityTypes;
import com.resourcefulbees.resourcefulbees.api.ICustomBee;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler.IAcceptor;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler.IRemover;
import com.resourcefulbees.resourcefulbees.entity.passive.CustomBeeEntity;
import com.resourcefulbees.resourcefulbees.item.BeeJar;
import com.resourcefulbees.resourcefulbees.lib.ApiaryTabs;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.IApiaryMultiblock;
import com.resourcefulbees.resourcefulbees.utils.BeeInfoUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.resourcefulbees.resourcefulbees.lib.BeeConstants.MAX_TIME_IN_HIVE;

public class CreativeApiaryBreederTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IApiaryMultiblock {
    private static final int[] UPGRADE_SLOTS =   new int[]{0, 1, 2, 3};
    private static final int[] PARENT_1_SLOTS =  new int[]{4, 9, 14, 19, 24};
    private static final int[] FEED_1_SLOTS =    new int[]{5, 10, 15, 20, 25};
    private static final int[] PARENT_2_SLOTS =  new int[]{6, 11, 16, 21, 26};
    private static final int[] FEED_2_SLOTS =    new int[]{7, 12, 17, 22, 27};
    private static final int[] EMPTY_JAR_SLOTS = new int[]{8, 13, 18, 23, 28};

    private int breedersize = 5;
    private final CreativeApiaryBreederTileEntity.TileStackHandler tileStackHandler = new CreativeApiaryBreederTileEntity.TileStackHandler(29);
    private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(this::getTileStackHandler);
    private int[] time = new int[]{0, 0, 0, 0, 0};
    private int totalTime;
    private BlockPos apiaryPos;
    private CreativeApiaryTileEntity apiary;
    protected final IIntArray times;

    public CreativeApiaryBreederTileEntity() {
        super(ModTileEntityTypes.CREATIVE_APIARY_BREEDER_TILE_ENTITY.get());
        int number = (int) (Config.APIARY_MAX_BREED_TIME.get() * (1 - CreativeApiaryConfig.CREATIVE_APIARY_BREEDER_SPEED.get()));
        this.totalTime = number;
        this.times = new IIntArray() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return CreativeApiaryBreederTileEntity.this.getTime()[0];
                    case 1:
                        return CreativeApiaryBreederTileEntity.this.getTime()[1];
                    case 2:
                        return CreativeApiaryBreederTileEntity.this.getTime()[2];
                    case 3:
                        return CreativeApiaryBreederTileEntity.this.getTime()[3];
                    case 4:
                        return CreativeApiaryBreederTileEntity.this.getTime()[4];
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        CreativeApiaryBreederTileEntity.this.getTime()[0] = value;
                        break;
                    case 1:
                        CreativeApiaryBreederTileEntity.this.getTime()[1] = value;
                        break;
                    case 2:
                        CreativeApiaryBreederTileEntity.this.getTime()[2] = value;
                        break;
                    case 3:
                        CreativeApiaryBreederTileEntity.this.getTime()[3] = value;
                        break;
                    case 4:
                        CreativeApiaryBreederTileEntity.this.getTime()[4] = value;
                }

            }

            public int getCount() {
                return 5;
            }
        };
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
        if (this.apiaryPos != null && this.level != null) {
            TileEntity tile = this.level.getBlockEntity(this.apiaryPos);
            if (tile instanceof CreativeApiaryTileEntity) {
                return (CreativeApiaryTileEntity)tile;
            }
        }

        return null;
    }

    public boolean validateApiaryLink() {
        this.apiary = this.getApiary();
        if (this.apiary != null && this.apiary.getBreederPos() != null && this.apiary.getBreederPos().equals(this.getBlockPos()) && this.apiary.isValidApiary(false)) {
            return true;
        } else {
            this.apiaryPos = null;
            return false;
        }
    }

    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            this.validateApiaryLink();
            boolean dirty = false;

            for(int i = 0; i < this.breedersize; ++i) {
                if (!this.getTileStackHandler().getStackInSlot(getParent1Slots()[i]).isEmpty() && !this.getTileStackHandler().getStackInSlot(getFeed1Slots()[i]).isEmpty() && !this.getTileStackHandler().getStackInSlot(getParent2Slots()[i]).isEmpty() && !this.getTileStackHandler().getStackInSlot(getFeed2Slots()[i]).isEmpty() && !this.getTileStackHandler().getStackInSlot(getEmptyJarSlots()[i]).isEmpty()) {
                    if (this.canProcess(i)) {
                        int var10002 = this.getTime()[i]++;
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
        ItemStack p1Stack = this.getTileStackHandler().getStackInSlot(getParent1Slots()[slot]);
        ItemStack p2Stack = this.getTileStackHandler().getStackInSlot(getParent2Slots()[slot]);
        if (p1Stack.getItem() instanceof BeeJar && p2Stack.getItem() instanceof BeeJar) {
            BeeJar p1Jar = (BeeJar)p1Stack.getItem();
            BeeJar p2Jar = (BeeJar)p2Stack.getItem();
            Entity p1Entity = p1Jar.getEntityFromStack(p1Stack, this.level, true);
            Entity p2Entity = p2Jar.getEntityFromStack(p2Stack, this.level, true);
            if (p1Entity instanceof CustomBeeEntity && p2Entity instanceof CustomBeeEntity) {
                String p1Type = ((CustomBeeEntity)p1Entity).getBeeData().getName();
                String p2Type = ((CustomBeeEntity)p2Entity).getBeeData().getName();
                boolean canBreed = BeeRegistry.getRegistry().canParentsBreed(p1Type, p2Type);
                ItemStack f1Stack = this.getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]);
                ItemStack f2Stack = this.getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]);
                String p1FeedItem = ((CustomBeeEntity)p1Entity).getBeeData().getBreedData().getFeedItem();
                String p2FeedItem = ((CustomBeeEntity)p2Entity).getBeeData().getBreedData().getFeedItem();
                int f1StackCount = this.getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]).getCount();
                int f2StackCount = this.getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]).getCount();
                int p1FeedAmount = ((CustomBeeEntity)p1Entity).getBeeData().getBreedData().getFeedAmount();
                int p2FeedAmount = ((CustomBeeEntity)p2Entity).getBeeData().getBreedData().getFeedAmount();
                return canBreed && BeeInfoUtils.isValidBreedItem(f1Stack, p1FeedItem) && BeeInfoUtils.isValidBreedItem(f2Stack, p2FeedItem) && f1StackCount >= p1FeedAmount && f2StackCount >= p2FeedAmount && !this.getTileStackHandler().getStackInSlot(getEmptyJarSlots()[slot]).isEmpty();
            }
        }

        return false;
    }

    private void processBreed(int slot) {
        if (this.canProcess(slot)) {
            ItemStack p1Stack = this.getTileStackHandler().getStackInSlot(getParent1Slots()[slot]);
            ItemStack p2Stack = this.getTileStackHandler().getStackInSlot(getParent2Slots()[slot]);
            if (p1Stack.getItem() instanceof BeeJar && p2Stack.getItem() instanceof BeeJar) {
                BeeJar p1Jar = (BeeJar)p1Stack.getItem();
                BeeJar p2Jar = (BeeJar)p2Stack.getItem();
                Entity p1Entity = p1Jar.getEntityFromStack(p1Stack, this.level, true);
                Entity p2Entity = p2Jar.getEntityFromStack(p2Stack, this.level, true);
                if (p1Entity instanceof ICustomBee && p2Entity instanceof ICustomBee) {
                    ICustomBee bee1 = (ICustomBee)p1Entity;
                    ICustomBee bee2 = (ICustomBee)p2Entity;
                    String p1Type = bee1.getBeeData().getName();
                    String p2Type = bee2.getBeeData().getName();
                    if (this.level != null && this.validateApiaryLink()) {
                        TileEntity tile = this.level.getBlockEntity(this.apiary.getStoragePos());
                        if (tile instanceof CreativeApiaryStorageTileEntity) {
                            CreativeApiaryStorageTileEntity apiaryStorage = (CreativeApiaryStorageTileEntity)tile;
                            if (apiaryStorage.breedComplete(p1Type, p2Type)) {
                                this.getTileStackHandler().getStackInSlot(getEmptyJarSlots()[slot]).shrink(1);
                                this.getTileStackHandler().getStackInSlot(getFeed1Slots()[slot]).shrink(bee1.getBeeData().getBreedData().getFeedAmount());
                                this.getTileStackHandler().getStackInSlot(getFeed2Slots()[slot]).shrink(bee2.getBeeData().getBreedData().getFeedAmount());
                            }
                        }
                    }
                }
            }
        }

        this.getTime()[slot] = 0;
    }

    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromNBT(nbt);
    }

    @Nonnull
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        super.save(nbt);
        return this.saveToNBT(nbt);
    }

    public void loadFromNBT(CompoundNBT nbt) {
        CompoundNBT invTag = nbt.getCompound("inv");
        this.getTileStackHandler().deserializeNBT(invTag);
        this.setTime(nbt.getIntArray("time"));
        this.setTotalTime(nbt.getInt("totalTime"));
        if (nbt.contains("ApiaryPos")) {
            this.apiaryPos = NBTUtil.readBlockPos(nbt.getCompound("ApiaryPos"));
        }
        if (nbt.contains("BreederCount")) {
            this.setNumberOfBreeders(nbt.getInt("BreederCount"));
        }
    }

    public void setNumberOfBreeders(int numberOfBreeders) {
        this.breedersize = numberOfBreeders;
    }

    public CompoundNBT saveToNBT(CompoundNBT nbt) {
        CompoundNBT inv = this.getTileStackHandler().serializeNBT();
        nbt.put("inv", inv);
        nbt.putIntArray("time", this.getTime());
        nbt.putInt("totalTime", this.getTotalTime());
        if (this.apiaryPos != null) {
            nbt.put("ApiaryPos", NBTUtil.writeBlockPos(this.apiaryPos));
        }
        nbt.putInt("BreederCount", this.breedersize);
        return nbt;
    }

    @Nonnull
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        this.save(nbtTagCompound);
        return nbtTagCompound;
    }

    public void handleUpdateTag(@Nonnull BlockState state, CompoundNBT tag) {
        this.load(state, tag);
    }

    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.saveToNBT(nbt));
    }

    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbt = pkt.getTag();
        this.loadFromNBT(nbt);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ? this.lazyOptional.cast() : super.getCapability(cap, side);
    }

    public IAcceptor getAcceptor() {
        return (slot, stack, automation) -> {
            return !automation || slot > 3;
        };
    }

    public IRemover getRemover() {
        return (slot, automation) -> {
            return !automation || slot > 3;
        };
    }

    @Nullable
    public Container createMenu(int id, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        return new CreativeApiaryBreederContainer(id, this.level, this.worldPosition, playerInventory, this.times);
    }

    @Nonnull
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.creativeapiary.creative_apiary_breeder");
    }

    public void switchTab(ServerPlayerEntity player, ApiaryTabs tab) {
        if (this.level != null && this.apiaryPos != null) {
            TileEntity tile;
            if (tab == ApiaryTabs.MAIN) {
                tile = this.level.getBlockEntity(this.apiaryPos);
                NetworkHooks.openGui(player, (INamedContainerProvider)tile, this.apiaryPos);
            }

            if (tab == ApiaryTabs.STORAGE) {
                tile = this.level.getBlockEntity(this.apiary.getStoragePos());
                NetworkHooks.openGui(player, (INamedContainerProvider)tile, this.apiary.getStoragePos());
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

    @NotNull
    public CreativeApiaryBreederTileEntity.TileStackHandler getTileStackHandler() {
        return this.tileStackHandler;
    }

    public class TileStackHandler extends AutomationSensitiveItemStackHandler {
        private int maxSlots = 28;

        protected TileStackHandler(int slots) {
            super(slots);
        }

        public IAcceptor getAcceptor() {
            return CreativeApiaryBreederTileEntity.this.getAcceptor();
        }

        public IRemover getRemover() {
            return CreativeApiaryBreederTileEntity.this.getRemover();
        }

        public int getSlotLimit(int slot) {
            switch(slot) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 6:
                case 9:
                case 11:
                case 14:
                case 16:
                case 19:
                case 21:
                case 24:
                case 26:
                    return 1;
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                case 13:
                case 15:
                case 17:
                case 18:
                case 20:
                case 22:
                case 23:
                case 25:
                default:
                    return 64;
            }
        }

        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            switch(slot) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return true;
                case 4:
                case 6:
                case 9:
                case 11:
                case 14:
                case 16:
                case 19:
                case 21:
                case 24:
                case 26:
                    if (!this.isSlotVisible(slot)) {
                        return false;
                    }

                    return stack.getItem() instanceof BeeJar && BeeJar.isFilled(stack) && stack.getTag().getString("Entity").startsWith("resourcefulbees");
                case 5:
                case 7:
                case 10:
                case 12:
                case 15:
                case 17:
                case 20:
                case 22:
                case 25:
                case 27:
                    if (this.isSlotVisible(slot)) {
                        return !(stack.getItem() instanceof BeeJar);
                    }

                    return false;
                case 8:
                case 13:
                case 18:
                case 23:
                case 28:
                    if (!this.isSlotVisible(slot)) {
                        return false;
                    }

                    return stack.getItem() instanceof BeeJar && !BeeJar.isFilled(stack);
                default:
                    return false;
            }
        }

        private boolean isSlotVisible(int slot) {
            return slot <= this.maxSlots;
        }
    }
}
