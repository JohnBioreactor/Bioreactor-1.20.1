package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.nbt.CompoundTag;

public class FluidNBTHelper {

    /**
     * Merges the custom NBT keys "pH" and "salinity" from the extra tag into the base tag.
     * If the extra tag contains these keys, they override what is in base.
     *
     * @param base  The base CompoundTag (from the FluidStack)
     * @param extra The extra CompoundTag (typically built from stored water data)
     * @return The merged CompoundTag
     */
    public static CompoundTag mergeWaterNBT(CompoundTag base, CompoundTag extra) {
//        System.out.println("[FluidNBTHelper] mergeWaterNBT called.");
//        System.out.println("[FluidNBTHelper] Original base tag: " + base);
        if (base == null) {
            base = new CompoundTag();
        }
        if (extra == null) {
//            System.out.println("[FluidNBTHelper] Extra tag is null; returning base.");
            return base;
        }
        if (extra.contains("pH")) {
            int pH = extra.getInt("pH");
//            System.out.println("[FluidNBTHelper] Merging pH from stored data: " + pH);
            base.putInt("pH", pH);
        } else {
//            System.out.println("[FluidNBTHelper] No pH found in stored data; defaulting to 7");
            base.putInt("pH", 7);
        }
        if (extra.contains("salinity")) {
            int salinity = extra.getInt("salinity");
//            System.out.println("[FluidNBTHelper] Merging salinity from stored data: " + salinity);
            base.putInt("salinity", salinity);
        } else {
//            System.out.println("[FluidNBTHelper] No salinity found in stored data; defaulting to 0");
            base.putInt("salinity", 0);
        }
//        System.out.println("[FluidNBTHelper] Merged tag: " + base);
        return base;
    }
}
