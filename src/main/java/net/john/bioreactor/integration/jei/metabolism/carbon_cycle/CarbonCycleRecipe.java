package net.john.bioreactor.integration.jei.metabolism.carbon_cycle;

import net.john.bioreactor.content.metabolism.Metabolism;

/** Chaque recette du cycle du carbone est encapsulée dans ce wrapper pour faciliter la récupération des informations (items, fluides, quantités, etc.).
 *
 */

public class CarbonCycleRecipe {
    private final Metabolism metabolism;

    public CarbonCycleRecipe(Metabolism metabolism) {
        this.metabolism = metabolism;
    }

    public Metabolism getMetabolism() {
        return metabolism;
    }
}
