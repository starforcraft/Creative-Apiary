package com.YTrollman.CreativeApiary.network;

import com.YTrollman.CreativeApiary.CreativeApiary;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class CreativeNetPacketHandler {

    private static int id = 0;
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreativeApiary.MOD_ID, "main_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(++id, CreativeValidateApiaryMessage.class, CreativeValidateApiaryMessage::encode, CreativeValidateApiaryMessage::decode, CreativeValidateApiaryMessage::handle);
        INSTANCE.registerMessage(++id, CreativeBuildApiaryMessage.class, CreativeBuildApiaryMessage::encode, CreativeBuildApiaryMessage::decode, CreativeBuildApiaryMessage::handle);
        INSTANCE.registerMessage(++id, CreativeUpdateClientApiaryMessage.class, CreativeUpdateClientApiaryMessage::encode, CreativeUpdateClientApiaryMessage::decode, CreativeUpdateClientApiaryMessage::handle);
        INSTANCE.registerMessage(++id, CreativeLockBeeMessage.class, CreativeLockBeeMessage::encode, CreativeLockBeeMessage::decode, CreativeLockBeeMessage::handle);
        INSTANCE.registerMessage(++id, CreativeExportBeeMessage.class, CreativeExportBeeMessage::encode, CreativeExportBeeMessage::decode, CreativeExportBeeMessage::handle);
        INSTANCE.registerMessage(++id, CreativeImportBeeMessage.class, CreativeImportBeeMessage::encode, CreativeImportBeeMessage::decode, CreativeImportBeeMessage::handle);
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }

    public static void sendToAllLoaded(Object message, World world, BlockPos pos) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), message);
    }

    public static void sendToPlayer(Object message, ServerPlayerEntity playerEntity) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerEntity), message);
    }
}
