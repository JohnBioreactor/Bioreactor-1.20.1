package net.john.bioreactor.integration.jei.metabolism.sulfur_cycle;

import net.john.bioreactor.content.metabolism.Metabolism;

/** Chaque recette du cycle du S est encapsulée dans ce wrapper pour faciliter la récupération des informations (items, fluides, quantités, etc.).
 *
 */

public class SulfurCycleRecipe {
    private final Metabolism metabolism;

    public SulfurCycleRecipe(Metabolism metabolism) {
        this.metabolism = metabolism;
    }

    public Metabolism getMetabolism() {
        return metabolism;
    }
}
