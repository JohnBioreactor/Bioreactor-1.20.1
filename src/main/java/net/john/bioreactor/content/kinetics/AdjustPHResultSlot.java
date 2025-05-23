//package net.john.bioreactor.content.kinetics;
//
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.Container;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.item.ItemStack;
//
//public class AdjustPHResultSlot extends Slot {
//    private final Container craftingInventory;
//    private final AdjustPHRecipe recipe; // Your custom recipe type
//
//    public AdjustPHResultSlot(Player player, Container craftingInventory, int index, int x, int y, AdjustPHRecipe recipe) {
//        super(craftingInventory, index, x, y);
//        this.craftingInventory = craftingInventory;
//        this.recipe = recipe;
//    }
//
//    @Override
//    public void onTake(Player player, ItemStack stack) {
//        // Get the full dynamic remaining items based on our custom logic.
//        NonNullList<ItemStack> remaining = recipe.getRemainingItems((CraftingContainer)craftingInventory);
//        // Manually update each slot in the crafting inventory.
//        for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
//            craftingInventory.setItem(i, remaining.get(i));
//        }
//        super.onTake(player, stack);
//    }
//}
