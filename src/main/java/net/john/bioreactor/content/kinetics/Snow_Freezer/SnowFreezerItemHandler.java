package net.john.bioreactor.content.kinetics.Snow_Freezer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class SnowFreezerItemHandler implements IItemHandler {
    private final SnowFreezerBlockEntity freezer;

    public SnowFreezerItemHandler(SnowFreezerBlockEntity freezer) {
        this.freezer = freezer;
    }

    @Override
    public int getSlots() {
        return 1;  // Limite de 1 slot pour les carottes
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return freezer.getInventoryStack();
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // Validation: accepte seulement les carottes
        if (!stack.is(Items.CARROT))
            return stack;

        ItemStack currentStack = getStackInSlot(slot);
        if (!currentStack.isEmpty() && !simulate) {
            freezer.consumeCarrot(); // Active la consommation si une carotte est présente
            return ItemStack.EMPTY;
        }

        if (!simulate) {
            freezer.setInventoryStack(ItemHandlerHelper.copyStackWithSize(stack, 1));
            freezer.notifyUpdate();
        }

        return stack.getCount() > 1 ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1) : ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = getStackInSlot(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack extracted = stack.split(amount);
        if (!simulate) {
            freezer.setInventoryStack(stack);  // MàJ de l'inventaire
            if (stack.isEmpty())
                freezer.clearInventory();
            freezer.notifyUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.is(Items.CARROT); // Validation des carottes uniquement
    }
}
