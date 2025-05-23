package net.john.bioreactor.content.mixin.recipe;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(
        value = AllFanProcessingTypes.BlastingType.class,
        remap = false)

public class BlastingTypeMixin {
    @Inject(method = "process(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)

    private void onProcess(ItemStack input, Level level, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> defaultOutputs = cir.getReturnValue();
        if (defaultOutputs == null || defaultOutputs.isEmpty()) return;

        RecipeManager rm = level.getRecipeManager();
        // → grab all vanilla BLASTING (blast‐furnace) recipes
        List<BlastingRecipe> recipes = rm.getAllRecipesFor(RecipeType.BLASTING);  // RecipeType.BLASTING :contentReference[oaicite:1]{index=1}

        // wrap the input for matching
        Container inv = new SimpleContainer(input.copy());

        for (BlastingRecipe recipe : recipes) {
            if (!recipe.matches(inv, level)) continue;

            if (recipe instanceof ICustomNBTRecipe custom) {
                // enforce your strict input_nbt
                CompoundTag required = custom.bioreactor_getInputNBT();
                if (required != null) {
                    CompoundTag actual = input.getTag();
                    if (actual == null || !actual.equals(required))
                        continue;
                }
                // apply result_nbt to every output
                CompoundTag outTag = custom.bioreactor_getResultNBT();
                if (outTag != null) {
                    for (ItemStack outStack : defaultOutputs) {
                        outStack.setTag(outTag.copy());
                    }
                }
            }

            // custom recipe matched → return modified outputs
            cir.setReturnValue(defaultOutputs);
            return;
        }
        // no custom match → leave vanilla outputs untouched
    }
}
