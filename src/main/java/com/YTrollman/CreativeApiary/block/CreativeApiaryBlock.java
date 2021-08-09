package com.YTrollman.CreativeApiary.block;

import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;
import com.resourcefulbees.resourcefulbees.block.multiblocks.apiary.ApiaryBlock;
import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;
import com.resourcefulbees.resourcefulbees.utils.TooltipBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CreativeApiaryBlock extends ApiaryBlock {
  public static final DirectionProperty FACING;
  public static final BooleanProperty VALIDATED;
  private final int tier;

  public CreativeApiaryBlock(int tier, float hardness, float resistance) {
    super(100, hardness,resistance);
    this.tier = tier;
    this.registerDefaultState(this.stateDefinition.any().setValue(VALIDATED, false).setValue(FACING, Direction.NORTH));
  }

  @NotNull
  @Override
  public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
    if (!player.isShiftKeyDown() && !world.isClientSide) {
      INamedContainerProvider blockEntity = state.getMenuProvider(world, pos);
      NetworkHooks.openGui((ServerPlayerEntity)player, blockEntity, pos);
    }

    return ActionResultType.SUCCESS;
  }

  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    return context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()) : this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    builder.add(new Property[]{VALIDATED, FACING});
  }

  @Nullable
  @Override
  public INamedContainerProvider getMenuProvider(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos) {
    return (INamedContainerProvider)worldIn.getBlockEntity(pos);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CreativeApiaryTileEntity();
  }

  @Override
  public void setPlacedBy(World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
    TileEntity tile = worldIn.getBlockEntity(pos);
    if (tile instanceof CreativeApiaryTileEntity) {
      CreativeApiaryTileEntity apiaryTileEntity = (CreativeApiaryTileEntity)tile;
      apiaryTileEntity.setTier(this.tier);
    }
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
    if (Screen.hasShiftDown()) {
      tooltip.addAll((new TooltipBuilder()).addTip(I18n.get("block.resourcefulbees.beehive.tooltip.max_bees")).appendText(" " + CreativeApiaryConfig.TCREATIVE_APIARY_MAX_BEES.get()).appendText(TextFormatting.GOLD + " Bees", TextFormatting.RESET).applyStyle(TextFormatting.GOLD).build());

      int number = (int) (CreativeApiaryConfig.TCREATIVE_APIARY_SPEED.get() * 100);
      tooltip.addAll((new TooltipBuilder()).addTip(I18n.get("block.creativeapiary.creative_apiary.tooltip.speed") + (number) + "% tick reduction", TextFormatting.GOLD).build());


      int outputQuantity;
      ApiaryOutput outputTypeEnum;

      outputTypeEnum = (ApiaryOutput)CreativeApiaryConfig.TCREATIVE_APIARY_OUTPUT.get();
      outputQuantity = (Integer) CreativeApiaryConfig.TCREATIVE_APIARY_QUANTITY.get();

      String outputType = outputTypeEnum.equals(ApiaryOutput.COMB) ? I18n.get("honeycomb.resourcefulbees") : I18n.get("honeycomb_block.resourcefulbees");
      tooltip.addAll((new TooltipBuilder()).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.output_type")).appendText(" " + outputType).applyStyle(TextFormatting.GOLD).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.output_quantity")).appendText(" " + outputQuantity).applyStyle(TextFormatting.GOLD).build());
    } else if (Screen.hasControlDown()) {
      tooltip.addAll((new TooltipBuilder()).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.structure_size"), TextFormatting.AQUA).addTip(I18n.get("block.creativeapiary.creative_apiary.tooltip.requisites"), TextFormatting.AQUA).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.drops"), TextFormatting.AQUA).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.tags"), TextFormatting.AQUA).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.offset"), TextFormatting.AQUA).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.lock"), TextFormatting.AQUA).addTip(I18n.get("block.resourcefulbees.apiary.tooltip.lock_2"), TextFormatting.AQUA).build());
    } else {
      tooltip.add(new StringTextComponent(TextFormatting.YELLOW + I18n.get("resourcefulbees.shift_info")));
      tooltip.add(new StringTextComponent(TextFormatting.AQUA + I18n.get("resourcefulbees.ctrl_info")));
    }
  }

  static {
    FACING = HorizontalBlock.FACING;
    VALIDATED = BooleanProperty.create("validated");
  }
}
