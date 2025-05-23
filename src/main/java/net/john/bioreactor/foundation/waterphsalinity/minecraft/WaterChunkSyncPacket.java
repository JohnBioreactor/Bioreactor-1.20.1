package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Carries the entire WaterChunkData from a single chunk (pH & sal)
 * from the server to the client. The client overwrites its local chunk
 * capability, so WaterDataHelper.get(...) returns the correct pH/sal.
 */
public class WaterChunkSyncPacket {

    private final int chunkX;
    private final int chunkZ;
    private final Map<Long, int[]> waterMap;
    // key = BlockPos.asLong(), value = [pH, sal]

    // Construct from a chunk position + entire map
    public WaterChunkSyncPacket(ChunkPos chunkPos, Map<Long, int[]> waterMap) {
        this.chunkX = chunkPos.x;
        this.chunkZ = chunkPos.z;
        this.waterMap = waterMap;
    }

    // Decoder constructor
    public WaterChunkSyncPacket(FriendlyByteBuf buf) {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        int size = buf.readVarInt();
        this.waterMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            long posLong = buf.readLong();
            int pH = buf.readInt();
            int sal = buf.readInt();
            waterMap.put(posLong, new int[]{pH, sal});
        }
    }

    // Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeVarInt(waterMap.size());
        for (Map.Entry<Long, int[]> e : waterMap.entrySet()) {
            buf.writeLong(e.getKey());
            int[] arr = e.getValue();
            buf.writeInt(arr[0]); // pH
            buf.writeInt(arr[1]); // sal
        }
    }

    // Handler on the client side
    public static void handle(WaterChunkSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isClient()) return;

            var client = net.minecraft.client.Minecraft.getInstance();
            Level level = client.level;
            if (level == null) return;

            if (!level.hasChunk(msg.chunkX, msg.chunkZ)) {
                return; // chunk not loaded yet
            }
            var chunk = level.getChunk(msg.chunkX, msg.chunkZ);

            // Overwrite local chunk capability
            chunk.getCapability(WaterChunkCapability.WATER_DATA_CAP).ifPresent(cap -> {
                cap.getMap().clear();
                msg.waterMap.forEach((posLong, pHSal) -> {
                    int pH = pHSal[0];
                    int sal = pHSal[1];
                    cap.setWaterData(net.minecraft.core.BlockPos.of(posLong), pH, sal);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
