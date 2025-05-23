package net.john.bioreactor.integration.jei.metabolism.metal_cycle;

import net.john.bioreactor.content.metabolism.Metabolism;

/** Chaque recette du cycle du metal est encapsulée dans ce wrapper pour faciliter la récupération des informations (items, fluides, quantités, etc.).
 *
 */

public class MetalCycleRecipe {
    private final Metabolism metabolism;

    public MetalCycleRecipe(Metabolism metabolism) {
        this.metabolism = metabolism;
    }

    public Metabolism getMetabolism() {
        return metabolism;
    }
}
