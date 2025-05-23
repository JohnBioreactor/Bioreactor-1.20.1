package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores pH & salinity for any water blocks within a chunk.
 * Key = BlockPos.asLong().
 */
public class WaterChunkData {

    private final Map<Long, WaterChunkCapability.WaterData> waterMap = new HashMap<>();

    public WaterChunkCapability.WaterData getData(BlockPos pos) {
        // We keep it silent or log lightly:
        // System.out.println("[WaterChunkData.getData] pos=" + pos + " => " + wd);
        return waterMap.get(pos.asLong());
    }

    public boolean hasDataFor(BlockPos pos) {
        // System.out.println("[WaterChunkData.hasDataFor] pos=" + pos + " => " + has);
        return waterMap.containsKey(pos.asLong());
    }

    /**
     * We remove the println or leave it commented out to avoid spamming
     * if many water blocks get updated:
     */
    public void setWaterData(BlockPos pos, int ph, int sal) {
        // System.out.println("[WaterChunkData.setWaterData] pos=" + pos
        //         + ", pH=" + ph + ", sal=" + sal);

        //--- The WaterData constructor handles clamping ---//
        waterMap.put(pos.asLong(), new WaterChunkCapability.WaterData(ph, sal));
    }

    public void removeWaterData(BlockPos pos) {
        // System.out.println("[WaterChunkData.removeWaterData] pos=" + pos);
        waterMap.remove(pos.asLong());
    }

    public Map<Long, WaterChunkCapability.WaterData> getMap() {
        return waterMap;
    }
}
