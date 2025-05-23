package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.Map;

public class WaterChunkCapability {

    public static final Capability<WaterChunkData> WATER_DATA_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * A simple data record for pH & salinity.
     */
    public static class WaterData {
        private int ph;
        private int salinity;

        public WaterData(int ph, int salinity) {
            this.ph = ph;
            this.salinity = salinity;
        }

        public int getPH() { return ph; }
        public void setPH(int value) {
            // Ensure pH is between 1 and 14
            this.ph = Math.max(1, Math.min(14, value));
        }

        public int getSalinity() { return salinity; }
        public void setSalinity(int value) {
            // salinity is 0 or 1
            this.salinity = (value == 1) ? 1 : 0;
        }

        @Override
        public String toString() {
            return "(pH=" + ph + ", sal=" + salinity + ")";
        }
    }

    /**
     * The chunk-level provider, storing one WaterChunkData and
     * handling read/write from NBT.
     */
    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final WaterChunkData data = new WaterChunkData();
        private final LazyOptional<WaterChunkData> lazyOpt = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == WATER_DATA_CAP) {
                return lazyOpt.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();

            for (Map.Entry<Long, WaterData> entry : data.getMap().entrySet()) {
                long posLong = entry.getKey();
                WaterData wd = entry.getValue();

                CompoundTag entryTag = new CompoundTag();
                entryTag.putLong("pos", posLong);
                entryTag.putInt("ph", wd.getPH());
                entryTag.putInt("sal", wd.getSalinity());
                list.add(entryTag);
            }
            root.put("entries", list);
            return root;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.getMap().clear();
            ListTag list = nbt.getList("entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                long posLong = entryTag.getLong("pos");
                int ph = Math.max(1, Math.min(14, entryTag.getInt("ph")));
                int sal = (entryTag.getInt("sal") == 1) ? 1 : 0;
                data.getMap().put(posLong, new WaterData(ph, sal));
            }
        }
    }
}
