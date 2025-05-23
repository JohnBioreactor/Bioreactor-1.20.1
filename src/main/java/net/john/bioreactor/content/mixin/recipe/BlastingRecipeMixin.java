package net.john.bioreactor.content.mixin.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.BlastingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;

@Mixin(BlastingRecipe.class)

public abstract class BlastingRecipeMixin implements ICustomNBTRecipe {
    @Unique private CompoundTag bioreactor_inputNBT;
    @Unique private CompoundTag bioreactor_resultNBT;

    @Override public void   bioreactor_setInputNBT(CompoundTag nbt)   { this.bioreactor_inputNBT = nbt; }
    @Override public CompoundTag bioreactor_getInputNBT()             { return this.bioreactor_inputNBT; }
    @Override public void   bioreactor_setResultNBT(CompoundTag nbt)  { this.bioreactor_resultNBT = nbt; }
    @Override public CompoundTag bioreactor_getResultNBT()            { return this.bioreactor_resultNBT; }
}
