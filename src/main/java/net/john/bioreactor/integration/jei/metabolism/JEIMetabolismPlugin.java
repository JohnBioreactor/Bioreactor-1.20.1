package net.john.bioreactor.integration.jei.metabolism;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.block.BioreactorBlocks;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.metabolism.BioreactorMetabolisms;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.john.bioreactor.integration.jei.metabolism.carbon_cycle.CarbonCycleCategory;
import net.john.bioreactor.integration.jei.metabolism.carbon_cycle.CarbonCycleRecipe;
import net.john.bioreactor.integration.jei.metabolism.metal_cycle.MetalCycleCategory;
import net.john.bioreactor.integration.jei.metabolism.metal_cycle.MetalCycleRecipe;
import net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle.NitrogenCycleCategory;
import net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle.NitrogenCycleRecipe;
import net.john.bioreactor.integration.jei.metabolism.sulfur_cycle.SulfurCycleCategory;
import net.john.bioreactor.integration.jei.metabolism.sulfur_cycle.SulfurCycleRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIMetabolismPlugin implements IModPlugin {

        private static final ResourceLocation PLUGIN_UID = new ResourceLocation(Bioreactor.MOD_ID, "jei_plugin");



    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    /* --------------------------------------------------------------------- */
    /*  Métabolismes : catégories (cycle du C, N, S, Fe) de métabolismes     */
    /* --------------------------------------------------------------------- */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        // Enregistrer les catégories

        registration.addRecipeCategories(new CarbonCycleCategory(guiHelper));
        registration.addRecipeCategories(new NitrogenCycleCategory(guiHelper));
        registration.addRecipeCategories(new SulfurCycleCategory(guiHelper));
        registration.addRecipeCategories(new MetalCycleCategory(guiHelper));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {


        // Enregistre l’AnaerobicChamber comme machine catalyseur pour la catégorie carbon_cycle
        registration.addRecipeCatalyst(
                new ItemStack(BioreactorBlocks.ANAEROBIC_CHAMBER),

                CarbonCycleCategory.RECIPE_TYPE,
                NitrogenCycleCategory.RECIPE_TYPE,
                SulfurCycleCategory.RECIPE_TYPE,
                MetalCycleCategory.RECIPE_TYPE
        );
    }



    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        /* --------------------------------------------------------------------- */
        /*  Métabolismes                                                         */
        /* --------------------------------------------------------------------- */


        Map<String, Metabolism> metabolismMap = BioreactorMetabolisms.getMetabolisms();

        // 1) Prépare les listes
        List<CarbonCycleRecipe> carbonCycleRecipes = new ArrayList<>();
        List<NitrogenCycleRecipe> nitrogenCycleRecipes = new ArrayList<>();
        List<SulfurCycleRecipe> sulfurCycleRecipes = new ArrayList<>();
        List<MetalCycleRecipe> metalCycleRecipes = new ArrayList<>();

        // 2) Enregistrer les métabolismes dans 1/+ cycle
        for (Metabolism metabolism : metabolismMap.values()) {
            String name = metabolism.getName();
            switch (name) {
                case    "glucose_x_o2",
                        "glucose_x_fermentation"
                        -> {carbonCycleRecipes.add(new CarbonCycleRecipe(metabolism));}

                case    "glucose_x_nitrate"
                        -> {nitrogenCycleRecipes.add(new NitrogenCycleRecipe(metabolism));}

                case    "glucose_x_sulfate"
                        -> {sulfurCycleRecipes.add(new SulfurCycleRecipe(metabolism));}

                case    "glucose_x_iron"
                        -> {metalCycleRecipes.add(new MetalCycleRecipe(metabolism));}


                default -> {
                    // ignore les autres
                }
            }
        }

        // 3) Enregistre chaque catégorie
        registration.addRecipes(CarbonCycleCategory.RECIPE_TYPE, carbonCycleRecipes);
        registration.addRecipes(NitrogenCycleCategory.RECIPE_TYPE, nitrogenCycleRecipes);
        registration.addRecipes(SulfurCycleCategory.RECIPE_TYPE, sulfurCycleRecipes);
        registration.addRecipes(MetalCycleCategory.RECIPE_TYPE, metalCycleRecipes);

        // 4) Masque les icônes d’onglets
        registration.getIngredientManager().removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,List.of(
                        new ItemStack(BioreactorItems.CARBON_CYCLE_ICON),
                        new ItemStack(BioreactorItems.NITROGEN_CYCLE_ICON),
                        new ItemStack(BioreactorItems.SULFUR_CYCLE_ICON),
                        new ItemStack(BioreactorItems.METAL_CYCLE_ICON)

                )
        );


    }


}
