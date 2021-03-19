package com.YTrollman.CreativeApiary.network;

import java.util.function.Supplier;

import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreativeUpdateClientApiaryMessage {
    private final BlockPos pos;
    private final CompoundNBT data;

    public CreativeUpdateClientApiaryMessage(BlockPos pos, CompoundNBT data){
        this.pos = pos;
        this.data = data;
    }

    public static void encode(CreativeUpdateClientApiaryMessage message, PacketBuffer buffer){
        buffer.writeBlockPos(message.pos);
        buffer.writeNbt(message.data);
    }

    public static CreativeUpdateClientApiaryMessage decode(PacketBuffer buffer){
        return new CreativeUpdateClientApiaryMessage(buffer.readBlockPos(), buffer.readNbt());
    }

    public static void handle(CreativeUpdateClientApiaryMessage message, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                if (player.level.isLoaded(message.pos)) {
                    TileEntity tileEntity = player.level.getBlockEntity(message.pos);
                    if (tileEntity instanceof CreativeApiaryTileEntity) {
                        CreativeApiaryTileEntity apiaryTileEntity = (CreativeApiaryTileEntity) tileEntity;
                        apiaryTileEntity.bees.clear();
                        apiaryTileEntity.loadFromNBT(message.data);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}


