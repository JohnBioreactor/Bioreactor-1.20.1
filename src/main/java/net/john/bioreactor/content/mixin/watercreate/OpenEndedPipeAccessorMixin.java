package net.john.bioreactor.content.mixin.watercreate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.fluids.OpenEndedPipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Accessor Mixin exposing the private fields of OpenEndedPipe.
 */
@Mixin(OpenEndedPipe.class)
public interface OpenEndedPipeAccessorMixin {

    @Accessor("world")
    Level getWorld();

    @Accessor("outputPos")
    BlockPos getOutputPos();
}
