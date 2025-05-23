package net.john.bioreactor.content.kinetics.axenisation;

import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Calcule le résultat final de la recette (item produit) en fonction des conditions et des métabolismes actifs.
 */

/**
 * Rôle :
 * Décider dynamiquement si une bactérie devient axénique, enrichie ou morte.
 */


public class RecipeResult {
    private final Map<BacteriaData, ConditionState> outcomes;

    public RecipeResult(Map<BacteriaData, ConditionState> outcomes) {
        this.outcomes = outcomes;
    }

    /**
     * Détermine l’item produit en fonction des bactéries restantes.
     */
    public ItemStack processOutcomes(List<BacteriaData> remainingBacteria) {
        // Filtrer les bactéries viables (pas de condition critique 🔴)
        List<BacteriaData> viableBacteria = new ArrayList<>();
        for (BacteriaData bacteria : remainingBacteria) {
            ConditionState state = outcomes.get(bacteria);
            if (state != ConditionState.RED) { // 🔴 élimine la bactérie
                viableBacteria.add(bacteria);
            }
        }

        if (viableBacteria.isEmpty()) {
            // Aucune bactérie viable → biomasse morte
            return new ItemStack(BioreactorItems.DEAD_BIOMASS.get());
        } else if (viableBacteria.size() == 1) {
            BacteriaData singleBacteria = viableBacteria.get(0);
            if (outcomes.get(singleBacteria) == ConditionState.GREEN) {
                // Une seule bactérie avec conditions optimales → axénique
                return singleBacteria.getAxenicOutput();
            } else {
                // Une seule bactérie mais conditions acceptables → enrichie
                return createEnrichedBacteriaItem(List.of(singleBacteria));
            }
        } else {
            // Plusieurs bactéries viables → enrichie multiple
            return createEnrichedBacteriaItem(viableBacteria);
        }
    }

    /**
     * Crée un item Enriched_bacteria_multiple avec les bactéries restantes.
     */
    private ItemStack createEnrichedBacteriaItem(List<BacteriaData> bacteriaList) {
        ItemStack enrichedItem = new ItemStack(BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get());
        List<String> bacteriaIds = new ArrayList<>();
        for (BacteriaData bacteria : bacteriaList) {
            bacteriaIds.add(bacteria.getBacteriaId()); // Identifiant unique de la bactérie
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("remaining_bacteria", String.join(",", bacteriaIds)); // Stocker comme chaîne séparée par des virgules
        enrichedItem.setTag(tag);
        return enrichedItem;
    }
}