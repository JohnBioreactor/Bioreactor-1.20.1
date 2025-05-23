package net.john.bioreactor.content.kinetics.axenisation;

import java.util.List;


/**
 * Description:
 * Définit des plages de valeurs pour chaque paramètre environnemental (ex. pH entre 6 et 8 pour une condition 🟡 yellow).
 */

/**
 * Rôle :
 * Simplifier la vérification des conditions dans BacteriaData.
 */

public class ConditionRange {
    private final List<String> greenValues;  // Métabolismes optimaux (🟢) vert
    private final List<String> yellowValues; // Métabolismes acceptables (🟡) jaune

    public ConditionRange(List<String> greenValues, List<String> yellowValues) {
        this.greenValues = greenValues;
        this.yellowValues = yellowValues;
    }

    public boolean isGreen(String value) {
        return greenValues.contains(value);
    }

    public boolean isYellow(String value) {
        return yellowValues.contains(value);
    }

    public boolean isRed(String value) {
        return !isGreen(value) && !isYellow(value); // Tout ce qui n'est ni 🟢 (vert) ni 🟡 (jaune) est 🔴 (rouge)
    }

    // Getters
    public List<String> getGreenValues() {
        return greenValues;
    }
    public List<String> getYellowValues() {
        return yellowValues;
    }
}