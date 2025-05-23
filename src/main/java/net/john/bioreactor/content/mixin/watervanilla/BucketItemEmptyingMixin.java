package net.john.bioreactor.content.mixin.watervanilla;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterPHMerger;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
public abstract class BucketItemEmptyingMixin {

    @Inject(
            method = "checkExtraContent(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;)V",
            at = @At("TAIL")
    )
    private void afterCheckExtraContent(Player player, Level level, ItemStack containerStack, BlockPos pos, CallbackInfo ci) {

        if (containerStack == null || !containerStack.hasTag()) return;
        CompoundTag tag = containerStack.getTag();
        if (tag == null || !tag.contains("pH") || !tag.contains("salinity")) return;

        int pH_bucket = tag.getInt("pH");
        int sal_bucket = tag.getInt("salinity");
        System.out.println("[BucketItemEmptyingMixin] Bucket pH=" + pH_bucket + ", sal=" + sal_bucket);

        // if placed fluid is water, we unify with neighbors
        if (level.getFluidState(pos).getType() == Fluids.WATER) {
            int[] merged = WaterPHMerger.computeAveragedPHSal(
                    level, pos,
                    pH_bucket, sal_bucket,
                    true
            );
            int finalPH = merged[0];
            int finalSal = merged[1];

            WaterDataHelper.set(level, pos, finalPH, finalSal);
            System.out.println("[BucketItemEmptyingMixin] => Final placed pH=" + finalPH + ", sal=" + finalSal);
        }
    }
}
