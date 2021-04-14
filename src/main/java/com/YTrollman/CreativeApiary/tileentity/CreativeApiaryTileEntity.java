package com.YTrollman.CreativeApiary.tileentity;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.YTrollman.CreativeApiary.block.CreativeApiaryStorageBlock;
import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.YTrollman.CreativeApiary.registry.ModBlocks;
import com.google.common.collect.Lists;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.ApiaryTileEntity;
import net.minecraft.tileentity.BeehiveTileEntity;
import org.antlr.v4.runtime.misc.NotNull;

import com.YTrollman.CreativeApiary.block.CreativeApiaryBlock;
import com.YTrollman.CreativeApiary.container.UnvalidatedCreativeApiaryContainer;
import com.YTrollman.CreativeApiary.container.ValidatedCreativeApiaryContainer;
import com.YTrollman.CreativeApiary.network.CreativeNetPacketHandler;
import com.YTrollman.CreativeApiary.network.CreativeUpdateClientApiaryMessage;
import com.YTrollman.CreativeApiary.registry.ModTileEntityTypes;
import com.resourcefulbees.resourcefulbees.api.ICustomBee;
import com.resourcefulbees.resourcefulbees.block.multiblocks.apiary.ApiaryBreederBlock;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.resourcefulbees.resourcefulbees.item.BeeJar;
import com.resourcefulbees.resourcefulbees.lib.ApiaryTabs;
import com.resourcefulbees.resourcefulbees.lib.BeeConstants;
import com.resourcefulbees.resourcefulbees.lib.NBTConstants;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.MultiBlockHelper;
import com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.IApiaryMultiblock;
import com.resourcefulbees.resourcefulbees.utils.BeeInfoUtils;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static com.resourcefulbees.resourcefulbees.lib.BeeConstants.*;

public class CreativeApiaryTileEntity extends ApiaryTileEntity implements ITickableTileEntity, INamedContainerProvider, IApiaryMultiblock {
    public static final int IMPORT = 0;
    public static final int EXPORT = 2;
    public static final int EMPTY_JAR = 1;
    public final List<CreativeApiaryTileEntity.CreativeApiaryBee> bees = new ArrayList();
    private final List<BlockPos> structureBlocks = new ArrayList<>();
    protected int tier;
    private boolean isValidApiary;
    private boolean previewed;
    private final CreativeApiaryTileEntity.TileStackHandler tileStackHandler = new CreativeApiaryTileEntity.TileStackHandler(3);
    private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(this::getTileStackHandler);
    private int horizontalOffset = 0;
    private int verticalOffset = 0;
    private int numPlayersUsing;
    private int ticksSinceValidation;
    private int ticksSinceSync;
    private BlockPos storagePos;
    private BlockPos breederPos;
    private CreativeApiaryStorageTileEntity apiaryStorage;
    private CreativeApiaryBreederTileEntity apiaryBreeder;
    protected int ticksSinceBeesFlagged;

    public CreativeApiaryTileEntity() {
        super(ModTileEntityTypes.CREATIVE_APIARY_TILE_ENTITY.get()); //ModTileEntityTypes.CREATIVE_APIARY_TILE_ENTITY.get()
    }

    //region PLAYER SYNCING
    public static int calculatePlayersUsingSync(World world, CreativeApiaryTileEntity apiaryTileEntity, int ticksSinceSync, int posX, int posY, int posZ, int numPlayersUsing) {
        if (!world.isClientSide && numPlayersUsing != 0 && (ticksSinceSync + posX + posY + posZ) % 200 == 0) {
            numPlayersUsing = calculatePlayersUsing(world, apiaryTileEntity, posX, posY, posZ);
        }

        return numPlayersUsing;
    }

    public static int calculatePlayersUsing(World world, CreativeApiaryTileEntity apiaryTileEntity, int posX, int posY, int posZ) {
        int i = 0;
        float f = 5.0F;

        for (PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(posX - f, posY - f, posZ - f, (posX + 1) + f, (posY + 1) + f, (posZ + 1) + f))) {
            if (playerentity.containerMenu instanceof ValidatedCreativeApiaryContainer) {
                CreativeApiaryTileEntity apiaryTileEntity1 = ((ValidatedCreativeApiaryContainer) playerentity.containerMenu).getApiaryTileEntity();
                if (apiaryTileEntity1 == apiaryTileEntity) {
                    ++i;
                }
            }
        }

