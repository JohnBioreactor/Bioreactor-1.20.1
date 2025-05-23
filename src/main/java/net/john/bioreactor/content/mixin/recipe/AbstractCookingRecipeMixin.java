package net.john.bioreactor.content.mixin.recipe;

import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Implémente ICustomNBTRecipe et injecte le NBT sur le résultat
 * renvoyé par getResultItem(), utilisé par JEI.
 */
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin implements ICustomNBTRecipe {

    @Shadow @Final protected ItemStack result;

    @Unique private CompoundTag bioreactor_inputNBT;
    @Unique private CompoundTag bioreactor_resultNBT;

    // --- ICustomNBTRecipe impl. ---
    @Override public void bioreactor_setInputNBT(CompoundTag nbt)  { this.bioreactor_inputNBT = nbt; }
    @Override public CompoundTag bioreactor_getInputNBT()          { return this.bioreactor_inputNBT; }
    @Override public void bioreactor_setResultNBT(CompoundTag nbt) { this.bioreactor_resultNBT = nbt; }
    @Override public CompoundTag bioreactor_getResultNBT()         { return this.bioreactor_resultNBT; }


    /**
     * Quand JEI (ou quiconque) demande l’ItemStack de sortie,
     * on y greffe ici le NBT qu’on a stocké.
     */
    @Inject(method = "getResultItem",
            at = @At("RETURN"),
            cancellable = true)

    private void injectGetResultItem(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (bioreactor_resultNBT != null) {
            stack.setTag(bioreactor_resultNBT.copy());
            cir.setReturnValue(stack);
        }
    }

    /**
     * Quand JEI (ou Minecraft) veut connaître la liste des ingrédients,
     * on remplace le premier Ingredient par un Ingredient contenant
     * un ItemStack déjà muni de notre inputNBT.
     */
    @Inject(method = "getIngredients",
            at = @At("RETURN"),
            cancellable = true)

    private void injectGetIngredients(CallbackInfoReturnable<List<Ingredient>> cir) {
        List<Ingredient> ingredients = cir.getReturnValue();
        if (bioreactor_inputNBT != null && !ingredients.isEmpty()) {
            Ingredient orig = ingredients.get(0);
            ItemStack[] items = orig.getItems();
            if (items.length > 0) {
                ItemStack taggedInput = items[0].copy();
                taggedInput.setTag(bioreactor_inputNBT.copy());
                ingredients.set(0, Ingredient.of(taggedInput));
            }
        }
        cir.setReturnValue(ingredients);
    }
}