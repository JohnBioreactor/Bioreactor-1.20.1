package net.john.bioreactor.content.mixin.watercreate;

import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.content.fluids.OpenEndedPipe;

/**
 * Mixin hooking into the mechanical pump's open-ended pipe logic.
 *
 * removeFluidFromSpace(...) -> we read chunk data from the block
 * provideFluidToSpace(...)  -> we set chunk data to the block
 */
@Mixin(value = OpenEndedPipe.class, remap = false)
public abstract class OpenEndedPipeMixin {

    /**
     * If the pipe extracts a water block, unify the chunk-based pH/salinity into
     * the returned FluidStack.
     */
    @Inject(method = "removeFluidFromSpace",
            at = @At("RETURN"),
            cancellable = true)
    private void bioreactor$afterRemoveFluidFromSpace(boolean simulate,
                                                      CallbackInfoReturnable<FluidStack> cir) {

        FluidStack result = cir.getReturnValue();
        if (result.isEmpty() || result.getFluid() != Fluids.WATER) {
            return; // not water => do nothing
        }

        // Access the private fields via your existing Accessor Mixin
        OpenEndedPipeAccessorMixin self = (OpenEndedPipeAccessorMixin) (Object) this;
        Level world = self.getWorld();
        BlockPos pos = self.getOutputPos();

        if (world == null || !world.isLoaded(pos)) {
            return;
        }

        // If the block was just turned to air, we can still read chunk data from that position
        // to get "the water that was here."
        int pH = 7;
        int sal = 0;
        var data = WaterDataHelper.get(world, pos);
        if (data != null) {
            pH = data.getPH();
            sal = data.getSalinity();
        }

        // Overwrite the returned fluid's NBT
        CompoundTag tag = result.getOrCreateTag();
        tag.putInt("pH", pH);
        tag.putInt("salinity", sal);
        result.setTag(tag);

        System.out.println("[OpenEndedPipeMixin] => Removed water block pH=" + pH + ", sal=" + sal);
        cir.setReturnValue(result);
    }

    /**
     * If the pipe places water, unify the fluid's pH/salinity into chunk data.
     */
    @Inject(method = "provideFluidToSpace",
            at = @At("HEAD"))
    private void bioreactor$onProvideFluidToSpace(FluidStack fluid, boolean simulate,
                                                  CallbackInfoReturnable<Boolean> cir) {

        if (fluid.isEmpty() || fluid.getFluid() != Fluids.WATER) {
            return;
        }
        // if no pH/salinity, skip
        if (!fluid.hasTag() || !fluid.getTag().contains("pH")) {
            return;
        }

        // Access the private fields via your Accessor
        OpenEndedPipeAccessorMixin self = (OpenEndedPipeAccessorMixin) (Object) this;
        Level world = self.getWorld();
        BlockPos pos = self.getOutputPos();

        if (world == null || simulate || !world.isLoaded(pos)) {
            return;
        }

        // read from fluid
        int pH = fluid.getTag().getInt("pH");
        int sal = fluid.getTag().getInt("salinity");

        // Write to chunk data
        WaterDataHelper.set(world, pos, pH, sal);

        System.out.println("[OpenEndedPipeMixin] => Placing water => pH=" + pH + ", sal=" + sal + " at " + pos);
    }

    /**
     * (Optional) If you want a helper to check if a block is water source.
     * But the above code doesn't strictly need it, so it's omitted.
     */
    @Unique
    private boolean bioreactor$isBlockSourceWater(Level world, BlockPos pos) {
        if (!world.isLoaded(pos))
            return false;
        BlockState bs = world.getBlockState(pos);
        if (bs.getBlock() instanceof LiquidBlock
                && bs.getFluidState().isSource()
                && bs.getFluidState().getType() == Fluids.WATER) {
            return true;
        }
        if (bs.hasProperty(BlockStateProperties.WATERLOGGED)
                && bs.getValue(BlockStateProperties.WATERLOGGED)) {
            return true;
        }
        return false;
    }
}
