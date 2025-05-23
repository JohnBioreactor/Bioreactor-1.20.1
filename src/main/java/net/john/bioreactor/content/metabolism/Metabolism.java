package net.john.bioreactor.content.metabolism;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Metabolism {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    private final List<ItemStack> requiredItems;
    private final List<FluidIngredient> requiredFluids;
    private final List<ItemStack> outputItems;
    private final List<FluidIngredient> outputFluids;
    private final MetabolismLoader.o2_Requirement oxygenRequirement;
    private final int pHAdjustment;
    private final boolean requireLight;
    private final String refDOI;

    public Metabolism(String name, List<ItemStack> requiredItems, List<FluidIngredient> requiredFluids,
                      List<ItemStack> outputItems, List<FluidIngredient> outputFluids,
                      MetabolismLoader.o2_Requirement oxygenRequirement, int pHAdjustment, boolean requireLight, String refDOI) {
        this.name = name;
        this.requiredItems = requiredItems;
        this.requiredFluids = requiredFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
        this.oxygenRequirement = oxygenRequirement;
        this.pHAdjustment = pHAdjustment;
        this.requireLight = requireLight;
        this.refDOI = refDOI;
    }

    public static Metabolism fromJson(JsonObject json) {
        String name = json.get("metabolism_ID").getAsString();
        String refDOI = json.has("ref_DOI") ? json.get("ref_DOI").getAsString() : null;

        // ------- Ingredients -------
        JsonObject ingredients = json.getAsJsonObject("ingredients");

        // 1. Items
        List<ItemStack> requiredItems = new ArrayList<>();
        JsonArray requiredItemsArray = ingredients.getAsJsonArray("item");
        if (requiredItemsArray != null) {
            for (JsonElement element : requiredItemsArray) {
                JsonObject itemJson = element.getAsJsonObject();
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemJson.get("item").getAsString()));
                int count = itemJson.get("count").getAsInt();
                requiredItems.add(new ItemStack(item, count));
            }
        }

        // 2. Fluids
        List<FluidIngredient> requiredFluids = new ArrayList<>();
        JsonArray requiredFluidsArray = ingredients.getAsJsonArray("fluid");
        if (requiredFluidsArray != null) {
            for (JsonElement element : requiredFluidsArray) {
                JsonObject fluidJson = element.getAsJsonObject();
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidJson.get("fluid").getAsString()));
                int amount = fluidJson.get("amount").getAsInt();
                requiredFluids.add(FluidIngredient.fromFluid(fluid, amount));
            }
        }

        // ------- Results -------
        JsonObject results = json.getAsJsonObject("results");

        // 1. Items
        List<ItemStack> outputItems = new ArrayList<>();
        JsonArray outputItemsArray = results.getAsJsonArray("item");
        if (outputItemsArray != null) {
            for (JsonElement element : outputItemsArray) {
                JsonObject itemJson = element.getAsJsonObject();
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemJson.get("item").getAsString()));
                int count = itemJson.get("count").getAsInt();
                outputItems.add(new ItemStack(item, count));
            }
        }

        // 2. Fluids
        List<FluidIngredient> outputFluids = new ArrayList<>();
        JsonArray outputFluidsArray = results.getAsJsonArray("fluid");
        if (outputFluidsArray != null) {
            for (JsonElement element : outputFluidsArray) {
                JsonObject fluidJson = element.getAsJsonObject();
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidJson.get("fluid").getAsString()));
                int amount = fluidJson.get("amount").getAsInt();
                outputFluids.add(FluidIngredient.fromFluid(fluid, amount));
            }
        }

        // ------- Conditions -------
        // 1. Oxygen level
        MetabolismLoader.o2_Requirement oxygenRequirement = null;
        if (json.has("oxygen_requirement")) {
            oxygenRequirement = MetabolismLoader.o2_Requirement.valueOf(json.get("oxygen_requirement").getAsString());
        }

        // 2. pH modification
        int pHAdjustment = json.get("pHAdjustment").getAsInt();

        // 3. Require light : phototrophy vs chemotrophy
        boolean requireLight = json.get("requireLight").getAsBoolean();

        return new Metabolism(name, requiredItems, requiredFluids, outputItems, outputFluids, oxygenRequirement, pHAdjustment, requireLight, refDOI);
    }

    public boolean matches(BlockEntity entity, IItemHandler itemHandler, IFluidHandler fluidHandler, String oxygenState, int lightLevel) {
        if (itemHandler == null || fluidHandler == null) return false;

        for (ItemStack required : requiredItems) {
            boolean found = false;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && ItemStack.isSameItem(stack, required) && stack.getCount() >= required.getCount()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        for (FluidIngredient requiredFluid : requiredFluids) {
            boolean found = false;
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                if (!fluidInTank.isEmpty() && requiredFluid.test(fluidInTank)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        if (oxygenRequirement != null) {
            switch (oxygenRequirement) {
                case ANAEROBIC:
                    if (!"anoxic".equals(oxygenState)) return false;
                    break;
                case MICROAEROPHILIC:
                    if (!"hypoxic".equals(oxygenState)) return false;
                    break;
                case AEROBIC:
                    if (!"oxic".equals(oxygenState)) return false;
                    break;
            }
        }

        if (requireLight && lightLevel <= 10) {
            return false;
        }

        return true;
    }

    public List<ItemStack> apply(IFluidHandler fluidHandler, FluidStack waterTank) {
        if (fluidHandler != null) {
            for (FluidIngredient fluidInput : requiredFluids) {
                FluidStack requiredStack = fluidInput.getMatchingFluidStacks().get(0);
                for (int i = 0; i < fluidHandler.getTanks(); i++) {
                    FluidStack current = fluidHandler.getFluidInTank(i);
                    if (!current.isEmpty() && current.isFluidEqual(requiredStack) && current.getAmount() >= requiredStack.getAmount()) {
                        fluidHandler.drain(requiredStack, IFluidHandler.FluidAction.EXECUTE);
                        break;
                    }
                }
            }

            for (FluidIngredient fluidOutput : outputFluids) {
                FluidStack outputStack = fluidOutput.getMatchingFluidStacks().get(0);
                for (int i = 0; i < fluidHandler.getTanks(); i++) {
                    FluidStack current = fluidHandler.getFluidInTank(i);
                    if (current.isEmpty() || current.isFluidEqual(outputStack)) {
                        fluidHandler.fill(outputStack, IFluidHandler.FluidAction.EXECUTE);
                        break;
                    }
                }
            }
        }

        if (waterTank != null && !waterTank.isEmpty()) {
            CompoundTag tag = waterTank.getOrCreateTag();
            int currentPH = tag.getInt("pH");
            int newPH = Math.min(14, Math.max(0, currentPH + pHAdjustment));
            tag.putInt("pH", newPH);
        }

        return outputItems.stream().map(ItemStack::copy).collect(Collectors.toList());
    }

    public String getName() { return name; }
    public List<ItemStack> getRequiredItems() { return Collections.unmodifiableList(requiredItems); }
    public List<FluidIngredient> getRequiredFluids() { return Collections.unmodifiableList(requiredFluids); }
    public List<ItemStack> getOutputItems() { return Collections.unmodifiableList(outputItems); }
    public List<FluidIngredient> getOutputFluids() { return Collections.unmodifiableList(outputFluids); }
    public MetabolismLoader.o2_Requirement getOxygenRequirement() { return oxygenRequirement; }
    public int getpHAdjustment() { return pHAdjustment; }
    public boolean getRequireLight() { return requireLight; }
    public String getRefDOI() { return refDOI; }
}