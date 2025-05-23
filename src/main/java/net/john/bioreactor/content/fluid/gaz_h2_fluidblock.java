package net.john.bioreactor.content.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class gaz_h2_fluidblock extends LiquidBlock {

    public gaz_h2_fluidblock(
            Supplier<? extends net.minecraft.world.level.material.FlowingFluid> fluidSupplier,
            Properties properties
    ) {
        super(fluidSupplier, properties);
    }

    /** Quand le bloc est placé / fluid update */
    @Override
    public void onPlace(BlockState newState, Level level, BlockPos pos,
                        BlockState oldState, boolean isMoving) {
        super.onPlace(newState, level, pos, oldState, isMoving);
        checkAndDisappearIfOpen(level, pos);
    }

    /** Quand un bloc adjacent change */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        checkAndDisappearIfOpen(level, pos);
    }

    private void checkAndDisappearIfOpen(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        if (!isFullySurrounded(level, pos)) {
            // bulles + son
            spawnBubbleParticles(level, pos);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);

            // On enlève vraiment le bloc en le remplaçant par de l'air
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private boolean isFullySurrounded(Level level, BlockPos pos) {
        BlockState stateHere = level.getBlockState(pos);
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // S'il est le même bloc
            if (adjacentState.getBlock() == stateHere.getBlock()) {
                continue;
            }
            // Ou bloc solide
            if (adjacentState.isSolid()) {
                continue;
            }
            // => Au moins une face est "ouverte", on arrête direct
            return false;
        }
        return true;
    }

    private void spawnBubbleParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return; // Particules côté client uniquement
        }

        // Centre du bloc (avec une légère élévation)
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.8D; // Élévation légère pour éviter les chevauchements
        double z = pos.getZ() + 0.5D;

        /// Particules ///
        // param 1 : Nombre de particules
        // param 2 à 4 : Dispersion X, Y et Z, Avec   Point central ± dispersion.
        // param 5 : Vitesse
        serverLevel.sendParticles(ParticleTypes.BUBBLE, x, y, z,
                20,
                0.3D,
                0.5D,
                0.3D,
                0.01D
        );
    }
}