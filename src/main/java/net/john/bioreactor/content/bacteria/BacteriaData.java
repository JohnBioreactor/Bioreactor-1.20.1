package net.john.bioreactor.content.bacteria;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

import static com.supermartijn642.fusion.FusionClient.LOGGER;

/**
 * Décrit les propriétés biologiques d'une bactérie pour le procédé
 * d'axénisation : affinité O₂, plages de tolérance physico-chimiques,
 * métabolismes préférés, item axénique de sortie.
 *
 * Les fichiers JSON sont attendus dans  data/<namespace>/bacteria/*.json  :
 *
 * {
 *   "bacteria_id":  "escherichia_coli",
 *   "axenic_item":  "bioreactor:bacteria_escherichia_coli",
 *   "o2_affinity":  "FACULTATIVE",
 *
 *   "conditions": {
 *     "salinity":    { "green": ["1"], "yellow": ["0"] },
 *     "pH":          { "green": ["7"], "yellow": ["6","8"] },
 *     "temperature": { "green": ["none"], "yellow": ["smouldering"] },
 *     "metabolism":  { "green": ["glucose_x_fermentation"],
 *                      "yellow": ["organic_acids_x_sulfate"] }
 *   }
 * }
 */
public class BacteriaData {

    /* ------------------------------------------------------------ */
    /*                       ENUMS & CONSTANTES                     */
    /* ------------------------------------------------------------ */

    public enum O2Affinity { AEROBIC, FACULTATIVE, MICROAEROPHILIC, ANAEROBIC }

    private static final String KEY_SALINITY   = "salinity";
    private static final String KEY_PH         = "pH";
    private static final String KEY_TEMPERATURE= "temperature";
    private static final String KEY_METABOLISM = "metabolism";

    /* ------------------------------------------------------------ */
    /*                         CHAMPS IMMUTABLES                    */
    /* ------------------------------------------------------------ */

    private final String id;
    private final ItemStack axenicOutput;
    private final Map<String, ConditionRange> conditions;   // clé → plage
    private final O2Affinity o2Affinity;

    /** Liste blanche/jaune des métabolismes (pré-calculée à partir de conditions.metabolism) */
    private final Set<String> greenMetabolisms;
    private final Set<String> yellowMetabolisms;

    /* ------------------------------------------------------------ */
    /*                          CONSTRUCTION                        */
    /* ------------------------------------------------------------ */

    /**
     * Construit l'objet depuis le JSON déjà parsé.
     * Lève une RuntimeException si un champ obligatoire est absent
     * ou si un item inexistant est référencé : mieux vaut échouer haut.
     */
    public static BacteriaData fromJson(JsonObject root) {
        String id = root.get("bacteria_id").getAsString();

        /* ---------- Item axénique produit ---------- */
        ResourceLocation itemRL = new ResourceLocation(root.get("axenic_item").getAsString());
        ItemStack axenic = new ItemStack(
                Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(itemRL),
                        () -> "Unknown item id in axenic_item: "+itemRL));

        /* ---------- Affinité O₂ ---------- */
        O2Affinity affinity =
                O2Affinity.valueOf(root.get("o2_affinity").getAsString().toUpperCase(Locale.ROOT));

        /* ---------- Conditions ---------- */
        JsonObject condJson = root.getAsJsonObject("conditions");
        Map<String, ConditionRange> conds = new HashMap<>();

        condJson.entrySet().forEach(entry -> {
            JsonObject obj = entry.getValue().getAsJsonObject();
            // ConditionRange.readList(JsonArray) doit exister dans la classe ConditionRange
            List<String> green  = ConditionRange.readList(obj.getAsJsonArray("green"));
            List<String> yellow = ConditionRange.readList(obj.getAsJsonArray("yellow"));
            conds.put(entry.getKey(), new ConditionRange(green, yellow));
        });

        /* ---------- Extraction rapide des métabolismes ---------- */
        Set<String> greenMeta = new HashSet<>();
        Set<String> yellowMeta = new HashSet<>();

        if (conds.containsKey(KEY_METABOLISM)) {
            ConditionRange metabRange = conds.get(KEY_METABOLISM);
            greenMeta.addAll(metabRange.green());
            yellowMeta.addAll(metabRange.yellow());
        }

