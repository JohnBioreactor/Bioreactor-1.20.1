package net.john.bioreactor.content.mixin.watervanilla;

import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterPHMerger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * If a new water source forms without chunk data, we average from neighbor
 * source blocks. If no neighbor data => fallback = (7,1).
 */
@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onFluidTick(Level level, BlockPos pos, FluidState fluidState, CallbackInfo ci) {
        if (level.isClientSide) return;

        // If it's source water with no chunk data
        if (fluidState.getType() == Fluids.WATER && fluidState.isSource()) {
            if (!WaterDataHelper.hasData(level, pos)) {

                // We want to do scenario #2 => gather neighbors only
                int[] merged = WaterPHMerger.computeNeighborsOnlyPHSal(
                        level, pos,
                        7, 1  // fallback if no neighbors => pH=7, sal=1
                );
                int finalPH = merged[0];
                int finalSal = merged[1];

                WaterDataHelper.set(level, pos, finalPH, finalSal);
                //System.out.println("[FlowingFluidMixin] => Formed new water source => pH=" + finalPH + ", sal=" + finalSal);
            }
        }
    }
}
