package net.john.bioreactor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class BioreactorNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;
    private static int packetId = 0;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Bioreactor.MOD_ID, "network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(
                packetId++,
                net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkSyncPacket.class,
                net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkSyncPacket::encode,
                net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkSyncPacket::new,
                net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkSyncPacket::handle
        );
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
