package net.john.bioreactor.content.mixin.recipe;

import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    /**
     * Preview dans la grille de crafting et dans JEI :
     * on taggue le résultat *avant* qu'il ne soit rendu.
     */
    @Inject(
            method = "getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at     = @At("RETURN"),
            cancellable = true
    )

    private void injectGetResultItem(RegistryAccess ra, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        // Tag 'oxic' sur les bouteilles vanilla ET la seringue
        if (
                result.getItem() == Items.GLASS_BOTTLE ||
                result.getItem() == BioreactorItems.SYRINGE_EMPTY.get())
        {
            result.getOrCreateTag().putBoolean("oxic", true);
            cir.setReturnValue(result);
        }
    }

    /**
     * Réel craft final : on taggue également l'ItemStack
     * donné au joueur lors du craft.
     */
    @Inject(
            method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at     = @At("RETURN"),
            cancellable = true
    )

    private void injectAssemble(CraftingContainer inv, RegistryAccess ra, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (
                result.getItem() == Items.GLASS_BOTTLE ||
                result.getItem() == BioreactorItems.SYRINGE_EMPTY.get())
        {
            result.getOrCreateTag().putBoolean("oxic", true);
            cir.setReturnValue(result);
        }
    }
}
