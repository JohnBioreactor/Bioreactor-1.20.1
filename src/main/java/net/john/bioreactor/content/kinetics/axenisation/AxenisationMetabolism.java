package net.john.bioreactor.content.kinetics.axenisation;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlockEntity;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.john.bioreactor.content.metabolism.MetabolismLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Holds the lists of metabolism IDs considered "green" (optimal) or "yellow" (acceptable)
 * within the context of a specific AxenisationRecipe's bacteria condition.
 *
 * matches(...) → doit être appelée depuis Metabolism
 * apply(...) → doit être appelée depuis Metabolism
 */



public class AxenisationMetabolism {
    private final Metabolism metabolism;

    public AxenisationMetabolism(Metabolism metabolism) {
        this.metabolism = metabolism;
    }

    public boolean matches(BlockEntity chamber) {
        if (!(chamber instanceof AnaerobicChamberBlockEntity)) return false;
        AnaerobicChamberBlockEntity acbe = (AnaerobicChamberBlockEntity) chamber;
        BasinBlockEntity basin = acbe.getRecipeProcessor().getBasin();
        if (basin == null) return false;
        IItemHandler itemHandler = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        IFluidHandler fluidHandler = (IFluidHandler) acbe.getTankManager();
        String oxygenState = acbe.getChamberOxygenState();

        // light level au niveau du anaerobicChamber
        int lightLevel = acbe.getLevel().getMaxLocalRawBrightness(acbe.getBlockPos());
        return metabolism.matches(acbe, itemHandler, fluidHandler, oxygenState, lightLevel);
    }

    public List<ItemStack> apply(BlockEntity chamber) {
        if (!(chamber instanceof AnaerobicChamberBlockEntity)) return List.of();
        AnaerobicChamberBlockEntity acbe = (AnaerobicChamberBlockEntity) chamber;
        BasinBlockEntity basin = acbe.getRecipeProcessor().getBasin();
        FluidStack water = basin != null ? basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0) : FluidStack.EMPTY;
        return metabolism.apply(acbe.getTankManager(), water);
    }


    /** --------- GETTERS ---------
      */
    public String getName() { return metabolism.getName(); }
    public String getRefDOI() { return metabolism.getRefDOI(); }
    public List<ItemStack> getRequiredItems() { return metabolism.getRequiredItems(); }
    public List<FluidIngredient> getRequiredFluids() { return metabolism.getRequiredFluids(); }
    public List<ItemStack> getOutputItems() { return metabolism.getOutputItems(); }
    public List<FluidIngredient> getOutputFluids() { return metabolism.getOutputFluids(); }
    public MetabolismLoader.o2_Requirement getOxygenRequirement() { return metabolism.getOxygenRequirement(); }
    public int getpHAdjustment() { return metabolism.getpHAdjustment(); }
    public boolean getRequireLight() { return metabolism.getRequireLight(); }
}