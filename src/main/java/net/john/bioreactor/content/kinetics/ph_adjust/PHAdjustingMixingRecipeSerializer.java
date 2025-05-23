//package net.john.bioreactor.content.kinetics.ph_adjust;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.simibubi.create.content.processing.recipe.ProcessingOutput;
//import com.simibubi.create.foundation.fluid.FluidIngredient;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeSerializer;
//import net.minecraftforge.fluids.FluidStack;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A "Create style" serializer that reads item + fluid inputs & fluid results
// * as Ingredient, FluidIngredient, ProcessingOutput, etc.
// * Also reads pH_delta as an int.
// */
//public class PHAdjustingMixingRecipeSerializer implements RecipeSerializer<PHAdjustingMixingRecipe> {
//
//    @Override
//    public PHAdjustingMixingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
//        // item & fluid inputs
//        JsonArray ingrArray = GsonHelper.getAsJsonArray(json, "ingredients");
//        List<Ingredient> itemInputs = new ArrayList<>();
//        List<FluidIngredient> fluidInputs = new ArrayList<>();
//        for (JsonElement e : ingrArray) {
//            JsonObject obj = e.getAsJsonObject();
//            if (obj.has("item")) {
//                String itemId = GsonHelper.getAsString(obj, "item");
//                // ignoring "count" for the matching logic (Create style)
//                Ingredient ing = Ingredient.of(
//                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId))
//                );
//                itemInputs.add(ing);
//            }
//            if (obj.has("fluid") || obj.has("fluidTag")) {
//                fluidInputs.add(parseFluidIngredient(obj));
//            }
//        }
//
//        // fluid results
//        // you could parse item results if you want => "rollResults()"
//        JsonArray resArray = GsonHelper.getAsJsonArray(json, "results");
//        List<ProcessingOutput> fluidOutputs = new ArrayList<>();
//        for (JsonElement e : resArray) {
//            JsonObject obj = e.getAsJsonObject();
//            if (obj.has("fluid") || obj.has("fluidTag")) {
//                // turn them into a ProcessingFluidOutput
//                FluidStack fs = parseFluidStack(obj);
//                // in many versions, we do "new ProcessingFluidOutput(fs, <chanceOrsomeValue>)"
//                ProcessingOutput processingOutput;
//                processingOutput = new ProcessingOutput(fs, 1.0f);
//            }
//        }
//
//        // parse pH_delta
//        int pHDelta = GsonHelper.getAsInt(json, "pH_delta", 0);
//
//        return new PHAdjustingMixingRecipe(
//                recipeId,
//                itemInputs,
//                fluidInputs,
//                new ArrayList<>(), // no item outputs => rollResults() empty
//                fluidOutputs,
//                pHDelta
//        );
//    }
//
//    private FluidIngredient parseFluidIngredient(JsonObject obj) {
//        String fluidKey = obj.has("fluid") ? "fluid" : "fluidTag";
//        String fluidId = GsonHelper.getAsString(obj, fluidKey);
//        int amount = GsonHelper.getAsInt(obj, "amount", 1000);
//        return FluidIngredient.fromFluid(
//                ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)),
//                amount
//        );
//    }
//
//    private FluidStack parseFluidStack(JsonObject obj) {
//        String fluidKey = obj.has("fluid") ? "fluid" : "fluidTag";
//        String fluidId = GsonHelper.getAsString(obj, fluidKey);
//        int amount = GsonHelper.getAsInt(obj, "amount", 1000);
//        return new FluidStack(
//                ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)),
//                amount
//        );
//    }
//
//    // network read/write
//
//    @Override
//    public PHAdjustingMixingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
//        // itemInputs
//        int iCount = buffer.readVarInt();
//        List<Ingredient> itemInputs = new ArrayList<>();
//        for (int i = 0; i < iCount; i++) {
//            itemInputs.add(Ingredient.fromNetwork(buffer));
//        }
//
//        // fluidInputs
//        int fCount = buffer.readVarInt();
//        List<FluidIngredient> fluidInputs = new ArrayList<>();
//        for (int i = 0; i < fCount; i++) {
//            fluidInputs.add(FluidIngredient.read(buffer));
//        }
//
//        // itemOutputs => let's skip for now
//        // fluidOutputs
//        int foCount = buffer.readVarInt();
//        List<ProcessingOutput> fluidOutputs = new ArrayList<>();
//        for (int i = 0; i < foCount; i++) {
//            FluidStack fs = FluidStack.readFromPacket(buffer);
//            ProcessingFluidOutput out = new ProcessingFluidOutput(fs, 1.0f);
//            fluidOutputs.add(out);
//        }
//
//        int pHDelta = buffer.readVarInt();
//
//        return new PHAdjustingMixingRecipe(
//                recipeId,
//                itemInputs, fluidInputs,
//                new ArrayList<>(), // no item outputs
//                fluidOutputs,
//                pHDelta
//        );
//    }
//
//    @Override
//    public void toNetwork(FriendlyByteBuf buffer, PHAdjustingMixingRecipe recipe) {
//        // itemInputs
//        buffer.writeVarInt(recipe.itemIngredients.size());
//        for (Ingredient ing : recipe.itemIngredients) {
//            ing.toNetwork(buffer);
//        }
//
//        // fluidInputs
//        buffer.writeVarInt(recipe.fluidIngredients.size());
//        for (FluidIngredient fi : recipe.fluidIngredients) {
//            fi.write(buffer);
//        }
//
//        // fluidOutputs
//        buffer.writeVarInt(recipe.fluidOutputs.size());
//        for (ProcessingOutput out : recipe.fluidOutputs) {
//            // must cast if we used ProcessingFluidOutput
//            if (out instanceof ProcessingFluidOutput pfo) {
//                pfo.getStack().writeToPacket(buffer);
//            } else {
//                // fallback or error
//                new FluidStack(null, 0).writeToPacket(buffer);
//            }
//        }
//
//        buffer.writeVarInt(recipe.getPHDelta());
//    }
//}
