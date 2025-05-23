package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class NBTAwareFluidIngredient extends FluidIngredient {
    private Fluid myFluid;
    private CompoundTag requiredNBT;

    public NBTAwareFluidIngredient(Fluid fluid, int amount, CompoundTag requiredNBT) {
        super();
        this.myFluid = fluid;
        // Store a copy of the required NBT
        this.requiredNBT = requiredNBT != null ? requiredNBT.copy() : new CompoundTag();
    }

    public static NBTAwareFluidIngredient fromFluid(Fluid fluid, int amount, CompoundTag nbt) {
        return new NBTAwareFluidIngredient(fluid, amount, nbt);
    }

    @Override
    protected boolean testInternal(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty())
            return false;
        // Compare against our stored fluid
        if (fluidStack.getFluid() != myFluid)
            return false;
        if (fluidStack.getAmount() < getRequiredAmount())
            return false;
        // If no NBT is required, it's a match.
        if (requiredNBT == null || requiredNBT.isEmpty())
            return true;
        CompoundTag stackNBT = fluidStack.getTag();
        if (stackNBT == null)
            return false;
        for (String key : requiredNBT.getAllKeys()) {
            if (!stackNBT.contains(key) || !stackNBT.get(key).equals(requiredNBT.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<FluidStack> determineMatchingFluidStacks() {
        // Produce a singleton list with a FluidStack built from our stored fluid, required amount, and required NBT.
        FluidStack stack = new FluidStack(myFluid, getRequiredAmount());
        if (requiredNBT != null && !requiredNBT.isEmpty()) {
            stack.setTag(requiredNBT.copy());
        }
        return Collections.singletonList(stack);
    }

    @Override
    public List<FluidStack> getMatchingFluidStacks() {
        List<FluidStack> stacks = super.getMatchingFluidStacks();
        // Attach the required NBT to each matching stack so that later processing can use it.
        for (FluidStack stack : stacks) {
            if (requiredNBT != null && !requiredNBT.isEmpty()) {
                stack.setTag(requiredNBT.copy());
            }
        }
        return stacks;
    }

    // These methods are required for serialization/deserialization.
    @Override
    protected void readInternal(FriendlyByteBuf buffer) {
        boolean hasNBT = buffer.readBoolean();
        if (hasNBT) {
            this.requiredNBT = buffer.readNbt();
        } else {
            this.requiredNBT = new CompoundTag();
        }
    }

    @Override
    protected void writeInternal(FriendlyByteBuf buffer) {
        boolean hasNBT = requiredNBT != null && !requiredNBT.isEmpty();
        buffer.writeBoolean(hasNBT);
        if (hasNBT) {
            buffer.writeNbt(requiredNBT);
        }
    }

    @Override
    protected void readInternal(JsonObject json) {
        // Not used here – handled by our custom serializer.
    }

    @Override
    protected void writeInternal(JsonObject json) {
        // Not used here – handled by our custom serializer.
    }



}