package net.john.bioreactor.content.kinetics.axenisation;

import net.john.bioreactor.content.bacteria.BacteriaData;
import net.john.bioreactor.content.bacteria.ConditionState;
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
        List<BacteriaData> greens = new ArrayList<>();

        for (BacteriaData bacteria : remainingBacteria) {
            ConditionState state = outcomes.get(bacteria);
            if (state != ConditionState.RED) { // 🔴 élimine la bactérie
                viableBacteria.add(bacteria);

                if (state == ConditionState.GREEN) {
                    greens.add(bacteria);  // mémoriser les GREEN
                }

            }
        }

        /* --- Aucune bactérie viable → biomasse morte --- */
        if (viableBacteria.isEmpty()) {
            return new ItemStack(BioreactorItems.DEAD_BIOMASS.get());
        }

        /* --- EXACTEMENT UNE bactérIe GREEN → axénique --- */
        if (greens.size() == 1) {
            return greens.get(0).getAxenicOutput();
        }


        if (viableBacteria.size() == 1) {
            BacteriaData single = viableBacteria.get(0);
            if (outcomes.get(single) == ConditionState.GREEN) {
                return single.getAxenicOutput();
            } else {
                return createEnrichedBacteriaItem(List.of(single));
            }
        }

        return createEnrichedBacteriaItem(viableBacteria);   // plusieurs viables

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