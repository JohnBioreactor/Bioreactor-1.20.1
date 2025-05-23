package net.john.bioreactor.content.mixin.watervanilla;

import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.content.kinetics.Sampling.SamplingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public class BottleItemMixin {

    @Inject(
            method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bioreactor_onUse(Level level,
                                  Player player,
                                  InteractionHand hand,
                                  CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != Items.GLASS_BOTTLE) return;

        CompoundTag tag = stack.getTag();
        boolean isSterile = tag != null && tag.getBoolean("sterile");

        // Premier ray-tracing : détecter les blocs solides uniquement
        BlockHitResult solidHit = level.clip(new ClipContext(
                player.getEyePosition(1.0F),
                player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(5.0D)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,  // Ignorer les fluides
                player
        ));

        // Si un bloc solide est touché, laisser SamplingEventHandler gérer le clic
        if (solidHit.getType() == HitResult.Type.BLOCK) {
            if (isSterile) {
                // Pour une bouteille stérile, bloquer le comportement vanilla
                cir.setReturnValue(InteractionResultHolder.fail(stack));
                cir.cancel();
            }
            return;  // Laisser SamplingEventHandler gérer les blocs solides
        }

        // Second ray-tracing : détecter les blocs d'eau
        BlockHitResult fluidHit = level.clip(new ClipContext(
                player.getEyePosition(1.0F),
                player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(5.0D)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,  // Détecter les fluides
                player
        ));

        if (fluidHit.getType() != HitResult.Type.BLOCK) {
            if (isSterile) {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
                cir.cancel();
            }
            return;
        }

        BlockPos pos = fluidHit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        // Vérifier que le bloc est de l'eau
        if (!state.is(Blocks.WATER) || state.getFluidState().isEmpty()) {
            if (isSterile) {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
                cir.cancel();
            }
            return;
        }

        // Si la bouteille n'est pas stérile, laisser le comportement vanilla pour l'eau
        if (!isSterile) {
            return;
        }

        // Pour une bouteille stérile, tenter d'appliquer une recette
        SamplingRecipe.SamplingContext sctx = new SamplingRecipe.SamplingContext(level, pos, state, stack, null);

        for (SamplingRecipe recipe : level.getRecipeManager().getAllRecipesFor(BioreactorRecipes.SAMPLING_TYPE.get())) {
            if (!recipe.matches(sctx)) continue;
            ItemStack out = recipe.assemble(sctx);
            if (out.isEmpty()) continue;

            if (!player.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
            }
            if (!player.addItem(out)) player.drop(out, false);

            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);

            cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
            cir.cancel();
            return;
        }

        // Si aucune recette ne correspond pour une bouteille stérile, échouer
        cir.setReturnValue(InteractionResultHolder.fail(stack));
        cir.cancel();
    }
}