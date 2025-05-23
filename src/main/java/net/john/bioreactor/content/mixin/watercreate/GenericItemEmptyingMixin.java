package net.john.bioreactor.content.mixin.watercreate;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import net.createmod.catnip.data.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensures that if the user empties a custom pH water bucket into a creative tank,
 * the resulting FluidStack has the same pH. Otherwise, the tag was empty => fallback pH=7.
 */
@Mixin(
        value = GenericItemEmptying.class,
        remap = false)

public abstract class GenericItemEmptyingMixin {

    @Inject(method = "emptyItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Z)Lnet/createmod/catnip/data/Pair;",
            at = @At(value = "RETURN"),
            cancellable = true)

    private static void bioreactor$afterEmptyItem(Level world, ItemStack stack, boolean simulate,
                                                  CallbackInfoReturnable<Pair<FluidStack, ItemStack>> cir) {

        // The returned Pair<FluidStack, ItemStack> => fluid is the fluid that was drained
        Pair<FluidStack, ItemStack> result = cir.getReturnValue();
        if (result == null) return;

        FluidStack drainedFluid = result.getFirst();
        ItemStack container = result.getSecond();

        // If the item was a custom pH water bucket, it might have "pH" in the bucket's tag
        CompoundTag bucketTag = stack.getTag();
        if (bucketTag == null || !bucketTag.contains("pH")) {
            // no custom pH => do nothing
            return;
        }

        // If the drained fluid is water, but has empty/partial NBT, we forcibly copy pH from the bucket
        if (!drainedFluid.isEmpty() && drainedFluid.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
            CompoundTag fluidTag = drainedFluid.getOrCreateTag();
            if (!fluidTag.contains("pH")) {
                int pH = bucketTag.getInt("pH");
                int sal = bucketTag.getInt("salinity");
                fluidTag.putInt("pH", pH);
                fluidTag.putInt("salinity", sal);
                drainedFluid.setTag(fluidTag);
            }
        }

        // Overwrite final result
        cir.setReturnValue(Pair.of(drainedFluid, container));
    }
}
