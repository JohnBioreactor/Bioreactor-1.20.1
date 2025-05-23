package net.john.bioreactor.content.kinetics.axenisation;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlockEntity;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeProcessor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MIN_RPM = 120;
    private static final int MAX_RPM = 200;
    private static final int PROCESS_TIME_TICKS = 400;  // 20 seconds at 20 ticks/sec

    private final AnaerobicChamberBlockEntity entity;
    private AxenisationRecipe currentRecipe;
    private boolean running;
    private int runningTicks;
    private int processingTicks;
    private String currentMetabolismId = null;
    private ItemStack selectedInoculum = ItemStack.EMPTY;

    private int lastCheckedRPM = 0;
    private int lastCheckedMinRPM = MIN_RPM;
    private int lastCheckedMaxRPM = MAX_RPM;
    private boolean isMissingBasinInput = false;
    private boolean isBasinOutputFull = false;

    // Flag pour éviter de démarrer une nouvelle recette tant que le mixer n'a pas terminé son ascension
    private boolean recipeAborted = false;
    public RecipeProcessor(AnaerobicChamberBlockEntity entity) {
        this.entity = entity;
        this.running = false;
        this.runningTicks = 0;
        this.processingTicks = -1;
    }

    public void tick() {

        /** ---------------- NOT RUNNING ----------------
         */
        // Only attempt to start a recipe if not already running
        if (!running) {

            MechanicalMixerBlockEntity mixer = getMechanicalMixer();
            if (mixer != null && mixer.running) {
                // Le mixer est encore en cours d'ascension, attendre.
                return;
            }
            // Réinitialiser le flag d'abandon pour ce nouveau cycle
            recipeAborted = false;



            Optional<AxenisationRecipe> recipeOpt = getMatchingAxenisationRecipe();
            // No matching recipe available
            if (!recipeOpt.isPresent()) {return;}
            currentRecipe = recipeOpt.get();


            // Étape 1/5 : Trouver l'inoculum dans le Basin
            ItemStack inoculum = getInoculumItem();
            if (inoculum.isEmpty()) {
                abortRecipe("No valid inoculum found in Basin") ; return;
            }

            selectedInoculum = inoculum.copy();

            // Étape 2/5 : Vérifier que le Basin contient au moins 1000 mB d'eau et que le mechanical mixer a une RPM valide
            BasinBlockEntity basin = getBasin();
            if (basin == null || basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0).getAmount() < 1000) {
                abortRecipe("Basin missing or insufficient water");
                return;
            }

            // Étape 3/5 : Vérifier que le mechanical mixer a une RPM valide
            int currentRPM = getMechanicalMixerRPM();
            if (Math.abs(currentRPM) < MIN_RPM || Math.abs(currentRPM) > MAX_RPM) {
                abortRecipe("RPM out of range: " + currentRPM + " (min=" + MIN_RPM + ", max=" + MAX_RPM + ")");
                return;
            }

            // Étape 4/5 : Déterminer le métabolisme applicable
            currentMetabolismId = currentRecipe.determineMetabolism(entity);
            if (currentMetabolismId == null) {
                abortRecipe("No valid metabolism found for recipe");
                return;
            }

            // Étape 5/5 : Vérifier que les items requis sont présents dans le Basin
            AxenisationMetabolism metabolism = currentRecipe.getMetabolisms().get(currentMetabolismId);
            if (!canConsumeExactInputs(basin, metabolism.getRequiredItems())) {
                abortRecipe("Required metabolic items not available in Basin at " + entity.getBlockPos().below());
                return;
            }


            // Lancer la recette
            running = true;
            runningTicks = 0;
            processingTicks = PROCESS_TIME_TICKS;
            LOGGER.debug("Started recipe with inoculum: " + selectedInoculum.getItem().getDescriptionId() +
                         ", metabolism: " + currentMetabolismId + " at " + entity.getBlockPos());

            // Via une Mixin : Faire descendre le pole du mixer au démarrage
            if (mixer instanceof IMechanicalMixer) {
                ((IMechanicalMixer) mixer).forcePoleDown(); // démarrage animation stable
                //LOGGER.debug("Mechanical Mixer pole forced down at " + mixer.getBlockPos());
            }

            entity.setChanged();
            entity.sendData();  // Envoie l'état initial au client
        }

        /** ---------------- RUNNING ----------------
         */
        // Si une recette est en cours, gérer la logique de traitement
        if (running) {

            /** 0. Avant la recette
             */
            // Si le basin ou l'eau disparaît pendant le traitement, arrêter la recette
            BasinBlockEntity basin = getBasin();
            if (basin == null || basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0).getAmount() < 1000) {
                LOGGER.debug("Basin missing or water depleted during processing at " + entity.getBlockPos().below());
                stopProcessing();  // this will reset `running` to false
                // Sync stop to client so it knows to stop effects
                entity.setChanged();
                entity.sendData();
                return;
            }

            /** 1. Pendant la recette
             */
            // Mise à jour dynamique toutes les secondes (20 ticks)
            if (runningTicks % 20 == 0) {

                // Vérifier que la vitesse du mixer reste dans la plage acceptable
                int currentRPM = getMechanicalMixerRPM();
                if (Math.abs(currentRPM) < MIN_RPM || Math.abs(currentRPM) > MAX_RPM) {
                    abortRecipe("RPM out of range during processing: " + currentRPM);
                    return;
                }

                // Vérifier que les items requis sont toujours présents dans le Basin
                AxenisationMetabolism metabolism = currentRecipe.getMetabolisms().get(currentMetabolismId);
                if (!canConsumeExactInputs(basin, metabolism.getRequiredItems())) {
                    abortRecipe("Required metabolic items missing during processing");
                    return;
                }

                // Réévaluer le métabolisme chaque 20 tick
                String newMetabolismId = currentRecipe.determineMetabolism(entity);
                if (newMetabolismId == null) {
                    abortRecipe("No valid metabolism available during processing");
                    return;
                } else if (!newMetabolismId.equals(currentMetabolismId)) {
                    LOGGER.debug("Metabolism changing from " + currentMetabolismId + " to " + newMetabolismId +
                            " at tick " + runningTicks);
                    currentMetabolismId = newMetabolismId;
                    processingTicks = PROCESS_TIME_TICKS;  // Réinitialiser le temps si le métabolisme change
                    LOGGER.debug("Processing time reset due to metabolism change.");
                }


                // Sync running state to client periodically** (keeps client updated during long process)
                entity.setChanged();
                entity.sendData();
            }

            // Décrémenter le temps restant et incrémenter le temps total
            processingTicks--;
            runningTicks++;

            /** 2. après la recette = Terminé
             */
            if (processingTicks <= 0) {

                if (currentMetabolismId == null) {
                    LOGGER.debug("Metabolism ID null at recipe completion.");
                    stopProcessing();

                    // Sync stop to client
                    entity.setChanged();
                    entity.sendData();

                    return;
                }

                // Vérifier les conditions FINALES et consommer les entrées pour produire la sortie
                AxenisationMetabolism metabolism = currentRecipe.getMetabolisms().get(currentMetabolismId);
                if (metabolism != null && metabolism.matches(entity)) {
                    // Prepare list of inputs to consume (inoculum + metabolic items)
                    List<ItemStack> inputsToConsume = new ArrayList<>();

                    // Ajouter l'inoculum (1 unité)
                    inputsToConsume.add(new ItemStack(selectedInoculum.getItem(), 1, selectedInoculum.getTag()));

                    // Ajouter les autres items requis
                    inputsToConsume.addAll(metabolism.getRequiredItems());

                    LOGGER.debug("Attempting to consume inputs at completion: " + inputsToConsume);
                    if (canConsumeExactInputs(basin, inputsToConsume)) {
                        if (consumeExactInputs(basin, inputsToConsume)) {
                                ItemStack finalResult = currentRecipe.evaluate(entity, selectedInoculum, currentMetabolismId);
                                LOGGER.debug("Evaluated final result: " + finalResult.getItem().getDescriptionId() + " at " + entity.getBlockPos());

                                // Préparer les outputs
                            List<ItemStack> allOutputs = new ArrayList<>();
                            allOutputs.add(finalResult);

                            // Appliquer le métabolisme uniquement si le résultat n’est pas dead_biomass
                            if (!finalResult.getItem().equals(BioreactorItems.DEAD_BIOMASS.get())) {
                                List<ItemStack> metabolismOutputs = metabolism.apply(entity);
                                allOutputs.addAll(metabolismOutputs);
                                LOGGER.debug("Metabolism applied, outputs added: " + metabolismOutputs);
                            } else {
                                LOGGER.debug("All bacteria are dead, skipping metabolism application.");
                            }

                            // Déposer les outputs dans le Basin
                            basin.acceptOutputs(allOutputs, Collections.emptyList(), false);
                            LOGGER.debug("Final result and metabolism outputs (if any) deposited at Basin " + basin.getBlockPos());
                        } else {
                        LOGGER.debug("Failed to consume required inputs despite availability at " + entity.getBlockPos());
                        }
                    } else {
                    LOGGER.debug("Required inputs not available in exact quantities at completion; aborting recipe.");
                    }
                } else {
                LOGGER.debug("Metabolism " + currentMetabolismId + " does not match at " + entity.getBlockPos());
                }

                // Arrêter la recette (succès ou échec)
                stopProcessing();
                entity.setChanged();
                entity.sendData();  // Synchroniser l'arrêt avec le client
            }
        }
    }


    private boolean canConsumeExactInputs(BasinBlockEntity basin, List<ItemStack> expectedInputs) {
        IItemHandler handler = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (handler == null) {
            LOGGER.debug("No item handler found for Basin at " + basin.getBlockPos());
            return false;
        }

    for (ItemStack expected : expectedInputs) {
        int requiredAmount = expected.getCount();
        int found = 0;

        boolean isInoculum = AxenisationRecipe.getInoculumItems().contains(expected.getItem());
        Item expectedItem = expected.getItem();

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack current = handler.getStackInSlot(slot);
            if (!current.isEmpty()) {
                if (isInoculum) {
                    /* ---------- INOCULUM ---------- */
                    if (expectedItem == BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get()) {
                        // comportement déjà présent
                        if ((expected.getTag() == null || expected.getTag().isEmpty())
                                && current.getItem() == expectedItem
                                && current.getTag() != null
                                && current.getTag().contains("remaining_bacteria")) {
                            found += current.getCount();
                        } else if (ItemStack.isSameItemSameTags(current, expected)) {
                            found += current.getCount();
                        }
                    } else if (expectedItem == BioreactorItems.SAMPLE_SOIL.get()
                        /* || expectedItem == BioreactorItems.SYRINGE_COW.get() */) {
                        // NEW : ignorer le NBT, seule la “coquille” de l’item compte (uniquement pour l'aspect quantitatif)


                        // Add new sample items here !






                        if (current.getItem() == expectedItem) {
                            found += current.getCount();
                        }
                    } else {
                        // autre inoculum étiqueté → comparaison stricte
                        if (ItemStack.isSameItemSameTags(current, expected)) {
                            found += current.getCount();
                        }
                    }
                } else {
                    /* ---------- ITEM MÉTABOLIQUE ---------- */
                    if (ItemStack.isSameItem(current, expected)) {
                        found += current.getCount();
                    }
                }
            }
        }
        LOGGER.debug("Checking " + expected.getItem().getDescriptionId() + " (" + (isInoculum ? "inoculum" : "metabolic")
                + "): required=" + requiredAmount + ", found=" + found);
        if (found < requiredAmount) {
            LOGGER.debug("Insufficient quantity of " + expected.getItem().getDescriptionId()
                    + " in Basin at " + basin.getBlockPos());
            return false;
        }
    }
    LOGGER.debug("All required inputs are available in Basin at " + basin.getBlockPos());
    return true;
}

    private boolean consumeExactInputs(BasinBlockEntity basin, List<ItemStack> expectedInputs) {
    IItemHandler handler = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    if (handler == null) {
        LOGGER.debug("No item handler found for Basin at " + basin.getBlockPos());
        return false;
    }

        for (ItemStack expected : expectedInputs) {
            int remaining     = expected.getCount();
            boolean isInoculum = AxenisationRecipe.getInoculumItems().contains(expected.getItem());
            Item expectedItem  = expected.getItem();

            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                ItemStack current = handler.getStackInSlot(slot);
                if (current.isEmpty()) continue;

                boolean matches;

                if (isInoculum) {
                    if (expectedItem == BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get()) {
                        if ((expected.getTag() == null || expected.getTag().isEmpty())
                                && current.getItem() == expectedItem
                                && current.getTag() != null
                                && current.getTag().contains("remaining_bacteria")) {
                            matches = true;
                        } else {
                            matches = ItemStack.isSameItemSameTags(current, expected);
                        }
                    } else if (expectedItem == BioreactorItems.SAMPLE_SOIL.get()
                        /* || expectedItem == BioreactorItems.SYRINGE_COW.get() */) {
                        matches = current.getItem() == expectedItem;   // NEW : tag ignoré (uniquement pour l'aspect quantitatif)
                    } else {
                        matches = ItemStack.isSameItemSameTags(current, expected);
                    }
                } else {
                    matches = ItemStack.isSameItem(current, expected);
                }

                if (matches) {
                    int toExtract = Math.min(current.getCount(), remaining);
                    ItemStack extracted = handler.extractItem(slot, toExtract, false);
                    remaining -= extracted.getCount();

                    LOGGER.debug("Extracted {} of {} from slot {}",
                            extracted.getCount(), expectedItem.getDescriptionId(), slot);
                }
            }

            if (remaining > 0) {
                LOGGER.debug("Failed to consume exact quantity of {}; remaining={}",
                        expectedItem.getDescriptionId(), remaining);
                return false;
            }
        }
    LOGGER.debug("Successfully consumed all required inputs from Basin at " + basin.getBlockPos());
    return true;
}


    private void stopProcessing() {
        running = false;
        processingTicks = -1;
        currentRecipe = null;
        currentMetabolismId = null;
        selectedInoculum = ItemStack.EMPTY;
        MechanicalMixerBlockEntity mixer = getMechanicalMixer();
        if (mixer instanceof IMechanicalMixer) {
            ((IMechanicalMixer) mixer).releasePole(); // lance l'ascension
            // Forcer le mixer à terminer son cycle d'ascension :
            mixer.runningTicks = 40;
        }
    }

    private void abortRecipe(String logMessage) {
        LOGGER.debug(logMessage + " at " + entity.getBlockPos());
        MechanicalMixerBlockEntity mixer = getMechanicalMixer();
        if (mixer instanceof IMechanicalMixer) {
            ((IMechanicalMixer) mixer).releasePole();
            // Forcer la fin du cycle d'ascension :
            mixer.runningTicks = 40;
        }
        running = false;
        processingTicks = -1;
        currentRecipe = null;
        currentMetabolismId = null;
        selectedInoculum = ItemStack.EMPTY;
        entity.setChanged();
        entity.sendData();
    }




    /** ---------- GETTER ----------
     */
    private ItemStack getInoculumItem() {
        BasinBlockEntity basin = getBasin();
        if (basin != null) {
            IItemHandler itemHandler = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (itemHandler != null) {
                // Parcourir tous les slots et ne retourner que l'item qui fait partie de l'inoculum
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack stack = itemHandler.getStackInSlot(slot);
                    if (!stack.isEmpty() && AxenisationRecipe.getInoculumItems().contains(stack.getItem())) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private int getMechanicalMixerRPM() {
        MechanicalMixerBlockEntity mixer = getMechanicalMixer();
        lastCheckedRPM = mixer != null ? Math.round(mixer.getSpeed()) : 0;
        return lastCheckedRPM;
    }

    private MechanicalMixerBlockEntity getMechanicalMixer() {
        BlockEntity above = entity.getLevel().getBlockEntity(entity.getBlockPos().above());
        return above instanceof MechanicalMixerBlockEntity ? (MechanicalMixerBlockEntity) above : null;
    }

    public BasinBlockEntity getBasin() {
        BlockEntity below = entity.getLevel().getBlockEntity(entity.getBlockPos().below());
        return below instanceof BasinBlockEntity ? (BasinBlockEntity) below : null;
    }

    public BlazeBurnerBlock.HeatLevel getCurrentHeatLevel() {
        BlockEntity belowBasin = entity.getLevel().getBlockEntity(entity.getBlockPos().below(2));
        if (belowBasin != null && belowBasin.getBlockState().is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get())) {
            if (belowBasin instanceof com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity) {
                return ((com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity) belowBasin).getHeatLevelFromBlock();
            } else {
                return BlazeBurnerBlock.getHeatLevelOf(belowBasin.getBlockState());
            }
        }
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    public Optional<AxenisationRecipe> getMatchingAxenisationRecipe() {
        return entity.getLevel().getRecipeManager().getRecipes().stream()
                .filter(recipe -> recipe.getType() == BioreactorRecipes.AXENISATION_TYPE)
                .map(recipe -> (AxenisationRecipe) recipe)
                .filter(recipe -> recipe.matches(entity, entity.getLevel()))
                .findFirst();
    }

    public ItemStack getSelectedInoculum() {
        return selectedInoculum.copy(); // Retourne une copie pour éviter les modifications externes
    }

    public String getCurrentMetabolism() { return currentMetabolismId; }

    public boolean isRunning() {
        return running;
    }


}