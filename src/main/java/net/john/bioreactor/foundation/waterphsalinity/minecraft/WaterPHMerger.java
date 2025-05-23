package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

public class WaterPHMerger {

    public static int[] computeNeighborsOnlyPHSal(Level level, BlockPos centerPos,
                                                  int defaultPH, int defaultSal) {
        int totalPH = 0;
        int count = 0;
        boolean anySalOne = false;

        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos neighPos = centerPos.relative(d);
            FluidState neighFluid = level.getFluidState(neighPos);
            if (!neighFluid.isSource() || neighFluid.getType() != Fluids.WATER) {
                continue;
            }
            var neighData = WaterDataHelper.get(level, neighPos);
            if (neighData != null) {
                totalPH += neighData.getPH();
                if (neighData.getSalinity() == 1) anySalOne = true;
            } else {
                totalPH += 7;
                anySalOne = true;
            }
            count++;
        }

        if (count == 0) {
            return new int[] {defaultPH, defaultSal};
        }
        int avgPH = Math.round(totalPH / (float)count);
        int finalSal = anySalOne ? 1 : 0;
        return new int[] {avgPH, finalSal};
    }

    public static int[] computeAveragedPHSal(Level level, BlockPos centerPos,
                                             int basePH, int baseSal, boolean includeBase) {
        int totalPH = 0;
        int count = 0;
        boolean anySalOne = false;

        if (includeBase) {
            totalPH += basePH;
            count++;
            if (baseSal == 1) anySalOne = true;
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos neighPos = centerPos.relative(d);
            FluidState neighFluid = level.getFluidState(neighPos);
            if (!neighFluid.isSource() || neighFluid.getType() != Fluids.WATER) {
                continue;
            }
            var neighData = WaterDataHelper.get(level, neighPos);
            if (neighData != null) {
                totalPH += neighData.getPH();
                if (neighData.getSalinity() == 1) anySalOne = true;
            } else {
                totalPH += 7;
                anySalOne = true;
            }
            count++;
        }

        if (count == 0) {
            return new int[]{ basePH, baseSal };
        }

        int finalPH = Math.round(totalPH / (float) count);
        int finalSal = anySalOne ? 1 : 0;
        return new int[]{ finalPH, finalSal };
    }
}
