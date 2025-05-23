package net.john.bioreactor.content.bacteria;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Représente, pour un paramètre donné (pH, salinité, température, métabolisme…),
 * les valeurs considérées comme :
 *   • {@link ConditionState#GREEN} : optimales
 *   • {@link ConditionState#YELLOW} : acceptables avec pénalité
 * Toutes les autres valeurs sont implicitement {@link ConditionState#RED}.
 *
 * Les valeurs sont stockées sous forme de chaînes ; leur
 * interprétation (entier, mot-clé, id de métabolisme…) est laissée
 * au code appelant.
 */
public class ConditionRange {

    private final List<String> green;
    private final List<String> yellow;

    /* ------------------------------------------------------------ */
    /*                       CONSTRUCTION                           */
    /* ------------------------------------------------------------ */

    /**
     * @param green  liste de valeurs optimales
     * @param yellow liste de valeurs sous-optimales
     */
    public ConditionRange(List<String> green, List<String> yellow) {
        this.green  = List.copyOf(green);
        this.yellow = List.copyOf(yellow);
    }

    /* ------------------------------------------------------------ */
    /*                        UTILITAIRES                           */
    /* ------------------------------------------------------------ */

    /**
     * Parse un tableau JSON simple `["v1","v2",...]` en liste de chaînes
     * normalisées en minuscule.
     */
    public static List<String> readList(JsonArray array) {
        if (array == null) return List.of();
        List<String> out = new ArrayList<>(array.size());
        array.forEach(el -> out.add(el.getAsString().toLowerCase(Locale.ROOT)));
        return out;
    }

    /**
     * Classe la valeur donnée selon cette plage.
     */
    public ConditionState evaluate(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (green.contains(v))  return ConditionState.GREEN;
        if (yellow.contains(v)) return ConditionState.YELLOW;
        return ConditionState.RED;
    }

    /* ------------------------------------------------------------ */
    /*                          GETTERS                             */
    /* ------------------------------------------------------------ */

    /** Valeurs optimales (liste immuable). */
    public List<String> green()  { return green;  }

    /** Valeurs acceptables (liste immuable). */
    public List<String> yellow() { return yellow; }
}
