package net.john.bioreactor.content.mixin;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.john.bioreactor.content.kinetics.axenisation.IMechanicalMixer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalMixerBlockEntity.class)
public abstract class MechanicalMixerMixin extends BlockEntity implements IMechanicalMixer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow public boolean running;
    @Shadow public int runningTicks;

    // Flag indiquant la phase de descente forcée
    private boolean poleForcedDown = false;
    // Nouveau flag pour forcer l'ascension (releasePole)
    private boolean forceAscent = false;

    public MechanicalMixerMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void forcePoleDown() {
        poleForcedDown = true;
        forceAscent = false; // Assurer que l'on démarre en descente
        running = true;
        runningTicks = 0; // Démarrer la descente à 0 pour une progression douce
        LOGGER.debug("ForcePoleDown: Starting descent at {}", getBlockPos());
        setChanged();
    }

    @Override
    public void releasePole() {
        poleForcedDown = false;
        forceAscent = true; // Passer en mode ascension forcée
        if (runningTicks <= 20) {
            runningTicks = 21; // S'assurer que l'on quitte la phase de descente
        }
        LOGGER.debug("ReleasePole: Starting ascent at {}", getBlockPos());
        setChanged();
    }

    // On empêche que continueWithPreviousRecipe() ne réinitialise runningTicks si on est en phase d'ascension
    @Inject(method = "continueWithPreviousRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void onContinueWithPreviousRecipe(CallbackInfoReturnable<Boolean> cir) {
        if (!poleForcedDown && runningTicks > 20) {
            cir.setReturnValue(false);
        }
    }

    // Injection dans tick() pour gérer la progression de l'animation
    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void onTick(CallbackInfo ci) {
        if (poleForcedDown) {
            // Pendant la descente forcée, verrouiller le compteur à 20 une fois atteint
            if (runningTicks >= 20) {
                runningTicks = 20;
            }
        } else if (forceAscent) {
            // Laisser le compteur s'incrémenter naturellement pour l'ascension
            if (runningTicks >= 40) {
                // Lorsque 40 ticks sont atteints, l'ascension est terminée
                running = false;
                forceAscent = false;
                runningTicks = 0;
                LOGGER.debug("onTick: Ascent complete, stopping mixer at {}", getBlockPos());
                setChanged();
            }
        }
    }
}
