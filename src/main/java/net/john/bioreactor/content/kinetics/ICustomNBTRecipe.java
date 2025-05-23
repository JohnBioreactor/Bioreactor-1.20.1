package net.john.bioreactor.content.kinetics;

import net.minecraft.nbt.CompoundTag;


// Utilisé pour ProcessingRecipeMixin
// stocke les flags copy_input_nbt et result_nbt.

public interface ICustomNBTRecipe {
    /** Si présent, obligatoire dans l’input pour que la recette matche */
    void bioreactor_setInputNBT(CompoundTag nbt);
    CompoundTag bioreactor_getInputNBT();

    /** Si présent, ce NBT sera **exactement** appliqué à l’output */
    void bioreactor_setResultNBT(CompoundTag nbt);
    CompoundTag bioreactor_getResultNBT();
}