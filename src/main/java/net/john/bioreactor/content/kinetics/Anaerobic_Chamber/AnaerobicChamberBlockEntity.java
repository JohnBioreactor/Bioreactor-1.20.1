package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.fluid.BioreactorFluids;
import net.john.bioreactor.content.kinetics.axenisation.AxenisationRecipe;
import net.john.bioreactor.content.kinetics.axenisation.RecipeProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnaerobicChamberBlockEntity extends SmartBlockEntity implements Container {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FluidTankManager tankManager;
    private final RecipeProcessor recipeProcessor;

    private int lastCheckedRPM = 0;
    private BlazeBurnerBlock.HeatLevel lastCheckedHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;


    //----------Particules --------
    // Flag synchronisé indiquant si une recette est en cours (côté serveur, transmis au client)
    private boolean recipeRunningClient = false;
    private int clientEffectCounter = 0;


    public AnaerobicChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        FluidTankManager tempTankManager = null;
        try {
            tempTankManager = new FluidTankManager(this);
        } catch (Exception e) {
            //LOGGER.error("Failed to initialize FluidTankManager: " + e.getMessage(), e);
        }
        this.tankManager = tempTankManager;

        RecipeProcessor tempRecipeProcessor = null;
        try {
            tempRecipeProcessor = new RecipeProcessor(this);
        } catch (Exception e) {
            //LOGGER.error("Failed to initialize RecipeProcessor: " + e.getMessage(), e);
        }
        this.recipeProcessor = tempRecipeProcessor;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        if (tankManager != null) {
            tankManager.addBehaviours(behaviours);
        } else {
            //LOGGER.error("tankManager is null in addBehaviours at " + getBlockPos());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (tankManager != null) {
            tankManager.onLoad();
        }
        updateStateFromNeighbors(); // Met à jour l'état initial (RPM, chaleur, etc.)
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        // SERVEUR
        if (!level.isClientSide) {
            if (recipeProcessor != null) {
                recipeProcessor.tick();

                // Synchronisation forcée chaque seconde pendant l'exécution
                if (level.getGameTime() % 20 == 0) { // chaque seconde exactement
                    sendData();
                }
            }
        }

        // CLIENT
        else {
            updateRPMFromMixer();
            updateHeatLevelFromBurner();

            if (recipeRunningClient) {
                clientEffectCounter++;
                spawnClientEffectsLocal();

            } else {
                // If no recipe running, reset counter to start fresh when a recipe starts
                clientEffectCounter = 0;
            }
        }

    }

    private void updateRPMFromMixer() {
        Level level = getLevel();
        if (level == null) return;

        BlockEntity mixerEntity = level.getBlockEntity(getBlockPos().above());
        if (mixerEntity instanceof MechanicalMixerBlockEntity mixer) {
            lastCheckedRPM = Math.round(mixer.getSpeed());
        } else {
            lastCheckedRPM = 0;
        }
    }

    private void updateHeatLevelFromBurner() {
        Level level = getLevel();
        if (level == null) return;

        BlockEntity burnerEntity = level.getBlockEntity(getBlockPos().below(2));
        if (burnerEntity != null && burnerEntity.getBlockState().is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get())) {
            if (burnerEntity instanceof BlazeBurnerBlockEntity burner) {
                // Utiliser la méthode existante de Create pour obtenir le niveau de chaleur
                lastCheckedHeatLevel = burner.getHeatLevelFromBlock();
                // Vérifier si le niveau est correct (débogage temporaire, à supprimer après test)
                //System.out.println("Blaze Burner Heat Level at " + getBlockPos().below(2) + ": " + lastCheckedHeatLevel);
            } else {
                lastCheckedHeatLevel = BlazeBurnerBlock.getHeatLevelOf(burnerEntity.getBlockState());
            }
        } else {
            lastCheckedHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;
        }
    }

    /**
     * Called when certain neighbor blocks (Mixer, Basin, Blaze Burner) update.
     * Ensures we update our stored state and sync it to client.
     */
    public void updateStateFromNeighbors() {
        Level level = getLevel();
        if (level == null) return;

        // Vérifier le Blaze Burner (deux blocs en dessous) via événement
        BlockEntity burnerEntity = level.getBlockEntity(getBlockPos().below(2));
        if (burnerEntity != null && burnerEntity.getBlockState().is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get())) {
            if (burnerEntity instanceof BlazeBurnerBlockEntity burner) {
                lastCheckedHeatLevel = burner.getHeatLevelFromBlock();
            } else {
                lastCheckedHeatLevel = BlazeBurnerBlock.getHeatLevelOf(burnerEntity.getBlockState());
            }
        } else {
            lastCheckedHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;
        }
        // If on server, send updated data to client
        if (!level.isClientSide) {
            this.setChanged();
            this.sendData();
        }
    }

    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();

        // Vérifier si la position est une Anaerobic Chamber
        BlockEntity chamberEntity = level.getBlockEntity(pos);
        if (chamberEntity instanceof AnaerobicChamberBlockEntity chamber) {
            // Vérifier les blocs adjacents (Mixer, Basin, Blaze Burner)
            BlockEntity mixerAbove = level.getBlockEntity(pos.above());
            BlockEntity basinBelow = level.getBlockEntity(pos.below());
            BlockEntity burnerBelow = level.getBlockEntity(pos.below(2));

            if (mixerAbove instanceof MechanicalMixerBlockEntity ||
                    basinBelow instanceof BasinBlockEntity ||
                    (burnerBelow != null && burnerBelow.getBlockState().is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get()))) {
                chamber.updateStateFromNeighbors();
            }
        }
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (tankManager != null) tankManager.write(tag, clientPacket);
        tag.putInt("LastCheckedRPM", lastCheckedRPM);
        tag.putString("LastCheckedHeatLevel", lastCheckedHeatLevel.name());
        // Synchroniser l’état de la recette côté serveur
        boolean recipeRunning = recipeProcessor != null && recipeProcessor.isRunning();
        tag.putBoolean("RecipeRunning", recipeRunning);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tankManager != null) tankManager.read(tag, clientPacket);
        lastCheckedRPM = tag.getInt("LastCheckedRPM");
        lastCheckedHeatLevel = BlazeBurnerBlock.HeatLevel.valueOf(tag.getString("LastCheckedHeatLevel"));

        // Récupérer l’état synchronisé
        this.recipeRunningClient = tag.getBoolean("RecipeRunning");
    }


    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && tankManager != null) {
            return tankManager.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }


    /** ------------ Particles and sounds ------------
     */
    private void spawnClientEffectsLocal() {
        if (clientEffectCounter % 20 == 0 && level != null) {
            double x = getBlockPos().getX() + 0.5;
            double y = getBlockPos().getY() - 0.3; // juste au-dessus du basin et sous l'AnaerobicChamber.
            double z = getBlockPos().getZ() + 0.5;

            // Plus de particules avec légers mouvements aléatoires
            for (int i = 0; i < 10; i++) { // (10 particules)
                double offsetX = (level.random.nextDouble() - 0.5) * 0.6;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
                double offsetY = level.random.nextDouble() * 0.3;
                level.addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        x + offsetX, y + offsetY, z + offsetZ,
                        0, 0.05, 0
                );
            }

            // Son plus fort, entendu plus loin
            level.playLocalSound(
                    x, y, z,
                    SoundEvents.COMPOSTER_FILL,
                    SoundSource.BLOCKS,
                    3.0F, // volume augmenté
                    1.0F, false
            );
        }
    }




    /** ------------- GETTER -------------
     */

    public String getChamberOxygenState() {
        if (tankManager == null) return "unknown";

        // On utilise les SmartFluidTankBehaviours A, B et C
        SmartFluidTankBehaviour[] tanks = {
                tankManager.getTankA(),
                tankManager.getTankB(),
                tankManager.getTankC()
        };

        int totalAirMb = 0;
        for (SmartFluidTankBehaviour tank : tanks) {
            FluidStack fluid = tank.getPrimaryHandler().getFluidInTank(0);
            if (!fluid.isEmpty() && fluid.getFluid().equals(BioreactorFluids.GAZ_AIR_SOURCE.get())) {
                totalAirMb += fluid.getAmount();
            }
        }

        if (totalAirMb == 0) {
            return "anoxic";
        } else if (totalAirMb >= 20 && totalAirMb <= 100) {
            return "hypoxic";
        } else if (totalAirMb > 100) {
            return "oxic";
        }
        return "unknown";
    }



    public int getChamberLightLevelState() {
        if (level == null) return 0;
        // On retourne le maximum de luminosité locale à la position du block
        return level.getMaxLocalRawBrightness(getBlockPos());
    }



    public int getLastCheckedRPM() { return lastCheckedRPM; }
    public BlazeBurnerBlock.HeatLevel getLastCheckedHeatLevel() { return lastCheckedHeatLevel; }
    public IFluidHandler getTankManager() {
        return tankManager.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                .orElse(null);
    }


    public RecipeProcessor getRecipeProcessor() { return recipeProcessor; }
    public Optional<AxenisationRecipe> getMatchingAxenisationRecipe() { return recipeProcessor != null ? recipeProcessor.getMatchingAxenisationRecipe() : Optional.empty(); }
    public ItemStack getInputItem() { return recipeProcessor != null ? recipeProcessor.getSelectedInoculum() : ItemStack.EMPTY; }

    // Méthodes de l'interface Container
    @Override public int getContainerSize() { return 0; }
    @Override public boolean isEmpty() { return true; }
    @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) { /* Rien à faire */ }
    @Override public void setChanged() { /* Géré par SmartBlockEntity */ }
    @Override public boolean stillValid(Player player) { return false; }
    @Override public void clearContent() { /* Rien à faire */ }
}