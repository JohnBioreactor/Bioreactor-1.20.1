package net.john.bioreactor.content.mixin.watercreate;

import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to preserve custom water NBT in CombinedTankWrapper fill operations.
 */
@Mixin(CombinedTankWrapper.class)
public abstract class CombinedTankWrapperMixin implements IFluidHandler {

    @Inject(method = "fill", at = @At("HEAD"), remap = false)
    private void onFill(FluidStack resource, FluidAction action, CallbackInfoReturnable<Integer> cir) {
//        System.out.println("[CombinedTankWrapperMixin] fill() with resource: " + resource);

        if (!resource.isEmpty() && resource.getFluid().isSame(Fluids.WATER)) {
            CompoundTag tag = resource.getOrCreateTag();
            // Example: do not forcibly overwrite. Only fill missing keys if desired.
            if (!tag.contains("pH")) {
                tag.putInt("pH", 7);
            }
            if (!tag.contains("salinity")) {
                tag.putInt("salinity", 0);
            }
            resource.setTag(tag);
        }
    }
}