        return new BacteriaData(id, axenic, conds, affinity, greenMeta, yellowMeta);
    }

    private BacteriaData(String id,
                         ItemStack axenicOutput,
                         Map<String, ConditionRange> conditions,
                         O2Affinity affinity,
                         Set<String> greenMetabolisms,
                         Set<String> yellowMetabolisms) {
        this.id                = id;
        this.axenicOutput      = axenicOutput;
        this.conditions        = Map.copyOf(conditions);
        this.o2Affinity        = affinity;
        this.greenMetabolisms  = Set.copyOf(greenMetabolisms);
        this.yellowMetabolisms = Set.copyOf(yellowMetabolisms);
    }

    /* ------------------------------------------------------------ */
    /*                      ÉVALUATION DES CONDITIONS               */
    /* ------------------------------------------------------------ */

    /**
     * Évalue l'adéquation des paramètres expérimentaux à cette bactérie.
     *
     * @param salinity         Salinité (entier arbitraire choisi par ton gameplay)
     * @param pH               pH (entier)
     * @param temperatureId    Identifiant (« none », « smouldering », …)
     * @param chamberHasAir    true si le fluide “Air” ≥1 mB est présent
     * @param sampleWasOxic    true si l’échantillon portait un tag “oxic”
     * @param activeMetabolism Identifiant du métabolisme appliqué (ou null)
     * @return {@link ConditionState#GREEN}, YELLOW ou RED
     *
     * Les règles :
     *   • chaque paramètre (salinity, pH, temperature) est comparé à
     *     sa plage (ConditionRange) → on retient le pire état ;
     *   • O₂ : une bactérie ANAEROBIC devient RED si Air présent ;
     *   • Une bactérie strictement AEROBIC devient RED si Air absent ;
     *   • Métabolisme : GREEN si id ∈ greenMetabolisms, YELLOW si dans yellow,
     *     sinon RED ; l'état final est le pire de tous.
     */
    public ConditionState evaluateConditions(int salinity,
                                             int pH,
                                             String temperatureId,
                                             boolean chamberHasAir,
                                             boolean sampleWasOxic,
                                             String activeMetabolism) {

        ConditionState state = ConditionState.GREEN;

        /* ------ paramètres quantitatifs ------ */
        state = state.worst(eval(KEY_SALINITY,   String.valueOf(salinity)));
        state = state.worst(eval(KEY_PH,         String.valueOf(pH)));
        state = state.worst(eval(KEY_TEMPERATURE,temperatureId));

        /* ------ O₂ ------ */
        switch (o2Affinity) {
            case ANAEROBIC -> {        // meurt si O₂ présent
                if (chamberHasAir) state = ConditionState.RED;
            }
            case AEROBIC -> {          // meurt si pas d’O₂
                if (!chamberHasAir) state = ConditionState.RED;
            }
            default -> { /* FACULTATIVE & MICROAEROPHILIC : tolèrent les deux */ }
        }

        /* ------ Métabolisme ------ */
        if (activeMetabolism != null) {
            if (greenMetabolisms.contains(activeMetabolism)) {
                // rien : reste green / yellow / red selon l'état courant
            } else if (yellowMetabolisms.contains(activeMetabolism)) {
                state = state.worst(ConditionState.YELLOW);
            } else {
                state = ConditionState.RED;
            }
        }


        LOGGER.debug("[AXE] {} – pH={}({}), salinity={}({}), T°={}({}), O2={}({}), metab={}({}) ⇒ {}",
                id,
                pH,                 eval(KEY_PH, String.valueOf(pH)),
                salinity,           eval(KEY_SALINITY, String.valueOf(salinity)),
                temperatureId,      eval(KEY_TEMPERATURE, temperatureId),
                chamberHasAir ? "oxic":"anoxic", o2Affinity,
                activeMetabolism,   (activeMetabolism==null?"-":
                                                    (greenMetabolisms.contains(activeMetabolism)?"GREEN":
                                                      yellowMetabolisms.contains(activeMetabolism)?"YELLOW":"RED")),
                state);

        return state;

    }

    private ConditionState eval(String key, String value) {
        ConditionRange cr = conditions.get(key);
        if (cr == null) return ConditionState.GREEN;           // aucune contrainte
        if (cr.green().contains(value))  return ConditionState.GREEN;
        if (cr.yellow().contains(value)) return ConditionState.YELLOW;
        return ConditionState.RED;
    }

    /* ------------------------------------------------------------ */
    /*                           GETTERS                            */
    /* ------------------------------------------------------------ */

    public String             getBacteriaId()      { return id; }
    public ItemStack          getAxenicOutput()    { return axenicOutput.copy(); }
    public Map<String,ConditionRange> getConditions(){ return conditions; }
    public O2Affinity         getO2Affinity()      { return o2Affinity; }
    public Set<String>        getGreenMetabolisms(){ return greenMetabolisms; }
    public Set<String>        getYellowMetabolisms(){ return yellowMetabolisms; }
}
