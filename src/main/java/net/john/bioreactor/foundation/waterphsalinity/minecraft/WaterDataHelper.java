package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.john.bioreactor.BioreactorNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class WaterDataHelper {

    public static WaterChunkCapability.WaterData get(Level level, BlockPos pos) {
        if (level == null || pos == null) return null;
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.getCapability(WaterChunkCapability.WATER_DATA_CAP).resolve()
                .map(cap -> cap.getData(pos))
                .orElse(null);
    }

    public static boolean hasData(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.getCapability(WaterChunkCapability.WATER_DATA_CAP).resolve()
                .map(cap -> cap.hasDataFor(pos))
                .orElse(false);
    }

    public static void set(Level level, BlockPos pos, int ph, int sal) {
        if (level.isClientSide()) return;
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getCapability(WaterChunkCapability.WATER_DATA_CAP).ifPresent(cap -> {
            cap.setWaterData(pos, ph, sal);
            chunk.setUnsaved(true);

            // Build map
            Map<Long, int[]> mapToSend = new HashMap<>();
            cap.getMap().forEach((posLong, wData) -> {
                mapToSend.put(posLong, new int[]{ wData.getPH(), wData.getSalinity() });
            });

            var packet = new net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkSyncPacket(chunk.getPos(), mapToSend);

            // Send to all watchers
            var watchers = ((ServerLevel)level).getChunkSource().chunkMap.getPlayers(chunk.getPos(), false);
            for (ServerPlayer sp : watchers) {
                BioreactorNetwork.sendToPlayer(packet, sp);
            }
        });
    }

}
