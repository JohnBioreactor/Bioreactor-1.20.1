//package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.simibubi.create.content.processing.recipe.HeatCondition;
//import com.simibubi.create.foundation.fluid.FluidIngredient;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.nbt.TagParser;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.RecipeSerializer;
//import net.minecraftforge.fluids.FluidStack;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public class SmallBioreactorRecipeSerializer implements RecipeSerializer<SmallBioreactorRecipe> {
//    @Override
//    public SmallBioreactorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
//        JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
//        List<ItemStack> requiredItems = new ArrayList<>();
//        List<FluidIngredient> requiredBasinFluids = new ArrayList<>();
//        List<FluidIngredient> requiredChamberFluids = new ArrayList<>();
//
//        for (JsonElement element : ingredientsArray) {
//            JsonObject obj = element.getAsJsonObject();
//            if (obj.has("item")) {
//                String itemId = GsonHelper.getAsString(obj, "item");
//                int count = GsonHelper.getAsInt(obj, "count", 1);
//                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), count);
//                requiredItems.add(stack);
//            }
//            if (obj.has("fluid_basin")) {
//                JsonObject fluidObj = GsonHelper.getAsJsonObject(obj, "fluid_basin");
//                FluidStack fluid = parseFluidStack(fluidObj);
//                if (fluid.hasTag() && fluid.getTag() != null && !fluid.getTag().isEmpty()) {
//                    requiredBasinFluids.add(NBTAwareFluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount(), fluid.getTag()));
//                } else {
//                    requiredBasinFluids.add(FluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount()));
//                }
//            }
//            if (obj.has("fluid_anaerobic_chamber")) {
//                JsonArray fluidsArray = GsonHelper.getAsJsonArray(obj, "fluid_anaerobic_chamber");
//                for (JsonElement fluidElement : fluidsArray) {
//                    JsonObject fluidObj = fluidElement.getAsJsonObject();
//                    FluidStack fluid = parseFluidStack(fluidObj);
//                    if (fluid.hasTag() && fluid.getTag() != null && !fluid.getTag().isEmpty()) {
//                        requiredChamberFluids.add(NBTAwareFluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount(), fluid.getTag()));
//                    } else {
//                        requiredChamberFluids.add(FluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount()));
//                    }
//                }
//            }
//        }
//
//        JsonArray resultsArray = GsonHelper.getAsJsonArray(json, "results");
//        List<ItemStack> outputItems = new ArrayList<>();
//        List<FluidIngredient> outputBasinFluids = new ArrayList<>();
//        List<FluidIngredient> outputChamberFluids = new ArrayList<>();
//
//        for (JsonElement element : resultsArray) {
//            JsonObject obj = element.getAsJsonObject();
//            if (obj.has("item")) {
//                String itemId = GsonHelper.getAsString(obj, "item");
//                int count = GsonHelper.getAsInt(obj, "count", 1);
//                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), count);
//                outputItems.add(stack);
//            }
//            if (obj.has("fluid_basin")) {
//                JsonObject fluidObj = GsonHelper.getAsJsonObject(obj, "fluid_basin");
//                FluidStack fluid = parseFluidStack(fluidObj);
//                if (fluid.hasTag() && fluid.getTag() != null && !fluid.getTag().isEmpty()) {
//                    outputBasinFluids.add(NBTAwareFluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount(), fluid.getTag()));
//                } else {
//                    outputBasinFluids.add(FluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount()));
//                }
//            }
//            if (obj.has("fluid_anaerobic_chamber")) {
//                JsonArray fluidsArray = GsonHelper.getAsJsonArray(obj, "fluid_anaerobic_chamber");
//                for (JsonElement fluidElement : fluidsArray) {
//                    JsonObject fluidObj = fluidElement.getAsJsonObject();
//                    FluidStack fluid = parseFluidStack(fluidObj);
//                    if (fluid.hasTag() && fluid.getTag() != null && !fluid.getTag().isEmpty()) {
//                        outputChamberFluids.add(NBTAwareFluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount(), fluid.getTag()));
//                    } else {
//                        outputChamberFluids.add(FluidIngredient.fromFluid(fluid.getFluid(), fluid.getAmount()));
//                    }
//                }
//            }
//        }
//
//        Optional<Integer> minRPM = json.has("minimum_rpm") ? Optional.of(GsonHelper.getAsInt(json, "minimum_rpm")) : Optional.empty();
//        Optional<Integer> maxRPM = json.has("maximum_rpm") ? Optional.of(GsonHelper.getAsInt(json, "maximum_rpm")) : Optional.empty();
//        HeatCondition requiredHeat = json.has("heatRequirement")
//                ? HeatCondition.deserialize(GsonHelper.getAsString(json, "heatRequirement"))
//                : HeatCondition.NONE;
//
//        return new SmallBioreactorRecipe(recipeId, requiredItems, requiredBasinFluids, requiredChamberFluids,
//                outputItems, outputBasinFluids, outputChamberFluids, minRPM, maxRPM, requiredHeat);
//    }
//
//    private FluidStack parseFluidStack(JsonObject fluidObj) {
//        String fluidName = GsonHelper.getAsString(fluidObj, "fluid");
//        int amount = GsonHelper.getAsInt(fluidObj, "amount", 1000);
//        FluidStack stack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)), amount);
//        if (fluidObj.has("nbt")) {
//            JsonElement nbtElement = fluidObj.get("nbt");
//            CompoundTag nbt = null;
//            if (nbtElement.isJsonObject()) {
//                nbt = parseJsonToCompoundTag(nbtElement.getAsJsonObject());
//            } else if (nbtElement.isJsonPrimitive()) {
//                try {
//                    // Use TagParser.parseTag to convert SNBT to a Tag, then cast to CompoundTag.
//                    Tag tag = TagParser.parseTag(nbtElement.getAsString());
//                    if (tag instanceof CompoundTag) {
//                        nbt = (CompoundTag) tag;
//                    } else {
//                        throw new RuntimeException("Parsed tag is not a CompoundTag: " + nbtElement.getAsString());
//                    }
//                } catch (CommandSyntaxException e) {
//                    throw new RuntimeException("Error parsing NBT string: " + nbtElement.getAsString(), e);
//                }
//            }
//            if (nbt != null) {
//                stack.setTag(nbt);
//            }
//        }
//        return stack;
//    }
//
//    private CompoundTag parseJsonToCompoundTag(JsonObject json) {
//        CompoundTag tag = new CompoundTag();
//        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
//            String key = entry.getKey();
//            JsonElement value = entry.getValue();
//            if (value.isJsonPrimitive()) {
//                if (value.getAsJsonPrimitive().isNumber()) {
//                    tag.putInt(key, value.getAsInt());
//                } else if (value.getAsJsonPrimitive().isBoolean()) {
//                    tag.putBoolean(key, value.getAsBoolean());
//                } else {
//                    tag.putString(key, value.getAsString());
//                }
//            }
//        }
//        return tag;
//    }
//
//    @Override
//    public SmallBioreactorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
//        List<ItemStack> requiredItems = buffer.readList(FriendlyByteBuf::readItem);
//        List<FluidIngredient> requiredBasinFluids = buffer.readList(FluidIngredient::read);
//        List<FluidIngredient> requiredChamberFluids = buffer.readList(FluidIngredient::read);
//        List<ItemStack> outputItems = buffer.readList(FriendlyByteBuf::readItem);
//        List<FluidIngredient> outputBasinFluids = buffer.readList(FluidIngredient::read);
//        List<FluidIngredient> outputChamberFluids = buffer.readList(FluidIngredient::read);
//        Optional<Integer> minSpeed = buffer.readOptional(FriendlyByteBuf::readInt);
//        Optional<Integer> maxSpeed = buffer.readOptional(FriendlyByteBuf::readInt);
//        HeatCondition requiredHeat = HeatCondition.deserialize(buffer.readUtf());
//        return new SmallBioreactorRecipe(recipeId, requiredItems, requiredBasinFluids, requiredChamberFluids,
//                outputItems, outputBasinFluids, outputChamberFluids, minSpeed, maxSpeed, requiredHeat);
//    }
//
//    @Override
//    public void toNetwork(FriendlyByteBuf buffer, SmallBioreactorRecipe recipe) {
//        buffer.writeCollection(recipe.getRequiredItems(), FriendlyByteBuf::writeItem);
//        buffer.writeCollection(recipe.getRequiredBasinFluids(), (buf, fluidIngredient) -> fluidIngredient.write(buf));
//        buffer.writeCollection(recipe.getRequiredChamberFluids(), (buf, fluidIngredient) -> fluidIngredient.write(buf));
//        buffer.writeCollection(recipe.getOutputItems(), FriendlyByteBuf::writeItem);
//        buffer.writeCollection(recipe.getOutputBasinFluids(), (buf, fluidIngredient) -> fluidIngredient.write(buf));
//        buffer.writeCollection(recipe.getOutputChamberFluids(), (buf, fluidIngredient) -> fluidIngredient.write(buf));
//        buffer.writeOptional(recipe.getMinSpeed(), FriendlyByteBuf::writeInt);
//        buffer.writeOptional(recipe.getMaxSpeed(), FriendlyByteBuf::writeInt);
//        buffer.writeUtf(recipe.getRequiredHeat().serialize());
//    }
//}
