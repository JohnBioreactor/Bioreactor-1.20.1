package net.john.bioreactor.content.kinetics.axenisation;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlockEntity;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.content.metabolism.BioreactorMetabolisms;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.john.bioreactor.content.metabolism.MetabolismLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class AxenisationRecipe implements Recipe<AnaerobicChamberBlockEntity> {
    private static final Logger LOGGER = LogManager.getLogger();


    private static final Set<Item> INOCULUM_ITEMS = Set.of(
            BioreactorItems.SAMPLE_SOIL.get(),
            BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get()
    );

    private final ResourceLocation id;
    private final List<ItemStack> inputItems;
    private final List<BacteriaData> targetBacteria;
    private final Map<String, AxenisationMetabolism> metabolisms;


    public AxenisationRecipe(ResourceLocation id, List<ItemStack> inputItems, List<BacteriaData> targetBacteria) {
        this.id = id;
        this.inputItems = inputItems;
        this.targetBacteria = targetBacteria;

        // Ne charge plus les métabolismes ici (ils seront chargés lors de la première utilisation)
        this.metabolisms = new HashMap<>();

    }

    @Override
    public boolean matches(AnaerobicChamberBlockEntity chamber, Level level) {
        BlockPos chamberPos = chamber.getBlockPos();
        BlockPos basinPos = chamberPos.below();
        BlockEntity basinEntity = level.getBlockEntity(basinPos);
        if (!(basinEntity instanceof BasinBlockEntity)) {
            LOGGER.debug("No Basin found below Anaerobic Chamber at " + chamberPos);
            return false;
        }
        BasinBlockEntity basin = (BasinBlockEntity) basinEntity;
        FluidStack basinWater = basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0);
        if (basinWater.isEmpty() || !basinWater.getFluid().equals(net.minecraft.world.level.material.Fluids.WATER) || basinWater.getAmount() < 1000) {
            LOGGER.debug("Invalid water conditions in Basin at " + basinPos + ": amount=" + basinWater.getAmount());
            return false;
        }
        IItemHandler itemHandler = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (itemHandler == null) {
            LOGGER.debug("No item handler found for Basin at " + basinPos);
            return false;
        }
        boolean validInput = false;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack basinItem = itemHandler.getStackInSlot(slot);
            if (!basinItem.isEmpty()) {
                for (ItemStack expected : inputItems) {
                    if (INOCULUM_ITEMS.contains(expected.getItem())) {
                        // Pour enriched_bacteria_multiple, comparer uniquement l'item (ignore les tags)
                        if (expected.getItem() == BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get()) {
                            if (ItemStack.isSameItem(basinItem, expected)) {
                                validInput = true;
                                LOGGER.debug("Valid enriched inoculum input found: " + basinItem.getItem().getDescriptionId());
                                break;
                            }
                        } else { // Pour les autres inoculum, utiliser la comparaison stricte
                            if (ItemStack.isSameItemSameTags(basinItem, expected)) {
                                validInput = true;
                                LOGGER.debug("Valid inoculum input found: " + basinItem.getItem().getDescriptionId() + " with NBT: " + basinItem.getTag());
                                break;
                            }
                        }
                    } else {
                        if (ItemStack.isSameItem(basinItem, expected)) {
                            validInput = true;
                            LOGGER.debug("Valid metabolic input found: " + basinItem.getItem().getDescriptionId());
                            break;
                        }
                    }
                }
                if (validInput) break;
            }
        }
        if (!validInput) {
            LOGGER.debug("No valid input items found in Basin at " + basinPos);
            return false;
        }
        // Vérifier les blocs (Blaze Burner, Mechanical Mixer) – inchangé
        BlockPos blazeBurnerPos = chamberPos.below(2);
        if (!level.getBlockState(blazeBurnerPos).is(AllBlocks.BLAZE_BURNER.get())) {
            LOGGER.debug("No Blaze Burner found at " + blazeBurnerPos + " below Basin");
            return false;
        }
        BlockEntity mixerEntity = level.getBlockEntity(chamberPos.above());
        if (!(mixerEntity instanceof MechanicalMixerBlockEntity)) {
            LOGGER.debug("No Mechanical Mixer found above Anaerobic Chamber at " + chamberPos.above());
            return false;
        }
        LOGGER.debug("All required blocks found in correct positions at " + chamberPos);
        return true;
    }

    public ItemStack evaluate(AnaerobicChamberBlockEntity chamber, ItemStack inoculum, String metabolismId) {
        RecipeProcessor processor = chamber.getRecipeProcessor();
        BasinBlockEntity basin = processor.getBasin();
        FluidStack water = basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0);

        /** Récupérer les différents état
         */
        // La salinité
        int salinity = water.getOrCreateTag().getInt("salinity");

        // Le pH
        int pH = water.getOrCreateTag().getInt("pH");

        // La température du blaze burner
        String temperature = processor.getCurrentHeatLevel().name().toLowerCase();

        // La présence d'oxygène dans l'anaerobic chamber ET dans l'échantillon
        String chamberOxygen = chamber.getChamberOxygenState();
        String sampleOxygen = inoculum.getOrCreateTag().contains("oxic") ? "oxic" : "anoxic";


        LOGGER.debug("Evaluating conditions at " + chamber.getBlockPos() +
                ": pH=" + pH + ", salinity=" + salinity +
                ", temperature=" + temperature +
                ", chamberOxygen=" + chamberOxygen +
                ", sampleOxygen=" + sampleOxygen +
                ", metabolism=" + (metabolismId != null ? metabolismId : "none"));


        //------
        List<BacteriaData> bacteriaList = getRemainingBacteria(inoculum);
        Map<BacteriaData, ConditionState> outcomes = bacteriaList.stream()
                .collect(Collectors.toMap(
                        bacteria -> bacteria,
                        bacteria -> bacteria.evaluateConditions(
                                salinity,
                                pH,
                                temperature,
                                chamberOxygen,
                                sampleOxygen,
                                metabolismId)
                ));
        RecipeResult resultProcessor = new RecipeResult(outcomes);
        ItemStack result = resultProcessor.processOutcomes(bacteriaList);
        LOGGER.debug("Evaluation result: " + result.getItem().getDescriptionId());
        return result;
    }

    public ItemStack evaluate(AnaerobicChamberBlockEntity chamber) {
        return evaluate(chamber, chamber
                .getRecipeProcessor()
                .getSelectedInoculum(),
                determineMetabolism(chamber));
    }

    public String determineMetabolism(AnaerobicChamberBlockEntity chamber) {

        // -------- Assurer que les métabolismes sont chargés dans la recette --------
        if (metabolisms.isEmpty()) {
            // Récupérer la map globale des métabolismes (chargée par MetabolismLoader)
            for (Map.Entry<String, Metabolism> entry : BioreactorMetabolisms.getMetabolisms().entrySet()) {
                metabolisms.put(entry.getKey(), new AxenisationMetabolism(entry.getValue()));
            }
            LOGGER.debug("Metabolisms loaded into recipe " + this.id + ": " + metabolisms.keySet());
        }
        // Ensuite, on peut chercher le(s) métabolisme(s) correspondant(s)
        List<String> matchingMetabolisms = new ArrayList<>();
        for (Map.Entry<String, AxenisationMetabolism> entry : metabolisms.entrySet()) {
            if (entry.getValue().matches(chamber)) {
                matchingMetabolisms.add(entry.getKey());
            }
        }
        if (matchingMetabolisms.isEmpty()) {
            LOGGER.debug("No matching metabolism found among: " + metabolisms.keySet());
            return null;
        }

        // -------- Récupérer les informations --------
        BasinBlockEntity basin = chamber.getRecipeProcessor().getBasin();
        FluidStack water = basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0);
        int salinity = water.getOrCreateTag().getInt("salinity");
        int pH = water.getOrCreateTag().getInt("pH");
        String temperature = chamber.getRecipeProcessor().getCurrentHeatLevel().name().toLowerCase();
        String chamberOxygen = chamber.getChamberOxygenState();
        ItemStack inoculum = chamber.getRecipeProcessor().getSelectedInoculum();
        String sampleOxygen = inoculum.getOrCreateTag().contains("oxic") ? "oxic" : "anoxic";


        List<String> greenMetabolisms = new ArrayList<>();

        for (String metabolism : matchingMetabolisms) {

            for (BacteriaData bacteria : targetBacteria) {
                ConditionState state = bacteria.evaluateConditions(
                        salinity,
                        pH,
                        temperature,
                        chamberOxygen,
                        sampleOxygen,
                        metabolism);

                if (state == ConditionState.GREEN) {
                    greenMetabolisms.add(metabolism);
                    break;
                }
            }
        }
        if (!greenMetabolisms.isEmpty()) {
            String selected = greenMetabolisms.stream()
                    .min(Comparator.comparingInt(MetabolismLoader.METABOLISM_HIERARCHY::indexOf))
                    .orElse(greenMetabolisms.get(0));
            LOGGER.debug("Selected GREEN metabolism: " + selected + " from conflicts: " + matchingMetabolisms);
            return selected;
        }
        String selected = matchingMetabolisms.stream()
                .min(Comparator.comparingInt(MetabolismLoader.METABOLISM_HIERARCHY::indexOf))
                .orElse(matchingMetabolisms.get(0));
        LOGGER.debug("Selected metabolism by hierarchy: " + selected + " from conflicts: " + matchingMetabolisms);
        return selected;
    }

    private List<BacteriaData> getRemainingBacteria(ItemStack input) {
        if (input.isEmpty()) {
            LOGGER.debug("Input is empty, defaulting to all target bacteria: " +
                    targetBacteria.stream().map(BacteriaData::getBacteriaId).collect(Collectors.toList()));
            return targetBacteria;
        }
        if (input.getItem() == BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get() || input.getItem() == BioreactorItems.SAMPLE_SOIL.get()) {
            // Pour enriched_bacteria_multiple, si le tag "remaining_bacteria" est présent, l'utiliser ; sinon, utiliser tous les target bacteria.
            CompoundTag tag = input.getTag();
            if (tag != null && tag.contains("remaining_bacteria")) {
                String bacteriaIds = tag.getString("remaining_bacteria");
                List<String> idList = Arrays.asList(bacteriaIds.split(","));
                LOGGER.debug("Input " + input.getItem().getDescriptionId() + " contains bacteria: " + idList);
                return targetBacteria.stream()
                        .filter(bacteria -> idList.contains(bacteria.getBacteriaId()))
                        .collect(Collectors.toList());
            }
            LOGGER.debug("No bacteria tag found in " + input.getItem().getDescriptionId() + ", using all target bacteria");
            return targetBacteria;
        }
        LOGGER.debug("Input item is not a bacteria source: " + input.getItem().getDescriptionId() + ", using all target bacteria");
        return targetBacteria;
    }


    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return BioreactorRecipes.AXENISATION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return BioreactorRecipes.AXENISATION_TYPE; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack assemble(AnaerobicChamberBlockEntity chamber, RegistryAccess registryAccess) { return evaluate(chamber); }

    public List<ItemStack> getInputItems() { return inputItems; }
    public Map<String, AxenisationMetabolism> getMetabolisms() { return metabolisms; }
    public List<BacteriaData> getTargetBacteria() { return targetBacteria; }
    public static Set<Item> getInoculumItems() { return INOCULUM_ITEMS; }
}