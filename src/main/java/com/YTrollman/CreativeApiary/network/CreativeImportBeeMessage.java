package com.YTrollman.CreativeApiary.network;

import java.util.function.Supplier;

import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreativeImportBeeMessage {

    private final BlockPos pos;

    public CreativeImportBeeMessage(BlockPos pos){
        this.pos = pos;
    }

    public static void encode(CreativeImportBeeMessage message, PacketBuffer buffer){
        buffer.writeBlockPos(message.pos);
    }

    public static CreativeImportBeeMessage decode(PacketBuffer buffer){
        return new CreativeImportBeeMessage(buffer.readBlockPos());
    }

    @SuppressWarnings("deprecation")
	public static void handle(CreativeImportBeeMessage message, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null) {
                if (player.level.hasChunkAt(message.pos)) {
                    TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                    if (tileEntity instanceof CreativeApiaryTileEntity) {
                        CreativeApiaryTileEntity apiaryTileEntity = (CreativeApiaryTileEntity) tileEntity;
                        apiaryTileEntity.importBee(player);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
