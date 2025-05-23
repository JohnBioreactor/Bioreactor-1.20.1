package net.john.bioreactor.integration.jei.metabolism;

import net.minecraftforge.fluids.FluidStack;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import java.util.ArrayList;
import java.util.List;

/** Ce helper permet d’extraire, pour chaque FluidIngredient,
 * le premier FluidStack correspondant (les quantités y sont déjà définies lors du parsing du JSON).
 */

public class RecipeHelper {
    public static List<FluidStack> getFluidStacks(List<FluidIngredient> fluidIngredients) {
        List<FluidStack> fluidStacks = new ArrayList<>();
        if (fluidIngredients != null) {
            for (FluidIngredient ingredient : fluidIngredients) {
                if (!ingredient.getMatchingFluidStacks().isEmpty()) {
                    fluidStacks.add(ingredient.getMatchingFluidStacks().get(0));
                }
            }
        }
        return fluidStacks;
    }
}
