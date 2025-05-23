package net.john.bioreactor.content.kinetics.axenisation;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Description:
 * Stocke les informations spécifiques à chaque type de bactérie, notamment leurs conditions optimales (par exemple, plages de pH ou de salinité marquées 🔴/🟡/🟢).
 */

/**
 * Rôle :
 * Permettre de comparer les conditions actuelles du bioréacteur avec les besoins de la bactérie.
 */

public class BacteriaData {
    private final String bacteria_id; // Identifiant unique, ex. "escherichia_coli"
    private final ItemStack axenicOutput; // Résultat axénique
    private final Map<String, ConditionRange> conditions; // Conditions optimales

    private final o2_affinity oxygen_affinity;


    public BacteriaData(String bacteria_id,
                        ItemStack axenicOutput,
                        Map<String, ConditionRange> conditions,
                        o2_affinity oxygen_affinity) {
        this.bacteria_id = bacteria_id;
        this.axenicOutput = axenicOutput;
        this.conditions = conditions;
        this.oxygen_affinity = oxygen_affinity;
    }

    /**
     * Évalue si les conditions actuelles sont optimales pour cette bactérie.
     */

    public ConditionState evaluateConditions(int salinity,
                                             int pH,
                                             String temperature,
                                             String chamberOxygen,
                                             String sampleOxygen,
                                             String metabolism) {

        ConditionState state = ConditionState.GREEN;

        /** Evaluer la salinité
         */
        ConditionRange salinityRange = conditions.get("salinity");
        if (!salinityRange.isGreen(String.valueOf(salinity))) {
            state = state.worst(salinityRange.isYellow(String.valueOf(salinity)) ? ConditionState.YELLOW : ConditionState.RED);
        }

        /** Evaluer le pH
         */
        ConditionRange pHRange = conditions.get("pH");
        if (!pHRange.isGreen(String.valueOf(pH))) {
            state = state.worst(pHRange.isYellow(String.valueOf(pH)) ? ConditionState.YELLOW : ConditionState.RED);
        }

        /** Evaluer la T°C
         */
        ConditionRange tempRange = conditions.get("temperature");
        if (!tempRange.isGreen(temperature)) {
            state = state.worst(tempRange.isYellow(temperature) ? ConditionState.YELLOW : ConditionState.RED);
        }

        /** Evaluer l'oxygène'
         */
        // Vérifications complémentaires basées sur l'échantillon :
        // nous utilisons directement l'enum oxygen_affinity pour évaluer la compatibilité.
        switch (oxygen_affinity) {

            case ANAEROBIC:
                // Pour une bactérie strictement anaérobie, la chambre doit être anoxic et l'échantillon anoxic, sion Dead_Biomass
                if (!chamberOxygen.equals("anoxic") ||    // ROUGE si l'AnaerobiChamber n'es pas anoxic
                        sampleOxygen.equals("oxic"))      // ROUGE si le sample à le tag oxic = doit être anoxic
                {state = state.worst(ConditionState.RED);} break;

            case MICROAEROPHILIC:
                // Pour une bactérie microaérophile, la chambre doit être hypoxic et l'échantillon anoxic
                if (!chamberOxygen.equals("hypoxic") ||   // ROUGE si l'AnaerobiChamber n'es pas hypoxic
                        sampleOxygen.equals("oxic"))      // ROUGE si le sample à le tag oxic = doit être anoxic
                {state = state.worst(ConditionState.RED);} break;

            case AEROBIC:
                // Pour une bactérie strictement aérobie, la chambre doit être oxic et l'échantillon oxic
                if (!chamberOxygen.equals("oxic") ||   // ROUGE si l'AnaerobiChamber n'es pas oxique
                        sampleOxygen.equals("anoxic")) // ROUGE si le sample à le tag anoxic = doit être oxic
                {state = state.worst(ConditionState.RED);} break;

            case AAF:
                // Pour AAF, nous ne forçons pas de contrainte stricte sur l'échantillon ici,
                // la logique dynamique se fera dans l'évaluation globale (par exemple, dans AxenisationRecipe)
                break;
        }


        /** check le metabolisme
         */
        ConditionRange metabolismRange = conditions.get("metabolism");
        if (metabolism != null) {
            if (metabolismRange.isGreen(metabolism)) {
                // GREEN metabolism: keep as is
            } else if (metabolismRange.isYellow(metabolism)) {
                state = state.worst(ConditionState.YELLOW);
            } else {
                state = state.worst(ConditionState.RED); // RED by default for unlisted metabolisms
            }
        } else {
            state = state.worst(ConditionState.RED); // No metabolism = RED
        }

        LOGGER.debug(
                "Evaluated conditions for " + bacteria_id +
                ": salinity=" + salinity +
                ", pH=" + pH +
                ", temperature=" + temperature +
                ", chamberOxygen=" + chamberOxygen +
                ", sampleOxygen=" + sampleOxygen +
                ", metabolism=" + metabolism +
                " -> " + state);

        return state;
    }


    /** ------- GETTER  -------
     */
    public ItemStack getAxenicOutput() {return axenicOutput.copy();}
    public String getBacteriaId() {return bacteria_id;}
    public o2_affinity getOxygenAffinity() {return oxygen_affinity;}
    public Map<String, ConditionRange> getConditions() { return conditions; }
}

    /** ------- Variables  -------
 */
enum ConditionState {
    RED,    // Conditions critiques
    YELLOW, // Conditions acceptables
    GREEN;  // Conditions optimales

    public ConditionState worst(ConditionState other) {
        return this.ordinal() < other.ordinal() ? this : other;
    }
}

enum o2_affinity {
    ANAEROBIC,
    MICROAEROPHILIC,
    AAF,       // Aérobie-Anaérobie Facultatif
    AEROBIC
}