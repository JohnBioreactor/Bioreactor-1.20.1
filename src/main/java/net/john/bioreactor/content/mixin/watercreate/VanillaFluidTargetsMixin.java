//package net.john.bioreactor.content.mixin.watercreate;
//
//import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
//import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkCapability;
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.material.Fluids;
//import net.minecraftforge.fluids.FluidStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import com.simibubi.create.content.fluids.pipes.VanillaFluidTargets;
//
//@Mixin(value = VanillaFluidTargets.class, remap = false)
//public abstract class VanillaFluidTargetsMixin {
//
//    /**
//     * Inject right after VanillaFluidTargets.drainBlock(...) returns its FluidStack.
//     */
//    @Inject(method = "drainBlock", at = @At("RETURN"), cancellable = true)
//    private static void afterDrainBlock(Level world, BlockPos pos,
//                                        net.minecraft.world.level.block.state.BlockState state,
//                                        boolean simulate,
//                                        CallbackInfoReturnable<FluidStack> cir) {
//        FluidStack result = cir.getReturnValue();
//        if (result.isEmpty()) {
//            return; // Nothing to do
//        }
//        // Only care if water
//        if (!result.getFluid().isSame(Fluids.WATER)) {
//            return;
//        }
//        // Look up chunk water data
//        WaterChunkCapability.WaterData data = WaterDataHelper.get(world, pos);
//        if (data != null) {
//            int pH = data.getPH();
//            int sal = data.getSalinity();
//            System.out.println("[VanillaFluidTargetsMixin] Found water chunk data at " + pos
//                    + " => pH=" + pH + ", sal=" + sal);
//            CompoundTag tag = result.getOrCreateTag();
//            tag.putInt("pH", pH);
//            tag.putInt("salinity", sal);
//            result.setTag(tag);
//        }
//        cir.setReturnValue(result);
//    }
//}
