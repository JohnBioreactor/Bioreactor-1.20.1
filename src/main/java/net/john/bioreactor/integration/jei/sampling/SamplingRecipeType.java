package net.john.bioreactor.integration.jei.sampling;

import mezz.jei.api.recipe.RecipeType;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.content.kinetics.Sampling.SamplingRecipe;

public class SamplingRecipeType {
    public static final RecipeType<SamplingRecipe> SAMPLING =
            RecipeType.create(
                    Bioreactor.MOD_ID,
                    BioreactorRecipes.SAMPLING_TYPE.getId().getPath(),
                    SamplingRecipe.class
            );
}
