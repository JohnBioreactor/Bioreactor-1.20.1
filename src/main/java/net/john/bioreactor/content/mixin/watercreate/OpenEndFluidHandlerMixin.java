package net.john.bioreactor.content.mixin.watercreate;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to preserve custom water NBT in open-ended pipes.
 * OLD lines forcing pH=13 or salinity=1 are removed.
 */
@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler")
public abstract class OpenEndFluidHandlerMixin extends FluidTank {

    public OpenEndFluidHandlerMixin(int capacity) {
        super(capacity);
    }

    @Inject(method = "fill", at = @At("HEAD"), remap = false)
    private void onFill(FluidStack resource, FluidAction action, CallbackInfoReturnable<Integer> cir) {
        // Example: only log or do minimal merges if you want a fallback
        // But do NOT forcibly set pH=13 or salinity=1

//        System.out.println("[OpenEndFluidHandlerMixin] fill() called with resource: " + resource);

//        if (!resource.isEmpty() && resource.getFluid().isSame(Fluids.WATER)) {
//            // If the fluidStack is missing NBT, you can supply mild defaults, or do nothing
//            CompoundTag tag = resource.getOrCreateTag();
//            if (!tag.contains("pH")) {
//                tag.putInt("pH", 7); // mild fallback
//            }
//            if (!tag.contains("salinity")) {
//                tag.putInt("salinity", 0);
//            }
//            resource.setTag(tag);
//        }
    }
}
