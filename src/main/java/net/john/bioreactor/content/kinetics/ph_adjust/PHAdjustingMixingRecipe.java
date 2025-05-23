//package net.john.bioreactor.content.kinetics.ph_adjust;
//
//import com.simibubi.create.AllRecipeTypes;
//import com.simibubi.create.content.processing.basin.BasinRecipe;
//import com.simibubi.create.content.processing.recipe.ProcessingOutput;
//import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
//import com.simibubi.create.foundation.fluid.FluidIngredient;
//import net.minecraft.core.NonNullList;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraftforge.fluids.FluidStack;
//
//import java.lang.reflect.Constructor;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A "pH-adjusting" Basin recipe recognized by the mechanical mixer,
// * storing item & fluid inputs, fluid outputs, plus a pHDelta field.
// *
// * We override the parent's EXACT method signatures:
// * - public List<Ingredient> getIngredients()
// * - public List<FluidIngredient> getFluidIngredients()
// * - public List<ProcessingOutput> getRollableResults()
// * - public List<ProcessingOutput> getFluidResults()
// *
// * Instead of referencing protected fields, we store everything ourselves.
// * And we reflect to call the protected "ProcessingRecipeParams(ResourceLocation)" so it won't be null.
// */
//public class PHAdjustingMixingRecipe extends BasinRecipe {
//
//    private final ResourceLocation id;
//
//    // Items input: the basin checks "getIngredients()" to match items
//    private final List<Ingredient> itemIngredients = new ArrayList<>();
//    // Fluids input: the basin checks "getFluidIngredients()"
//    private final List<FluidIngredient> fluidIngredients = new ArrayList<>();
//
//    // For final item outputs, the basin calls "rollResults()" => a list<ProcessingOutput>
//    private final List<ItemStack> itemOutputs = new ArrayList<>();
//    // For final fluid outputs, the basin calls "getFluidResults()" => a list<ProcessingOutput> or specialized "ProcessingFluidOutput"
//    private final List<FluidStack> fluidOutputs = new ArrayList<>();
//
//    // Our custom field to shift water pH by some integer
//    private final int pHDelta;
//
//    /**
//     * Constructor:
//     * 1) reflection => "ProcessingRecipeParams(ResourceLocation)"
//     * 2) store your item & fluid inputs + outputs, plus pHDelta
//     */
//    public PHAdjustingMixingRecipe(
//            ResourceLocation id,
//            List<Ingredient> itemIngredients,
//            List<FluidIngredient> fluidIngredients,
//            List<ProcessingOutput> itemOutputs,
//            List<ProcessingOutput> fluidOutputs,
//            int pHDelta
//    ) {
//        super(AllRecipeTypes.MIXING, createPublicParams(id));
//        this.id = id;
//        this.itemIngredients.addAll(itemIngredients);
//        this.fluidIngredients.addAll(fluidIngredients);
//        this.itemOutputs.addAll(itemOutputs);
//        this.fluidOutputs.addAll(fluidOutputs);
//        this.pHDelta = pHDelta;
//    }
//
//    // Reflection-based workaround for the protected constructor
//    private static ProcessingRecipeBuilder.ProcessingRecipeParams createPublicParams(ResourceLocation id) {
//        try {
//            Constructor<ProcessingRecipeBuilder.ProcessingRecipeParams> c =
//                    ProcessingRecipeBuilder.ProcessingRecipeParams.class
//                            .getDeclaredConstructor(ResourceLocation.class);
//            c.setAccessible(true);
//            return c.newInstance(id);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create ProcessingRecipeParams for " + id, e);
//        }
//    }
//
//    // The basin references this as the official ID
//    @Override
//    public ResourceLocation getId() {
//        return id;
//    }
//
//    // -------------------------------------------------
//    // EXACT method signatures from the parent:
//
//    // The mechanical mixer calls this to see item inputs
//    @Override
//    public NonNullList<Ingredient> getIngredients() {
//        return (NonNullList<Ingredient>) itemIngredients;
//    }
//
//    // The mechanical mixer calls this for fluid inputs
//    @Override
//    public NonNullList<FluidIngredient> getFluidIngredients() {
//        return (NonNullList<FluidIngredient>) fluidIngredients;
//    }
//
//    // If you produce item results, the basin calls this
//    @Override
//    public List<ItemStack> rollResults() {
//        return itemOutputs;
//    }
//
//    // If you produce fluid results, the basin calls this
//    @Override
//    public NonNullList<FluidStack> getFluidResults() {
//        return fluidOutputs;
//    }
//
//    // -------------------------------------------------
//    // Our custom property to shift water pH
//    public int getPHDelta() {
//        return pHDelta;
//    }
//}
