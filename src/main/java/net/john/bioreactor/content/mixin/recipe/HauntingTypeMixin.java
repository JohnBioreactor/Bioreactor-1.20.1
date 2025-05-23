package net.john.bioreactor.content.mixin.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe.HauntingWrapper;
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

@Mixin(
        value = AllFanProcessingTypes.HauntingType.class,
        remap = false)

public class HauntingTypeMixin {
    @Inject(method = "process(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)

    private void onProcess(ItemStack input, Level level, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> defaultOutputs = cir.getReturnValue();

        if (defaultOutputs == null || defaultOutputs.isEmpty())
            return;

        // Recherche d’une recette de haunting correspondante
        RecipeManager rm = level.getRecipeManager();
        List<HauntingRecipe> recipes = rm.getAllRecipesFor(AllRecipeTypes.HAUNTING.getType());

        for (HauntingRecipe recipe : recipes) {
            // Vérifier correspondance de base (sans NBT) via l’ingrédient vanilla
            HauntingWrapper wrapper = new HauntingWrapper();
            wrapper.setItem(0, input.copy());
            if (!recipe.matches(wrapper, level))
                continue;

            // Si la recette attend un NBT particulier, appliquer le filtre strict
            if (recipe instanceof ICustomNBTRecipe custom) {
                CompoundTag required = custom.bioreactor_getInputNBT();

                if (required != null) {
                    CompoundTag actual = input.getTag();

                    // Le stack d’entrée doit *exactement* posséder les tags définis (ni plus, ni moins)
                    if (actual == null || !actual.equals(required)) {
                        continue; // échec si NBT absent, différent, ou tags supplémentaires
                    }
                }
                // Recette matchée : appliquer le NBT de sortie strict à tous les items résultants
                CompoundTag outTag = custom.bioreactor_getResultNBT();

                if (outTag != null) {
                    for (ItemStack out : defaultOutputs) {
                        out.setTag(outTag.copy());
                    }
                }
            }

            // Succès : on renvoie les outputs (avec NBT éventuellement modifié) et on sort
            cir.setReturnValue(defaultOutputs);
            return;
        }

        // Aucune recette custom NBT correspondante n’a matché.
        // Si l’item d’entrée avait des NBT, on annule la transformation par défaut (stricteté requise),
        // sinon on laisse la transformation vanilla se produire normalement.
        if (input.hasTag() && input.getTag() != null && !input.getTag().isEmpty()) {
            cir.setReturnValue(null);
        }
    }
}
