package com.YTrollman.CreativeApiary.network;

import java.util.function.Supplier;

import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreativeBuildApiaryMessage {

    private final BlockPos pos;
    private final int verticalOffset;
    private final int horizontalOffset;

    public CreativeBuildApiaryMessage(BlockPos pos, int verticalOffset, int horizontalOffset){
        this.pos = pos;
        this.verticalOffset = verticalOffset;
        this.horizontalOffset = horizontalOffset;
    }

    public static void encode(CreativeBuildApiaryMessage message, PacketBuffer buffer){
        buffer.writeBlockPos(message.pos);
        buffer.writeInt(message.verticalOffset);
        buffer.writeInt(message.horizontalOffset);
    }

    public static CreativeBuildApiaryMessage decode(PacketBuffer buffer){
        return new CreativeBuildApiaryMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(CreativeBuildApiaryMessage message, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null && player.level.isLoaded(message.pos)) {
                TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                if (tileEntity instanceof CreativeApiaryTileEntity) {
                    CreativeApiaryTileEntity apiaryTileEntity = (CreativeApiaryTileEntity) tileEntity;
                    apiaryTileEntity.setVerticalOffset(message.verticalOffset);
                    apiaryTileEntity.setHorizontalOffset(message.horizontalOffset);
                    apiaryTileEntity.runCreativeBuild(player);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
