package net.john.bioreactor.foundation.waterphsalinity.create;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.FluidNBTHelper;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkCapability;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * A custom SmartFluidTank that merges chunk-based pH/sal only if
 * the fluid stack lacks pH. This prevents overwriting water
 * that already has a valid pH from the pipeline.
 */
public class BioreactorFluidTank extends SmartFluidTank {

    private final Level level;
    private final BlockPos pos;

    public BioreactorFluidTank(int capacity, Consumer<FluidStack> updateCallback,
                               Level level, BlockPos pos) {
        super(capacity, updateCallback);
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // Only do chunk-based logic if fluid is water
        if (!resource.isEmpty() && resource.getFluid().isSame(Fluids.WATER)) {

            // If the fluid already has "pH", do NOTHING
            // => we won't forcibly overwrite it with fallback pH=13.
            CompoundTag fluidTag = resource.getOrCreateTag();
            if (!fluidTag.contains("pH")) {

                // fluid has no pH => read chunk data
                WaterChunkCapability.WaterData data = WaterDataHelper.get(level, pos);
                CompoundTag storedTag = new CompoundTag();
                if (data != null) {
                    storedTag.putInt("pH", data.getPH());
                    storedTag.putInt("salinity", data.getSalinity());
                } else {
                    // fallback if chunk has no data
                    storedTag.putInt("pH", 7);
                    storedTag.putInt("salinity", 1);
                }
                // Merge chunk data
                CompoundTag merged = FluidNBTHelper.mergeWaterNBT(fluidTag, storedTag);
                resource.setTag(merged);
            }
        }

        return super.fill(resource, action);
    }
}
