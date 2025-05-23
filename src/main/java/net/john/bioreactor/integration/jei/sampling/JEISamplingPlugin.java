package net.john.bioreactor.integration.jei.sampling;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class JEISamplingPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Bioreactor.MOD_ID, "sampling");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new SamplingCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Récupère le RecipeManager depuis le client
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            RecipeManager manager = world.getRecipeManager();
            var recipes = manager.getAllRecipesFor(BioreactorRecipes.SAMPLING_TYPE.get());
            registration.addRecipes(SamplingRecipeType.SAMPLING, recipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.GLASS_BOTTLE),SamplingRecipeType.SAMPLING);
        registration.addRecipeCatalyst(new ItemStack(BioreactorItems.SYRINGE_EMPTY.get()),SamplingRecipeType.SAMPLING);

    }
}
