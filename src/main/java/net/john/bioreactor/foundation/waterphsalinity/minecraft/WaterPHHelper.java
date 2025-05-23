package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * getPHAndSal => returns [pH, sal], or [-1,0] if not water
 * getPH => returns just pH
 */
public class WaterPHHelper {

    public static int[] getPHAndSal(Level level, BlockPos pos) {
        FluidState fs = level.getFluidState(pos);
        if (fs.isEmpty() ||
                (!fs.getType().isSame(Fluids.WATER) &&
                        !fs.getType().isSame(Fluids.FLOWING_WATER))) {
            return new int[] {-1, 0};
        }
        var data = WaterDataHelper.get(level, pos);
        if (data != null) {
            return new int[]{ data.getPH(), data.getSalinity() };
        }
        // fallback => neighbor merge
        int[] merged = WaterPHMerger.computeNeighborsOnlyPHSal(level, pos, 7, 1);
        return merged;
    }

    public static int getPH(Level level, BlockPos pos) {
        return getPHAndSal(level, pos)[0];
    }
}
