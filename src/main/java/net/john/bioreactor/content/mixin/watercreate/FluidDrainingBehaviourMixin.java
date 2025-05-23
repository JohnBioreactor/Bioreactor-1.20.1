//// FILE: FluidDrainingBehaviourMixin.java
//package net.john.bioreactor.content.mixin.watercreate;
//
//import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
//import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
//import net.john.bioreactor.foundation.waterphsalinity.create.IFluidDrainingExtensions;
//import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.LiquidBlock;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//import net.minecraft.world.level.material.Fluids;
//import net.minecraftforge.fluids.FluidStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
///**
// * Mixin hooking into Create's FluidDrainingBehaviour.
// * - We store the SmartBlockEntity reference from its constructor in a new field.
// * - We implement IFluidDrainingExtensions to hold the last drained water block's
// *   custom NBT (pH/salinity).
// */
//@Mixin(value = FluidDrainingBehaviour.class, remap = false)
//public abstract class FluidDrainingBehaviourMixin implements IFluidDrainingExtensions {
//
//    /**
//     * We'll store the associated TE in this newly added field.
//     * "bioreactor$theBlockEntity" is @Unique to avoid naming conflicts.
//     */
//    @Unique
//    private SmartBlockEntity bioreactor$theBlockEntity;
//
//    /**
//     * The last water fluid we drained, with pH/salinity in tag.
//     */
//    @Unique
//    private FluidStack bioreactor$lastDrainedFluidStack = FluidStack.EMPTY;
//
//    /**
//     * During the FluidDrainingBehaviour(SmartBlockEntity be) constructor,
//     * we capture the 'be' reference.
//     */
//    @Inject(method = "<init>(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;)V",
//            at = @At("TAIL"))
//    private void bioreactor$onConstruct(SmartBlockEntity be, CallbackInfo ci) {
//        this.bioreactor$theBlockEntity = be;
//    }
//
//    /**
//     * Implementation of IFluidDrainingExtensions.
//     */
//    @Override
//    @Unique
//    public FluidStack bioreactor_getLastDrainedFluidStack() {
//        return this.bioreactor$lastDrainedFluidStack;
//    }
//
//    /**
//     * TAIL injection for pullNext(...). If it successfully drained a block,
//     * we check if 'root' was water. If so, we read pH/salinity from chunk data
//     * and store a new FluidStack in bioreactor$lastDrainedFluidStack.
//     */
//    @Inject(method = "pullNext(Lnet/minecraft/core/BlockPos;Z)Z",
//            at = @At("TAIL"), cancellable = false)
//    private void bioreactor$afterPullNext(BlockPos root, boolean simulate,
//                                          CallbackInfoReturnable<Boolean> cir) {
//
//        // If nothing was drained, clear
//        if (!cir.getReturnValueZ()) {
//            this.bioreactor$lastDrainedFluidStack = FluidStack.EMPTY;
//            return;
//        }
//
//        // Check if 'root' was water
//        if (!bioreactor$isSourceWater(root)) {
//            this.bioreactor$lastDrainedFluidStack = FluidStack.EMPTY;
//            return;
//        }
//
//        // Read chunk data => pH, salinity
//        int pH = 7;
//        int sal = 0;
//        Level lvl = bioreactor$theBlockEntity.getLevel();
//        var data = WaterDataHelper.get(lvl, root);
//        if (data != null) {
//            pH = data.getPH();
//            sal = data.getSalinity();
//        }
//
//        CompoundTag tag = new CompoundTag();
//        tag.putInt("pH", pH);
//        tag.putInt("salinity", sal);
//
//        // Store final fluid stack
//        this.bioreactor$lastDrainedFluidStack = new FluidStack(Fluids.WATER, 1000, tag);
//
//        System.out.println("[FluidDrainingBehaviourMixin] Drained water => pH=" + pH + ", sal=" + sal);
//    }
//
//    /**
//     * Check if 'pos' was a source water block or waterlogged.
//     */
//    @Unique
//    private boolean bioreactor$isSourceWater(BlockPos pos) {
//        Level lvl = this.bioreactor$theBlockEntity.getLevel();
//        if (lvl == null || !lvl.isLoaded(pos))
//            return false;
//
//        BlockState bs = lvl.getBlockState(pos);
//
//        // Source LiquidBlock
//        if (bs.getBlock() instanceof LiquidBlock) {
//            if (bs.getFluidState().isSource() && bs.getFluidState().getType() == Fluids.WATER)
//                return true;
//        }
//
//        // Waterlogged
//        if (bs.hasProperty(BlockStateProperties.WATERLOGGED)
//                && bs.getValue(BlockStateProperties.WATERLOGGED)) {
//            return true;
//        }
//        return false;
//    }
//}
