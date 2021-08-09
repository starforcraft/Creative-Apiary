package com.YTrollman.CreativeApiary.network;

import java.util.function.Supplier;

import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreativeExportBeeMessage {

    private final BlockPos pos;
    private final String beeType;

    public CreativeExportBeeMessage(BlockPos pos, String beeType){
        this.pos = pos;
        this.beeType = beeType;
    }

    public static void encode(CreativeExportBeeMessage message, PacketBuffer buffer){
        buffer.writeBlockPos(message.pos);
        buffer.writeUtf(message.beeType);
    }

    public static CreativeExportBeeMessage decode(PacketBuffer buffer){
        return new CreativeExportBeeMessage(buffer.readBlockPos(), buffer.readUtf(100));
    }

	public static void handle(CreativeExportBeeMessage message, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null && player.level.isLoaded(message.pos)) {
                TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                if (tileEntity instanceof CreativeApiaryTileEntity) {
                    CreativeApiaryTileEntity apiaryTileEntity = (CreativeApiaryTileEntity) tileEntity;
                    apiaryTileEntity.exportBee(player, message.beeType);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
