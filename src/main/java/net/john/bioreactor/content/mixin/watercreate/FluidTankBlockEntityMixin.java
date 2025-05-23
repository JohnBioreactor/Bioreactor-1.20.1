package net.john.bioreactor.content.mixin.watercreate;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.john.bioreactor.foundation.waterphsalinity.create.BioreactorFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Replaces the default inventory with BioreactorFluidTank that
 * merges chunk-based pH only if fluid lacks pH.
 */
@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin {

    // Overwrite the "createInventory()" method in FluidTankBlockEntity
    @Overwrite(remap = false)
    protected SmartFluidTank createInventory() {
        Level level = ((net.minecraft.world.level.block.entity.BlockEntity)(Object)this).getLevel();
        BlockPos pos = ((net.minecraft.world.level.block.entity.BlockEntity)(Object)this).getBlockPos();

        System.out.println("[FluidTankBlockEntityMixin] createInventory => Using BioreactorFluidTank at " + pos);

        // capacity = FluidTankBlockEntity.getCapacityMultiplier() * #blocks
        // but getCapacityMultiplier is a static, so we do:
        return new BioreactorFluidTank(
                FluidTankBlockEntity.getCapacityMultiplier(),
                this::invokeOnFluidStackChanged,
                level,
                pos
        );
    }

    // We need an invoker for the protected method "onFluidStackChanged"
    @Invoker(value = "onFluidStackChanged", remap = false)
    public abstract void invokeOnFluidStackChanged(FluidStack stack);
}
