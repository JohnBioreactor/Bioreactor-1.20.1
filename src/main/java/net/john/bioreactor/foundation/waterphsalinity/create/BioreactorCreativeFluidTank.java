package net.john.bioreactor.foundation.waterphsalinity.create;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
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
 * A custom "creative" fluid tank that merges chunk-based pH/sal
 * only if the fluid lacks pH.
 * If the fluid already has pH, we leave it as is.
 */
public class BioreactorCreativeFluidTank extends CreativeSmartFluidTank {

    private final Level level;
    private final BlockPos pos;

    public BioreactorCreativeFluidTank(int capacity, Consumer<FluidStack> updateCallback,
                                       Level level, BlockPos pos) {
        super(capacity, updateCallback);
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!resource.isEmpty() && resource.getFluid().isSame(Fluids.WATER)) {
            CompoundTag fluidTag = resource.getOrCreateTag();
            // Only fallback if pH is missing
            if (!fluidTag.contains("pH")) {
                WaterChunkCapability.WaterData data = WaterDataHelper.get(level, pos);
                CompoundTag storedTag = new CompoundTag();

                if (data != null) {
                    storedTag.putInt("pH", data.getPH());
                    storedTag.putInt("salinity", data.getSalinity());
                } else {
                    // fallback if chunk has no data
                    storedTag.putInt("pH", 13);
                    storedTag.putInt("salinity", 1);
                }

                CompoundTag merged = FluidNBTHelper.mergeWaterNBT(fluidTag, storedTag);
                resource.setTag(merged);
            }
        }

        // Then let normal CreativeSmartFluidTank logic do its infinite fill
        return super.fill(resource, action);
    }
}
