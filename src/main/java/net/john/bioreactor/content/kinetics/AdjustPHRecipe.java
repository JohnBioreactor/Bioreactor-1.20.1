//package net.john.bioreactor.content.kinetics;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import net.john.bioreactor.content.item.BioreactorItems;
//import net.minecraft.core.NonNullList;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.crafting.*;
//import net.minecraft.world.level.Level;
//
//public class AdjustPHRecipe extends ShapelessRecipe {
//    public static final String NBT_KEY_PH = "pH";
//
//    public AdjustPHRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
//        super(id, group, CraftingBookCategory.MISC, result, ingredients);
//    }
//
//    // Our recipe matches if there is exactly one water bucket and at least one powder anywhere.
//    @Override
//    public boolean matches(CraftingContainer container, Level level) {
//        boolean foundWater = false;
//        int powderTotal = 0;
//        for (int i = 0; i < container.getContainerSize(); i++) {
//            ItemStack stack = container.getItem(i);
//            if (!stack.isEmpty()) {
//                if (stack.is(Items.WATER_BUCKET)) {
//                    // Only one water bucket allowed
//                    if (foundWater) return false;
//                    foundWater = true;
//                } else if (stack.is(BioreactorItems.POWDER_HCL.get())
//                        || stack.is(BioreactorItems.POWDER_H2SO4.get())
//                        || stack.is(BioreactorItems.POWDER_NAOH.get())) {
//                    powderTotal += stack.getCount();
//                } else {
//                    // Any other item disqualifies the recipe.
//                    return false;
//                }
//            }
//        }
//        return foundWater && powderTotal > 0;
//    }
//
//    // A helper container for our computed consumption values.
//    private static class ConsumptionResult {
//        final int finalPh;
//        final int consumedHCL;
//        final int consumedH2SO4;
//        final int consumedNaOH;
//        final boolean isSingleType; // true if only one powder type is used
//
//        ConsumptionResult(int finalPh, int consumedHCL, int consumedH2SO4, int consumedNaOH, boolean isSingleType) {
//            this.finalPh = finalPh;
//            this.consumedHCL = consumedHCL;
//            this.consumedH2SO4 = consumedH2SO4;
//            this.consumedNaOH = consumedNaOH;
//            this.isSingleType = isSingleType;
//        }
//    }
//
//    /**
//     * Scans the entire crafting grid:
//     * - Finds the water bucket’s current pH.
//     * - Sums up all powder counts.
//     * - Determines whether only one powder type is present.
//     * - Computes the final pH and how many powder items to consume.
//     *
//     * In the "single–powder" case we only remove as many items as needed,
//     * in the "multiple–powder" case we remove every powder.
//     */
//    private static ConsumptionResult computeConsumption(CraftingContainer container) {
//        int initialPh = 7;
//        // Locate water bucket and read its pH.
//        for (int i = 0; i < container.getContainerSize(); i++) {
//            ItemStack stack = container.getItem(i);
//            if (!stack.isEmpty() && stack.is(Items.WATER_BUCKET)) {
//                if (stack.hasTag() && stack.getTag().contains(NBT_KEY_PH)) {
//                    initialPh = stack.getTag().getInt(NBT_KEY_PH);
//                }
//                break;
//            }
//        }
//        int hclCount = 0;
//        int h2so4Count = 0;
//        int naohCount = 0;
//        for (int i = 0; i < container.getContainerSize(); i++) {
//            ItemStack stack = container.getItem(i);
//            if (!stack.isEmpty()) {
//                if (stack.is(BioreactorItems.POWDER_HCL.get())) {
//                    hclCount += stack.getCount();
//                } else if (stack.is(BioreactorItems.POWDER_H2SO4.get())) {
//                    h2so4Count += stack.getCount();
//                } else if (stack.is(BioreactorItems.POWDER_NAOH.get())) {
//                    naohCount += stack.getCount();
//                }
//            }
//        }
//        int typesPresent = 0;
//        if (hclCount > 0) typesPresent++;
//        if (h2so4Count > 0) typesPresent++;
//        if (naohCount > 0) typesPresent++;
//        boolean isSingleType = (typesPresent == 1);
//        int finalPh = initialPh;
//        int consumeHCL = 0;
//        int consumeH2SO4 = 0;
//        int consumeNaOH = 0;
//
//        if (isSingleType) {
//            // Only one type: consume just as many as needed to hit a pH boundary.
//            if (hclCount > 0) {
//                int phRange = initialPh - 1; // cannot go below 1
//                int needed = (phRange + 2 - 1) / 2;  // each HCl lowers pH by 2 (ceiling division)
//                consumeHCL = Math.min(hclCount, needed);
//                finalPh = initialPh - consumeHCL * 2;
//            } else if (h2so4Count > 0) {
//                int phRange = initialPh - 1;
//                int needed = (phRange + 4 - 1) / 4;  // each H2SO4 lowers pH by 4
//                consumeH2SO4 = Math.min(h2so4Count, needed);
//                finalPh = initialPh - consumeH2SO4 * 4;
//            } else if (naohCount > 0) {
//                int phRange = 14 - initialPh; // cannot go above 14
//                int needed = (phRange + 2 - 1) / 2;  // each NaOH raises pH by 2
//                consumeNaOH = Math.min(naohCount, needed);
//                finalPh = initialPh + consumeNaOH * 2;
//            }
//            finalPh = Math.max(1, Math.min(14, finalPh));
//        } else {
//            // Multiple powder types: sum the full effect and consume all powder items.
//            int totalChange = (-2 * hclCount) + (-4 * h2so4Count) + (2 * naohCount);
//            finalPh = initialPh + totalChange;
//            finalPh = Math.max(1, Math.min(14, finalPh));
//            consumeHCL = hclCount;
//            consumeH2SO4 = h2so4Count;
//            consumeNaOH = naohCount;
//        }
//        return new ConsumptionResult(finalPh, consumeHCL, consumeH2SO4, consumeNaOH, isSingleType);
//    }
//
//    // Assemble a new water bucket with the adjusted pH.
//    @Override
//    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
//        ConsumptionResult consumption = computeConsumption(container);
//        ItemStack bucketSource = ItemStack.EMPTY;
//        for (int i = 0; i < container.getContainerSize(); i++) {
//            ItemStack stack = container.getItem(i);
//            if (!stack.isEmpty() && stack.is(Items.WATER_BUCKET)) {
//                bucketSource = stack;
//                break;
//            }
//        }
//        ItemStack output = new ItemStack(Items.WATER_BUCKET);
//        if (!bucketSource.isEmpty() && bucketSource.hasTag()) {
//            output.setTag(bucketSource.getTag().copy());
//        }
//        output.getOrCreateTag().putInt(NBT_KEY_PH, consumption.finalPh);
//        return output;
//    }
//
//    /**
//     * Returns the list of items remaining in each slot after crafting.
//     *
//     * In the multiple–powder case (more than one powder type), every slot
//     * that contained a water bucket or powder is cleared.
//     *
//     * In the single–powder case, only as many powder items as needed are removed,
//     * leaving the rest in the slot.
//     *
//     * (Your CustomCraftingResultSlot's onTake() will call this method and then update the
//     * crafting container manually.)
//     */
//    @Override
//    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
//        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
//        ConsumptionResult consumption = computeConsumption(container);
//        if (!consumption.isSingleType) {
//            // Multiple powder types: clear any water bucket and any powder item.
//            for (int i = 0; i < container.getContainerSize(); i++) {
//                ItemStack stack = container.getItem(i);
//                if (!stack.isEmpty() && (stack.is(Items.WATER_BUCKET)
//                        || stack.is(BioreactorItems.POWDER_HCL.get())
//                        || stack.is(BioreactorItems.POWDER_H2SO4.get())
//                        || stack.is(BioreactorItems.POWDER_NAOH.get()))) {
//                    remaining.set(i, ItemStack.EMPTY);
//                } else {
//                    remaining.set(i, stack.copy());
//                }
//            }
//        } else {
//            // Single powder type: subtract only as many powder items as needed.
//            int needHCL = consumption.consumedHCL;
//            int needH2SO4 = consumption.consumedH2SO4;
//            int needNaOH = consumption.consumedNaOH;
//            for (int i = 0; i < container.getContainerSize(); i++) {
//                ItemStack stack = container.getItem(i);
//                if (!stack.isEmpty()) {
//                    if (stack.is(Items.WATER_BUCKET)) {
//                        remaining.set(i, ItemStack.EMPTY);
//                    } else if (stack.is(BioreactorItems.POWDER_HCL.get())) {
//                        int count = stack.getCount();
//                        if (needHCL > 0) {
//                            int toConsume = Math.min(count, needHCL);
//                            needHCL -= toConsume;
//                            int left = count - toConsume;
//                            if (left > 0) {
//                                ItemStack leftover = stack.copy();
//                                leftover.setCount(left);
//                                remaining.set(i, leftover);
//                            } else {
//                                remaining.set(i, ItemStack.EMPTY);
//                            }
//                        } else {
//                            remaining.set(i, stack.copy());
//                        }
//                    } else if (stack.is(BioreactorItems.POWDER_H2SO4.get())) {
//                        int count = stack.getCount();
//                        if (needH2SO4 > 0) {
//                            int toConsume = Math.min(count, needH2SO4);
//                            needH2SO4 -= toConsume;
//                            int left = count - toConsume;
//                            if (left > 0) {
//                                ItemStack leftover = stack.copy();
//                                leftover.setCount(left);
//                                remaining.set(i, leftover);
//                            } else {
//                                remaining.set(i, ItemStack.EMPTY);
//                            }
//                        } else {
//                            remaining.set(i, stack.copy());
//                        }
//                    } else if (stack.is(BioreactorItems.POWDER_NAOH.get())) {
//                        int count = stack.getCount();
//                        if (needNaOH > 0) {
//                            int toConsume = Math.min(count, needNaOH);
//                            needNaOH -= toConsume;
//                            int left = count - toConsume;
//                            if (left > 0) {
//                                ItemStack leftover = stack.copy();
//                                leftover.setCount(left);
//                                remaining.set(i, leftover);
//                            } else {
//                                remaining.set(i, ItemStack.EMPTY);
//                            }
//                        } else {
//                            remaining.set(i, stack.copy());
//                        }
//                    } else {
//                        remaining.set(i, stack.copy());
//                    }
//                }
//            }
//        }
//        return remaining;
//    }
//
//    @Override
//    public boolean isSpecial() {
//        return true;
//    }
//
//    // ---------------- Serializer ----------------
//    public static class Serializer implements RecipeSerializer<AdjustPHRecipe> {
//        @Override
//        public AdjustPHRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
//            String group = GsonHelper.getAsString(json, "group", "");
//            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
//            NonNullList<Ingredient> ingredients = NonNullList.create();
//            for (int i = 0; i < ingredientsArray.size(); i++) {
//                ingredients.add(Ingredient.fromJson(ingredientsArray.get(i)));
//            }
//            // The "result" object in JSON is a placeholder.
//            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
//            return new AdjustPHRecipe(recipeId, group, result, ingredients);
//        }
//        @Override
//        public AdjustPHRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
//            String group = buffer.readUtf();
//            int ingredientCount = buffer.readVarInt();
//            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
//            for (int i = 0; i < ingredientCount; i++) {
//                ingredients.set(i, Ingredient.fromNetwork(buffer));
//            }
//            ItemStack result = buffer.readItem();
//            return new AdjustPHRecipe(recipeId, group, result, ingredients);
//        }
//        @Override
//        public void toNetwork(FriendlyByteBuf buffer, AdjustPHRecipe recipe) {
//            buffer.writeUtf(recipe.getGroup());
//            buffer.writeVarInt(recipe.getIngredients().size());
//            for (Ingredient ingredient : recipe.getIngredients()) {
//                ingredient.toNetwork(buffer);
//            }
//            buffer.writeItem(recipe.getResultItem(null));
//        }
//    }
//}
