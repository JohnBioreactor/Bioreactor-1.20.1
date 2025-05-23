package net.john.bioreactor.content.mixin.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe.SplashingWrapper;
import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AllFanProcessingTypes.SplashingType.class, remap = false)
public class SplashingTypeMixin {
    @Inject(method = "process(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private void onProcess(ItemStack input, Level level, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> defaultOutputs = cir.getReturnValue();
        if (defaultOutputs == null || defaultOutputs.isEmpty())
            return;

        RecipeManager rm = level.getRecipeManager();
        List<SplashingRecipe> recipes = rm.getAllRecipesFor(AllRecipeTypes.SPLASHING.getType());

        for (SplashingRecipe recipe : recipes) {
            SplashingWrapper wrapper = new SplashingWrapper();
            wrapper.setItem(0, input.copy());
            if (!recipe.matches(wrapper, level))
                continue;

            if (recipe instanceof ICustomNBTRecipe custom) {
                CompoundTag required = custom.bioreactor_getInputNBT();
                if (required != null) {
                    CompoundTag actual = input.getTag();
                    if (actual == null || !actual.equals(required))
                        continue;
                }
                CompoundTag outTag = custom.bioreactor_getResultNBT();
                if (outTag != null) {
                    for (ItemStack out : defaultOutputs) {
                        out.setTag(outTag.copy());
                    }
                }
            }

            cir.setReturnValue(defaultOutputs);
            return;
        }
        // sinon on conserve la recette vanilla
    }
}
