package net.john.bioreactor.content.kinetics.axenisation;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.john.bioreactor.content.bacteria.BacteriaData;
import net.john.bioreactor.content.bacteria.BacteriaDataManager;
import net.john.bioreactor.content.bacteria.ConditionState;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlockEntity;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
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
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recette *unique* : toute l’axénisation passe par ce fichier.
 * La liste des bactéries est lue à la volée via BacteriaDataManager
 * en fonction du tag NBT de l’inoculum.
 */
public class AxenisationRecipe implements Recipe<AnaerobicChamberBlockEntity> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Items acceptés comme source de bactéries */
    private static final Set<Item> INOCULUM_ITEMS = Set.of(
            BioreactorItems.SAMPLE_SOIL.get(),
            BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get()
    );

    /* ---------- Singleton ---------- */
    public static final AxenisationRecipe INSTANCE =
            new AxenisationRecipe(new ResourceLocation("bioreactor", "axenisation"));

    private final ResourceLocation id;

    private AxenisationRecipe(ResourceLocation id) { this.id = id; }

    /* ---------- Chargement / cache des métabolismes ---------- */

    /** Map <ID JSON → wrapper AxenisationMetabolism> */
    private volatile Map<String, AxenisationMetabolism> metabolismsCache;

    private Map<String, AxenisationMetabolism> metabolisms() {
        if (metabolismsCache == null) {
            Map<String, Metabolism> raw = MetabolismLoader.getMetabolisms();
            Map<String, AxenisationMetabolism> tmp = new HashMap<>();
            raw.forEach((id, meta) -> tmp.put(id, new AxenisationMetabolism(meta)));
            metabolismsCache = Map.copyOf(tmp);
            LOGGER.debug("Loaded {} metabolisms from JSON", metabolismsCache.size());
        }
        return metabolismsCache;
    }

    public Map<String, AxenisationMetabolism> getMetabolisms() { return metabolisms(); }

    /**
     * Sélectionne le premier métabolisme valide, en respectant la hiérarchie
     * GREEN > YELLOW > (aucun).
     */
    String determineMetabolism(AnaerobicChamberBlockEntity chamber) {

        /* 1) collecter les métabolismes valides pour cette chambre */
        List<String> applicable = new ArrayList<>();
        for (Map.Entry<String, AxenisationMetabolism> e : metabolisms().entrySet()) {
            if (e.getValue().matches(chamber))
                applicable.add(e.getKey());
        }
        if (applicable.isEmpty())
            return null;

        /* 2) appliquer la hiérarchie officielle */
        for (String preferred : MetabolismLoader.METABOLISM_HIERARCHY) {
            if (applicable.contains(preferred)) {
                if (applicable.size() > 1)
                    LOGGER.info("[AXE] Metabolism conflict {} → selected {}",applicable, preferred);
                return preferred;
            }
        }

        /* 3) aucun des métabolismes « officiels » - on prend le premier trouvé */
        if (applicable.size() > 1)
            LOGGER.info("[AXE] Metabolism conflict (no hierarchy) {} → selected {}",applicable, applicable.get(0));

        return applicable.get(0);
    }

    /* ---------- PARTIE CRAFTING API ---------- */

    @Override
    public boolean matches(AnaerobicChamberBlockEntity chamber, Level level) {
        /* Vérifie seulement la structure (Basin + Blaze Burner + Mixer). */
        BlockPos pos = chamber.getBlockPos();
        BlockEntity below = level.getBlockEntity(pos.below());
        BlockEntity above = level.getBlockEntity(pos.above());

        return below instanceof BasinBlockEntity
            && level.getBlockState(pos.below(2)).is(AllBlocks.BLAZE_BURNER.get())
            && above instanceof MechanicalMixerBlockEntity;
    }

    @Override public ItemStack assemble(AnaerobicChamberBlockEntity chamber, RegistryAccess reg) { return evaluate(chamber); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess reg) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId()  { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return BioreactorRecipes.AXENISATION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType()   { return BioreactorRecipes.AXENISATION_TYPE; }

    /* ---------- Évaluation ---------- */

    public ItemStack evaluate(AnaerobicChamberBlockEntity chamber) {
        return evaluate(
                chamber,
                chamber.getRecipeProcessor().getSelectedInoculum(),
                chamber.getRecipeProcessor().getCurrentMetabolism()
        );
    }

    public ItemStack evaluate(AnaerobicChamberBlockEntity chamber,
                              ItemStack inoculum,
                              String metabolismId) {

        /* ----- Contexte ----- */
        BasinBlockEntity basin = chamber.getRecipeProcessor().getBasin();
        FluidStack water       = basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0);

        int salinity  = water.getOrCreateTag().getInt("salinity");
        int pH        = water.getOrCreateTag().getInt("pH");
        String temp   = chamber.getRecipeProcessor().getCurrentHeatLevel().name().toLowerCase(Locale.ROOT);

        /* ----- O₂ : utilisation de l’API chamber.getChamberOxygenState() ----- */
        String chamberO2State = chamber.getChamberOxygenState();          // « oxic » / « anoxic »
        boolean chamberHasAir = !"anoxic".equals(chamberO2State);

        boolean sampleWasOxic = inoculum.getOrCreateTag().getBoolean("oxic");

        /* ----- Bactéries concernées ----- */
        List<BacteriaData> bacteria = getRemainingBacteria(inoculum);

        LOGGER.info("[AXE] Temp={}, pH={}, salinity={}, O2={}, metabolism={}, bacteria={}",
                temp, pH, salinity, chamberO2State, metabolismId,
                bacteria.stream().map(BacteriaData::getBacteriaId).collect(Collectors.joining(",")));

        Map<BacteriaData, ConditionState> evaluation = bacteria.stream()
                .collect(Collectors.toMap(
                        b -> b,
                        b -> b.evaluateConditions(
                                salinity, pH, temp,
                                chamberHasAir, sampleWasOxic,
                                metabolismId)
                ));

        ItemStack result = new RecipeResult(evaluation).processOutcomes(bacteria);

        LOGGER.info("[AXE] Outcome → {}", result.getItem().getDescriptionId());

        return result;
    }

    /* ---------- Outil interne ---------- */
    private List<BacteriaData> getRemainingBacteria(ItemStack input) {
        if (input.isEmpty()) return List.copyOf(BacteriaDataManager.all());

        CompoundTag tag = input.getTag();
        if (tag != null && tag.contains("remaining_bacteria")) {
            return Arrays.stream(tag.getString("remaining_bacteria").split(","))
                    .map(BacteriaDataManager::get)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.copyOf(BacteriaDataManager.all());
    }

    /* ---------- Helpers publics ---------- */
    public static boolean isInoculum(ItemStack stack)          { return INOCULUM_ITEMS.contains(stack.getItem()); }
    public static Set<Item> getInoculumItems()                 { return INOCULUM_ITEMS; }
}
