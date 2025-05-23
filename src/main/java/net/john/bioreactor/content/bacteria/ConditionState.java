package net.john.bioreactor.content.bacteria;

/**
 * Représente l’état de compatibilité d’une condition expérimentale
 * vis-à-vis d’une bactérie :
 *
 * <ul>
 *   <li><b>GREEN</b> : conditions optimales</li>
 *   <li><b>YELLOW</b> : conditions tolérées avec pénalité</li>
 *   <li><b>RED</b> : conditions létales / incompatibles</li>
 * </ul>
 *
 * La méthode {@link #worst(ConditionState)} permet de conserver
 * le « pire » état lorsqu’on combine plusieurs critères.
 */
public enum ConditionState {

    GREEN,
    YELLOW,
    RED;

    /**
     * Renvoie l’état le plus défavorable entre {@code this} et {@code other}.
     * Utile pour agréger plusieurs évaluations partielles (pH, température, etc.).
     */
    public ConditionState worst(ConditionState other) {
        return this.ordinal() >= other.ordinal() ? this : other;
    }
}
