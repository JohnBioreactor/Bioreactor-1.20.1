package net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle;

import net.john.bioreactor.content.metabolism.Metabolism;

/** Chaque recette du cycle du N est encapsulée dans ce wrapper pour faciliter la récupération des informations (items, fluides, quantités, etc.).
 *
 */

public class NitrogenCycleRecipe {
    private final Metabolism metabolism;

    public NitrogenCycleRecipe(Metabolism metabolism) {
        this.metabolism = metabolism;
    }

    public Metabolism getMetabolism() {
        return metabolism;
    }
}
