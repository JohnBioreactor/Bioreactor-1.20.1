package net.john.bioreactor.content.kinetics;

import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.kinetics.Sampling.SamplingRecipe;
import net.john.bioreactor.content.kinetics.Sampling.SamplingRecipeSerializer;
import net.john.bioreactor.content.kinetics.axenisation.AxenisationRecipe;
import net.john.bioreactor.content.kinetics.axenisation.AxenisationRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BioreactorRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =DeferredRegister.create(Registries.RECIPE_SERIALIZER, Bioreactor.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =DeferredRegister.create(Registries.RECIPE_TYPE, Bioreactor.MOD_ID);

    // Recipe
    public static final RegistryObject<RecipeSerializer<SamplingRecipe>> SAMPLING_SERIALIZER =RECIPE_SERIALIZERS.register("sampling", SamplingRecipeSerializer::new);

    // → Nouveau : RecipeType enregistré via DeferredRegister !
    public static final RegistryObject<RecipeType<SamplingRecipe>> SAMPLING_TYPE =RECIPE_TYPES
            .register("sampling",() -> RecipeType.simple(new ResourceLocation(Bioreactor.MOD_ID, "sampling")));

    public static final RecipeType<AxenisationRecipe> AXENISATION_TYPE =RecipeType.simple(new ResourceLocation(Bioreactor.MOD_ID, "axenisation"));

    public static final RegistryObject<RecipeSerializer<AxenisationRecipe>> AXENISATION_SERIALIZER = RECIPE_SERIALIZERS.register("axenisation", AxenisationRecipeSerializer::new);

//    public static final RecipeType<SmallBioreactorRecipe> SMALL_BIOREACTOR_TYPE =
//            RecipeType.simple(new ResourceLocation(Bioreactor.MOD_ID, "small_bioreactor"));

//    public static final RecipeType<net.john.bioreactor.content.kinetics.ph_adjust.PHAdjustingMixingRecipe> PH_ADJUSTING_MIXING_TYPE =
//            RecipeType.simple(new ResourceLocation(Bioreactor.MOD_ID, "ph_adjusting_mixing"));






//    public static final RegistryObject<RecipeSerializer<SmallBioreactorRecipe>> SMALL_BIOREACTOR_SERIALIZER =
//            RECIPE_SERIALIZERS.register("small_bioreactor", SmallBioreactorRecipeSerializer::new);

//    public static final RegistryObject<RecipeSerializer<net.john.bioreactor.content.kinetics.ph_adjust.PHAdjustingMixingRecipe>> PH_ADJUSTING_MIXING_SERIALIZER =
//            RECIPE_SERIALIZERS.register("ph_adjusting_mixing", PHAdjustingMixingRecipeSerializer::new);


    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