        return i;
    }

    public static void syncApiaryToPlayersUsing(World world, BlockPos pos, CompoundNBT data) {
        CreativeNetPacketHandler.sendToAllLoaded(new CreativeUpdateClientApiaryMessage(pos, data), world, pos);
    }
    //endregion

    @Override
    public boolean isValidApiary(boolean runValidation) {
        if (runValidation) {
            runStructureValidation(null);
        }
        return isValidApiary;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public int getMaxBees() {
        return CreativeApiaryConfig.TCREATIVE_APIARY_MAX_BEES.get();
    }

    @Override
    public int getBeeCount() {
        return this.bees.size();
    }

    public CreativeApiaryStorageTileEntity getApiaryStorage2() {
        if (this.level != null && this.getStoragePos() != null) {
            TileEntity tile = this.level.getBlockEntity(this.getStoragePos());
            if (tile instanceof CreativeApiaryStorageTileEntity) {
                return (CreativeApiaryStorageTileEntity) tile;
            }
        }

        this.setStoragePos((BlockPos)null);
        return null;
    }

    public CreativeApiaryBreederTileEntity getApiaryBreeder2() {
        if (level != null && getBreederPos() != null) {
            TileEntity tile = level.getBlockEntity(getBreederPos());
            if (tile instanceof CreativeApiaryBreederTileEntity) {
                return (CreativeApiaryBreederTileEntity) tile;
            }
        }
        setBreederPos(null);
        return null;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    //region BEE HANDLING
    public boolean releaseBee(@Nonnull BlockState state, CreativeApiaryBee apiaryBee, boolean exportBee) {
        BlockPos blockPos = this.getBlockPos();
        Direction direction = state.getValue(BeehiveBlock.FACING);
        BlockPos blockPos1 = blockPos.relative(direction);
        CompoundNBT nbt = apiaryBee.entityData;

        if (level != null && this.level.getBlockState(blockPos1).getCollisionShape(this.level, blockPos1).isEmpty()) {
            nbt.remove("Passengers");
            nbt.remove("Leash");
            nbt.remove("UUID");
            Entity entity = EntityType.loadEntityRecursive(nbt, this.level, entity1 -> entity1);
            if (entity == null) return true;
            BeeInfoUtils.setEntityLocationAndAngle(blockPos, direction, entity);

            if (entity instanceof BeeEntity) {
                BeeEntity vanillaBeeEntity = (BeeEntity) entity;

                BlockPos flowerPos = apiaryBee.savedFlowerPos;
                if (flowerPos != null && !vanillaBeeEntity.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
                    vanillaBeeEntity.setSavedFlowerPos(flowerPos);
                }

                if (nbt.getBoolean("HasNectar")) {
                    vanillaBeeEntity.dropOffNectar();

                    if (!exportBee && isValidApiary(true)) {
                        getApiaryStorage2().deliverHoneycomb(((BeeEntity) entity), getTier());
                    }
                }

                this.ageBee(apiaryBee.getTicksInHive(), vanillaBeeEntity);

                if (exportBee) {
                    exportBee(vanillaBeeEntity);
                } else {
                    BlockPos hivePos = this.getBlockPos();
                    this.level.playSound(null, hivePos.getX(), hivePos.getY(), hivePos.getZ(), SoundEvents.BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    this.level.addFreshEntity(entity);
                }
            }
            return true;
        }
        return false;
    }

    private void ageBee(int ticksInHive, BeeEntity beeEntity) {
        BeeInfoUtils.ageBee(ticksInHive, beeEntity);
    }

    @Override
    public boolean tryEnterHive(Entity bee, boolean hasNectar, boolean imported) {
        if (isValidApiary(true))
            return this.tryEnterHive(bee, hasNectar, 0, imported);
        return false;
    }

    @Override
    public boolean tryEnterHive(@Nonnull Entity bee, boolean hasNectar, int ticksInHive, boolean imported) {
        if (this.level != null && bee instanceof BeeEntity) {
            BeeEntity beeEntity = (BeeEntity) bee;
            String type = VANILLA_BEE_TYPE;
            String beeColor = VANILLA_BEE_COLOR;

            if (bee instanceof ICustomBee) {
                type = ((ICustomBee) bee).getBeeType();
            }

            if (this.bees.size() < this.getMaxBees()) { //!this.bees.containsKey(type) &&
                bee.ejectPassengers();
                CompoundNBT nbt = new CompoundNBT();
                bee.save(nbt);

                int number = (int) (MAX_TIME_IN_HIVE * (1 - CreativeApiaryConfig.TCREATIVE_APIARY_SPEED.get()));
                int maxTimeInHive = getMaxTimeInHive(number);
                if (bee instanceof ICustomBee) {
                    ICustomBee iCustomBee = (ICustomBee) bee;
                    maxTimeInHive = getMaxTimeInHive(iCustomBee.getBeeData().getMaxTimeInHive());

                    if (iCustomBee.getBeeData().getColorData().hasPrimaryColor()) {
                        beeColor = iCustomBee.getBeeData().getColorData().getPrimaryColor();
                    } else if (iCustomBee.getBeeData().getColorData().isRainbowBee()) {
                        beeColor = RAINBOW_COLOR;
                    } else if (iCustomBee.getBeeData().getColorData().hasHoneycombColor()) {
                        beeColor = iCustomBee.getBeeData().getColorData().getHoneycombColor();
                    }
                }

                int finalMaxTimeInHive = maxTimeInHive;
                String finalType = String.valueOf(type);

                String finalBeeColor = beeColor;

                ITextComponent displayName = bee.getName();

                int number2 = (int) (MIN_HIVE_TIME * (1 - CreativeApiaryConfig.TCREATIVE_APIARY_SPEED.get()));
                this.bees.add(new CreativeApiaryBee(nbt, ticksInHive, hasNectar ? finalMaxTimeInHive : number2, beeEntity.getSavedFlowerPos(), finalType, finalBeeColor, displayName));
                BlockPos pos = this.getBlockPos();
                this.level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (imported) {
                    this.bees.get(Integer.valueOf(type)).setLocked(true);
                }

                if (this.getNumPlayersUsing() > 0)
                    syncApiaryToPlayersUsing(this.level, this.getBlockPos(), this.saveToNBT(new CompoundNBT()));

                bee.remove();
                return true;
            }
        }
        return false;
    }

    private int getMaxTimeInHive(int timeInput) {
        int number = (int) (timeInput * (1 - CreativeApiaryConfig.TCREATIVE_APIARY_SPEED.get()));
        return (number);
    }

    @Override
    public void tick() {
        if (level != null) {
            BlockPos blockpos = this.getBlockPos();
            int x = blockpos.getX();
            int y = blockpos.getY();
            int z = blockpos.getZ();
            ++ticksSinceSync;
            this.setNumPlayersUsing(calculatePlayersUsingSync(this.level, this, this.ticksSinceSync, x, y, z, this.getNumPlayersUsing()));

            this.tickBees();

            if (!level.isClientSide && isValidApiary) {
                if (ticksSinceValidation >= 20) runStructureValidation(null);
                else ticksSinceValidation++;

                if (this.bees.size() > 0 && this.level.getRandom().nextDouble() < 0.005D) {
                    double d0 = blockpos.getX() + 0.5D;
                    double d1 = blockpos.getY();
                    double d2 = blockpos.getZ() + 0.5D;
                    this.level.playSound(null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                ticksSinceBeesFlagged++;
                if (ticksSinceBeesFlagged == 80) {
                    BeeInfoUtils.flagBeesInRange(worldPosition, level);
                    ticksSinceBeesFlagged = 0;
                }
            }
        }
    }

    private void tickBees() {
        if (this.level != null) {
            Iterator<CreativeApiaryBee> iterator = this.bees.iterator();
            BlockState blockstate = this.getBlockState();

            while (iterator.hasNext()) {
                Map.Entry<String, CreativeApiaryBee> element = (Map.Entry)iterator.next();
                CreativeApiaryTileEntity.CreativeApiaryBee apiaryBee = element.getValue();
                if (!apiaryBee.isLocked() && apiaryBee.getTicksInHive() > apiaryBee.minOccupationTicks && !level.isClientSide) {

                    if (this.releaseBee(blockstate, apiaryBee, false)) {
                        iterator.remove();
                        if (this.getNumPlayersUsing() > 0 && !this.level.isClientSide)
                            syncApiaryToPlayersUsing(this.level, this.getBlockPos(), this.saveToNBT(new CompoundNBT()));
                    }
                } else {
                    apiaryBee.setTicksInHive(apiaryBee.getTicksInHive() + 1);
                    apiaryBee.setTicksInHive(Math.min(apiaryBee.getTicksInHive(), Integer.MAX_VALUE - 1));
                }
            }
        }
    }

    @Override
    public boolean isFullOfBees() {
        return this.bees.size() >= getMaxBees();
    }

    @Override
    public boolean isAllowedBee() {
        Block hive = getBlockState().getBlock();
        return isValidApiary(false) && hive instanceof CreativeApiaryBlock;
    }

    //@Override
    public void lockOrUnlockBee(int beeType) {
        this.bees.get(beeType).setLocked(!this.bees.get(beeType).isLocked());
        syncApiaryToPlayersUsing(this.level, this.getBlockPos(), this.saveToNBT(new CompoundNBT()));
    }
    //endregion

    //region NBT HANDLING

    @Nonnull
    public ListNBT writeBees() {
        ListNBT listnbt = new ListNBT();

        for(CreativeApiaryBee apiaryBee : this.bees) {
        //this.bees.forEach((key, apiaryBee) -> {
            apiaryBee.entityData.remove("UUID");
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.put("EntityData", apiaryBee.entityData);
            compoundnbt.putInt("TicksInHive", apiaryBee.getTicksInHive());
            compoundnbt.putInt("MinOccupationTicks", apiaryBee.minOccupationTicks);
            compoundnbt.putBoolean(NBTConstants.NBT_LOCKED, apiaryBee.isLocked());
            compoundnbt.putString(NBTConstants.NBT_BEE_TYPE, apiaryBee.beeType);
            compoundnbt.putString(NBTConstants.NBT_COLOR, apiaryBee.beeColor);
            compoundnbt.putString(NBTConstants.NBT_BEE_NAME, ITextComponent.Serializer.toJson(apiaryBee.displayName));
            if (apiaryBee.savedFlowerPos != null) {
                compoundnbt.put(NBTConstants.NBT_FLOWER_POS, NBTUtil.writeBlockPos(apiaryBee.savedFlowerPos));
            }
            listnbt.add(compoundnbt);
        }//);

        return listnbt;
    }

    @Override
    public void loadBees(CompoundNBT nbt) {
        ListNBT listnbt = nbt.getList(NBTConstants.NBT_BEES, 10);

        if (!listnbt.isEmpty()) {
            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT data = listnbt.getCompound(i);

                BlockPos savedFlowerPos = data.contains(NBTConstants.NBT_FLOWER_POS) ? NBTUtil.readBlockPos(data.getCompound(NBTConstants.NBT_FLOWER_POS)) : null;
                String beeType = data.getString(NBTConstants.NBT_BEE_TYPE);
                String beeColor = data.contains(NBTConstants.NBT_COLOR) ? data.getString(NBTConstants.NBT_COLOR) : VANILLA_BEE_COLOR;
                ITextComponent displayName = data.contains(NBTConstants.NBT_BEE_NAME) ? ITextComponent.Serializer.fromJson(data.getString(NBTConstants.NBT_BEE_NAME)) : new StringTextComponent("Temp Bee Name");

                this.bees.add(Integer.valueOf(data.getString(NBTConstants.NBT_BEE_TYPE)), new CreativeApiaryBee(
                        data.getCompound("EntityData"),
                        data.getInt("TicksInHive"),
                        data.getInt("MinOccupationTicks"),
                        savedFlowerPos,
                        beeType,
                        beeColor,
                        displayName));

                this.bees.get(Integer.valueOf(beeType)).setLocked(data.getBoolean(NBTConstants.NBT_LOCKED));
            }
        }
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromNBT(nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        super.save(nbt);
        return this.saveToNBT(nbt);
    }

    @Override
    public void loadFromNBT(CompoundNBT nbt) {
        loadBees(nbt);

        if (nbt.contains(NBTConstants.NBT_VALID_APIARY))
            this.isValidApiary = nbt.getBoolean(NBTConstants.NBT_VALID_APIARY);
        if (nbt.contains(NBTConstants.NBT_VERT_OFFSET))
            this.setVerticalOffset(nbt.getInt(NBTConstants.NBT_VERT_OFFSET));
        if (nbt.contains(NBTConstants.NBT_HOR_OFFSET))
            this.setHorizontalOffset(nbt.getInt(NBTConstants.NBT_HOR_OFFSET));
        CompoundNBT invTag = nbt.getCompound(NBTConstants.NBT_INVENTORY);
        getTileStackHandler().deserializeNBT(invTag);

        if (nbt.contains(NBTConstants.NBT_STORAGE_POS))
            setStoragePos(NBTUtil.readBlockPos(nbt.getCompound(NBTConstants.NBT_STORAGE_POS)));
        if (nbt.contains(NBTConstants.NBT_BREEDER_POS))
            setBreederPos(NBTUtil.readBlockPos(nbt.getCompound(NBTConstants.NBT_BREEDER_POS)));
        if (nbt.contains(NBTConstants.NBT_TIER)) {
            setTier(nbt.getInt(NBTConstants.NBT_TIER));
        }
        validateBees();
    }

    private void validateBees() {
        if (this.level == null) return;
        for(CreativeApiaryBee apiaryBee : this.bees) {
        //bees.forEach((s, apiaryBee) -> {
            String id = apiaryBee.entityData.getString("id");
            EntityType<?> type = BeeInfoUtils.getEntityType(id);
            if (type == EntityType.PIG) bees.remove(apiaryBee);
        }//);
    }

    @Override
    public CompoundNBT saveToNBT(CompoundNBT nbt) {
        CompoundNBT inv = this.getTileStackHandler().serializeNBT();
        nbt.put(NBTConstants.NBT_INVENTORY, inv);
        nbt.put(NBTConstants.NBT_BEES, this.writeBees());
        nbt.putBoolean(NBTConstants.NBT_VALID_APIARY, isValidApiary);
        nbt.putInt(NBTConstants.NBT_VERT_OFFSET, getVerticalOffset());
        nbt.putInt(NBTConstants.NBT_HOR_OFFSET, getHorizontalOffset());
        if (getStoragePos() != null)
            nbt.put(NBTConstants.NBT_STORAGE_POS, NBTUtil.writeBlockPos(getStoragePos()));
        if (getBreederPos() != null)
            nbt.put(NBTConstants.NBT_BREEDER_POS, NBTUtil.writeBlockPos(getBreederPos()));
        nbt.putInt(NBTConstants.NBT_TIER, getTier());
        return nbt;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        save(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(@Nonnull BlockState state, CompoundNBT tag) {
        this.load(state, tag);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return super.getUpdatePacket();
    }
    //endregion

    //region IMPORT/EXPORT
    @Override
    public void importBee(ServerPlayerEntity player) {
        World world = this.level;
        boolean imported = false;
        if (world != null && !this.getTileStackHandler().getStackInSlot(IMPORT).isEmpty() && this.getTileStackHandler().getStackInSlot(EMPTY_JAR).getCount() < 16) {
            ItemStack filledJar = this.getTileStackHandler().getStackInSlot(IMPORT);
            ItemStack emptyJar = this.getTileStackHandler().getStackInSlot(EMPTY_JAR);

            if (filledJar.getItem() instanceof BeeJar) {
                BeeJar jarItem = (BeeJar) filledJar.getItem();
                Entity entity = jarItem.getEntityFromStack(filledJar, world, true);

                if (entity instanceof BeeEntity) {
                    BeeEntity beeEntity = (BeeEntity) entity;
                    imported = tryEnterHive(beeEntity, beeEntity.hasNectar(), true);

                    if (imported) {
                        filledJar.shrink(1);
                        if (emptyJar.isEmpty()) {
                            this.getTileStackHandler().setStackInSlot(EMPTY_JAR, new ItemStack(jarItem));
                        } else {
                            emptyJar.grow(1);
                        }
                    }
                }
            }
        }

        player.displayClientMessage(new TranslationTextComponent("gui.resourcefulbees.apiary.import." + imported), true);
    }

    //@Override
    public void exportBee(ServerPlayerEntity player, int beeType) {
        boolean exported = false;
        CreativeApiaryBee bee = this.bees.get(beeType);

        if (bee.isLocked() && getTileStackHandler().getStackInSlot(EXPORT).isEmpty() && !getTileStackHandler().getStackInSlot(EMPTY_JAR).isEmpty()) {
            exported = releaseBee(this.getBlockState(), bee, true);
        }
        if (exported) {
            this.bees.remove(beeType);
            this.getTileStackHandler().getStackInSlot(EMPTY_JAR).shrink(1);
            if (this.getNumPlayersUsing() > 0 && this.level != null && !this.level.isClientSide)
                syncApiaryToPlayersUsing(this.level, this.getBlockPos(), this.saveToNBT(new CompoundNBT()));
        }

        player.displayClientMessage(new TranslationTextComponent("gui.resourcefulbees.apiary.export." + exported), true);
    }

    @Override
    public void exportBee(BeeEntity beeEntity) {
        ItemStack beeJar = new ItemStack(ModItems.BEE_JAR.get());
        beeJar.setTag(BeeInfoUtils.createJarBeeTag(beeEntity, NBTConstants.NBT_ENTITY));
        BeeJar.renameJar(beeJar, beeEntity);
        this.getTileStackHandler().setStackInSlot(EXPORT, beeJar);
    }
    //endregion

    //region STRUCTURE VALIDATION
    @Override
    public void runStructureValidation(@Nullable ServerPlayerEntity validatingPlayer) {
        if (this.level != null && !this.level.isClientSide()) {
            if (!this.isValidApiary || structureBlocks.isEmpty())
                buildStructureBlockList();
            this.isValidApiary = validateStructure(this.level, validatingPlayer);
            this.level.setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(CreativeApiaryBlock.VALIDATED, this.isValidApiary));
            if (validatingPlayer != null && this.isValidApiary) {
                NetworkHooks.openGui(validatingPlayer, this, this.getBlockPos());
            }
            this.ticksSinceValidation = 0;
        }
    }

    @Override
    public boolean validateStructure(World worldIn, @Nullable ServerPlayerEntity validatingPlayer) {
        AtomicBoolean isStructureValid = new AtomicBoolean(true);
        this.apiaryStorage = getApiaryStorage2();
        this.apiaryBreeder = getApiaryBreeder2();
        validateLinks();
        isStructureValid.set(validateBlocks(isStructureValid, worldIn, validatingPlayer));

        if (apiaryStorage == null) {
            isStructureValid.set(false);
            if (validatingPlayer != null) {
                validatingPlayer.displayClientMessage(new StringTextComponent("Missing Creative Apiary Storage Block!"), false);
            }
        }

        if (validatingPlayer != null) {
            validatingPlayer.displayClientMessage(new TranslationTextComponent("gui.resourcefulbees.apiary.validated." + isStructureValid.get()), true);
        }
        return isStructureValid.get();
    }

    private boolean validateBlocks(AtomicBoolean isStructureValid, World worldIn, @Nullable ServerPlayerEntity validatingPlayer) {
        structureBlocks.forEach(pos -> {
            Block block = worldIn.getBlockState(pos).getBlock();
            if (block.is(BeeInfoUtils.getValidApiaryTag()) || block instanceof ApiaryBreederBlock || block instanceof CreativeApiaryStorageBlock || block instanceof CreativeApiaryBlock) {
                TileEntity tile = worldIn.getBlockEntity(pos);
                linkStorageAndBreeder(tile);
            } else {
                isStructureValid.set(false);
                if (validatingPlayer != null)
                    validatingPlayer.displayClientMessage(new StringTextComponent(String.format("Block at position (X: %1$s Y: %2$s Z: %3$s) is invalid!", pos.getX(), pos.getY(), pos.getZ())), false);
            }
        });

        return isStructureValid.get();
    }


    @Override
    public MutableBoundingBox buildStructureBounds(int horizontalOffset, int verticalOffset) {
        return MultiBlockHelper.buildStructureBounds(this.getBlockPos(), 7, 6, 7, -horizontalOffset - 3, -verticalOffset - 2, 0, this.getBlockState().getValue(CreativeApiaryBlock.FACING));
    }

    private void buildStructureBlockList() {
        if (this.level != null) {
            MutableBoundingBox box = buildStructureBounds(this.getHorizontalOffset(), this.getVerticalOffset());
            structureBlocks.clear();
            BlockPos.betweenClosedStream(box).forEach((blockPos -> {
                if (blockPos.getX() == box.x0 || blockPos.getX() == box.x1 ||
                        blockPos.getY() == box.y0 || blockPos.getY() == box.y1 ||
                        blockPos.getZ() == box.z0 || blockPos.getZ() == box.z1) {
                    BlockPos savedPos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    structureBlocks.add(savedPos);
                }
            }));
        }
    }

    @Override
    public void runCreativeBuild(ServerPlayerEntity player) {
        if (this.level != null) {
            buildStructureBlockList();
            boolean addedStorage = false;
            for (BlockPos pos : structureBlocks) {
                Block block = this.level.getBlockState(pos).getBlock();
                if (!(block instanceof CreativeApiaryBlock)) {
                    if (addedStorage) {
                        this.level.setBlockAndUpdate(pos, net.minecraft.block.Blocks.GLASS.defaultBlockState());
                    } else {
                        this.level.setBlockAndUpdate(pos, ModBlocks.CREATIVE_APIARY_STORAGE_BLOCK.get().defaultBlockState());
                        addedStorage = true;
                    }
                }
            }
            runStructureValidation(player);
        }
    }

    @Override
    public boolean linkStorageAndBreeder(TileEntity tile) {
        if (tile instanceof CreativeApiaryStorageTileEntity && apiaryStorage == null && ((CreativeApiaryStorageTileEntity) tile).getApiaryPos() == null) {
            apiaryStorage = (CreativeApiaryStorageTileEntity) tile;
            setStoragePos(apiaryStorage.getBlockPos());
            apiaryStorage.setApiaryPos(this.worldPosition);
            if (level != null) {
                level.sendBlockUpdated(getStoragePos(), apiaryStorage.getBlockState(), apiaryStorage.getBlockState(), 2);
            }
            return true;
        }

        if (tile instanceof CreativeApiaryBreederTileEntity && apiaryBreeder == null && ((CreativeApiaryBreederTileEntity) tile).getApiaryPos() == null) {
            apiaryBreeder = (CreativeApiaryBreederTileEntity) tile;
            setBreederPos(apiaryBreeder.getBlockPos());
            apiaryBreeder.setApiaryPos(this.worldPosition);
            if (level != null) {
                level.sendBlockUpdated(getBreederPos(), apiaryBreeder.getBlockState(), apiaryBreeder.getBlockState(), 2);
            }
            return true;
        }

        return false;
    }

    private boolean validateLinks() {
        boolean linksValid = true;
        if (apiaryStorage != null && (apiaryStorage.getApiaryPos() == null || positionsMismatch(apiaryStorage.getApiaryPos()))) {
            apiaryStorage = null;
            setStoragePos(null);
            linksValid = false;
        }
        if (apiaryBreeder != null && (apiaryBreeder.getApiaryPos() == null || positionsMismatch(apiaryBreeder.getApiaryPos()))) {
            apiaryBreeder = null;
            setBreederPos(null);
            linksValid = false;
        }
        if (!linksValid) {
            setChanged();
        }
        return linksValid;
    }

    private boolean positionsMismatch(BlockPos pos) {
        return pos.compareTo(this.worldPosition) != 0;
    }
    //endregion

    //region SCREEN HANDLING
    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        if (level != null) {
            setNumPlayersUsing(getNumPlayersUsing() + 1);
            if (isValidApiary(true)) {
                return new ValidatedCreativeApiaryContainer(i, level, worldPosition, playerInventory);
            }
            return new UnvalidatedCreativeApiaryContainer(i, level, worldPosition, playerInventory);
        }
        return null;
    }

    @Override
    public void switchTab(ServerPlayerEntity player, ApiaryTabs tab) {
        if (level != null) {
            if (tab == ApiaryTabs.STORAGE) {
                NetworkHooks.openGui(player, getApiaryStorage2(), getStoragePos());
            }
            if (tab == ApiaryTabs.BREED) {
                NetworkHooks.openGui(player, getApiaryBreeder2(), getBreederPos());
            }
        }
    }

    //endregion

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? getLazyOptional().cast() :
                super.getCapability(cap, side);
    }

    public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation || slot == 0 || slot == 1;
    }

    public AutomationSensitiveItemStackHandler.IRemover getRemover() {
        return (slot, automation) -> !automation || slot == 1 || slot == 2;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.creativeapiary.creative_apiary");
    }

    public boolean isPreviewed() {
        return previewed;
    }

    public void setPreviewed(boolean previewed) {
        this.previewed = previewed;
    }

    @NotNull
    public CreativeApiaryTileEntity.TileStackHandler getTileStackHandler2() {
        return this.tileStackHandler;
    }

    public LazyOptional<IItemHandler> getLazyOptional() {
        return lazyOptional;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    public void setHorizontalOffset(int horizontalOffset) {
        this.horizontalOffset = horizontalOffset;
    }

    public int getVerticalOffset() {
        return verticalOffset;
    }

    public void setVerticalOffset(int verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    public int getNumPlayersUsing() {
        return numPlayersUsing;
    }

    public void setNumPlayersUsing(int numPlayersUsing) {
        this.numPlayersUsing = numPlayersUsing;
    }

    public BlockPos getStoragePos() {
        return storagePos;
    }

    public void setStoragePos(BlockPos storagePos) {
        this.storagePos = storagePos;
    }

    public BlockPos getBreederPos() {
        return breederPos;
    }

    public void setBreederPos(BlockPos breederPos) {
        this.breederPos = breederPos;
    }

    public enum State {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY
    }

    public static class CreativeApiaryBee {
        public final CompoundNBT entityData;
        public final int minOccupationTicks;
        public final BlockPos savedFlowerPos;
        public final String beeType;
        private int ticksInHive;
        private boolean isLocked = false;
        public final String beeColor;
        public final ITextComponent displayName;

        public CreativeApiaryBee(CompoundNBT nbt, int ticksInHive, int minOccupationTicks, @Nullable BlockPos flowerPos, String beeType, String beeColor, ITextComponent displayName) {
            nbt.remove("UUID");
            this.entityData = nbt;
            this.setTicksInHive(ticksInHive);
            this.minOccupationTicks = minOccupationTicks;
            this.savedFlowerPos = flowerPos;
            this.beeType = beeType;
            this.beeColor = beeColor;
            this.displayName = displayName;
        }

        public int getTicksInHive() {
            return ticksInHive;
        }

        public void setTicksInHive(int ticksInHive) {
            this.ticksInHive = ticksInHive;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public void setLocked(boolean locked) {
            isLocked = locked;
        }
    }

    public class TileStackHandler extends AutomationSensitiveItemStackHandler {

        protected TileStackHandler(int slots) {
            super(slots);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == IMPORT) {
                return stack.getItem() instanceof BeeJar && BeeJar.isFilled(stack);
            } else if (slot == EMPTY_JAR) {
                return stack.getItem() instanceof BeeJar && !BeeJar.isFilled(stack);
            } else {
                return false;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == IMPORT) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }

        @Override
        public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
            return CreativeApiaryTileEntity.this.getAcceptor();
        }

        @Override
        public AutomationSensitiveItemStackHandler.IRemover getRemover() {
            return CreativeApiaryTileEntity.this.getRemover();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    }
    //tryenterhive, releaseBee
}