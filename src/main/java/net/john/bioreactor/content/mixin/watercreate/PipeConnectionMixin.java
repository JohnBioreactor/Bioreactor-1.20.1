//package net.john.bioreactor.content.mixin.watercreate;
//
//import com.simibubi.create.content.fluids.PipeConnection;
//import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
//import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
//import net.john.bioreactor.foundation.waterphsalinity.create.IFluidDrainingExtensions;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.material.Fluids;
//import net.minecraftforge.fluids.FluidStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
///**
// * Mixin hooking PipeConnection#manageFlows to unify pH/salinity from
// * FluidDrainingBehaviourMixin's last drained fluid.
// */
//@Mixin(value = PipeConnection.class, remap = false)
//public abstract class PipeConnectionMixin {
//
//    @Inject(method = "manageFlows",
//            at = @At("HEAD"), cancellable = true)
//    private void bioreactor$onManageFlows(Level world, BlockPos pos,
//                                          FluidStack internalFluid,
//                                          java.util.function.Predicate<FluidStack> extractionPredicate,
//                                          CallbackInfoReturnable<Boolean> cir) {
//
//        // Must be water
//        if (internalFluid.isEmpty() || internalFluid.getFluid() != Fluids.WATER)
//            return;
//        if (world == null || !world.isLoaded(pos))
//            return;
//
//        // If there's a DrainingBehaviour, see if it implements our interface
//        var be = world.getBlockEntity(pos);
//        if (!(be instanceof SmartBlockEntity sbe))
//            return;
//
//        FluidDrainingBehaviour draining = sbe.getBehaviour(FluidDrainingBehaviour.TYPE);
//        if (draining == null)
//            return;
//
//        // If it has the interface, read the last drained fluid
//        if (draining instanceof IFluidDrainingExtensions drainingExt) {
//            FluidStack customStack = drainingExt.bioreactor_getLastDrainedFluidStack();
//            if (customStack.isEmpty())
//                return;
//            // If the fluid differs, skip
//            if (!customStack.isFluidEqual(internalFluid))
//                return;
//
//            // Merge NBT
//            if (customStack.hasTag()) {
//                internalFluid.setTag(customStack.getTag().copy());
//                System.out.println("[PipeConnectionMixin] Merged pH/sal => " + internalFluid.getTag());
//            }
//        }
//    }
//}
