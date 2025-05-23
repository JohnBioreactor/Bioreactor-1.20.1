package net.john.bioreactor.content.mixin.watervanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;

import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterChunkCapability.WaterData;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;

import net.minecraft.world.level.Level;

@Mixin(LiquidBlock.class)
public abstract class BucketItemPickupMixin {

    @Inject(
            method = "pickupBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void afterPickupBlock(LevelAccessor levelAccessor, BlockPos pos, BlockState state,
                                  CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();

        System.out.println("[BucketItemPickupMixin] pickupBlock at pos=" + pos
                + ", returning stack=" + result);

        if (!result.isEmpty() && result.getItem() == Items.WATER_BUCKET) {
            System.out.println("[BucketItemPickupMixin] It's a WATER_BUCKET!");
            if (levelAccessor instanceof Level level) {
                WaterData data = WaterDataHelper.get(level, pos);
                if (data != null) {
                    System.out.println("[BucketItemPickupMixin] Found data => pH="
                            + data.getPH() + ", sal=" + data.getSalinity());
                    CompoundTag tag = result.getOrCreateTag();
                    tag.putInt("pH", data.getPH());
                    tag.putInt("salinity", data.getSalinity());
                } else {
                    System.out.println("[BucketItemPickupMixin] Data was null for pos=" + pos);
                }
            }
        }
    }
}
