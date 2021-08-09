package com.YTrollman.CreativeApiary.block;

import com.YTrollman.CreativeApiary.config.CreativeApiaryConfig;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryBreederTileEntity;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.utils.TooltipBuilder;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class CreativeApiaryBreederBlock extends Block {
    public CreativeApiaryBreederBlock(Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.stateDefinition.any());
    }

    @Nonnull
    public ActionResultType use(@Nonnull BlockState state, @NotNull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult blockRayTraceResult) {
        if (!player.isShiftKeyDown() && !world.isClientSide) {
            INamedContainerProvider blockEntity = state.getMenuProvider(world, pos);
            NetworkHooks.openGui((ServerPlayerEntity)player, blockEntity, pos);
        }

        return ActionResultType.SUCCESS;
    }

    @Nullable
    public INamedContainerProvider getMenuProvider(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos) {
        return (INamedContainerProvider)worldIn.getBlockEntity(pos);
    }

    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CreativeApiaryBreederTileEntity();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        int number = (int) (Config.APIARY_MAX_BREED_TIME.get() * (1 - CreativeApiaryConfig.CREATIVE_APIARY_BREEDER_SPEED.get()));
        tooltip.addAll((new TooltipBuilder()).addTip(I18n.get("block.creativeapiary.creative_apiary_breeder.tooltip.info"), TextFormatting.GOLD).addTip(I18n.get("block.resourcefulbees.apiary_breeder.tooltip.info1"), TextFormatting.GOLD).appendText(String.format("%1$s ticks", number), TextFormatting.GOLD).addTip(I18n.get("block.creativeapiary.creative_apiary_breeder.tooltip.info2"), TextFormatting.GOLD).build());
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
