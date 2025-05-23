package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.BioreactorNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkWatcher {

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ServerPlayer player = event.getPlayer();
        ServerLevel serverLevel = player.serverLevel();
        LevelChunk chunk = serverLevel.getChunk(event.getPos().x, event.getPos().z);

        chunk.getCapability(WaterChunkCapability.WATER_DATA_CAP).ifPresent(cap -> {
            Map<Long, int[]> mapToSend = new HashMap<>();
            cap.getMap().forEach((posLong, wData) -> {
                mapToSend.put(posLong, new int[]{ wData.getPH(), wData.getSalinity() });
            });

            var packet = new WaterChunkSyncPacket(chunk.getPos(), mapToSend);
            BioreactorNetwork.sendToPlayer(packet, player);
        });
    }
}
