package net.john.bioreactor.content.mixin.watercreate;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor Mixin for "onFluidStackChanged" in FluidTankBlockEntity
 * so we can invoke it from the creative tank mixin.
 */
@Mixin(FluidTankBlockEntity.class)
public interface FluidTankBlockEntityAccessorMixin {

    @Invoker("onFluidStackChanged")
    void bioreactor_callOnFluidStackChanged(FluidStack stack);
}
