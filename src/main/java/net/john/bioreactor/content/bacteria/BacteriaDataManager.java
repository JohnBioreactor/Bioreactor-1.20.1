package net.john.bioreactor.content.bacteria;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * Charge tous les fichiers JSON du dossier data/<namespace>/bacteria
 * et expose une map <bacteria_id, BacteriaData>.
 */
public class BacteriaDataManager extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON   = new Gson();

    private static Map<String, BacteriaData> REGISTRY = Map.of();

    public BacteriaDataManager() {
        super(GSON, "bacteria");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        ImmutableMap.Builder<String, BacteriaData> builder = ImmutableMap.builder();

        objectMap.forEach((rl, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                BacteriaData data = BacteriaData.fromJson(json);
                builder.put(data.getBacteriaId(), data);
            } catch (Exception e) {
                LOGGER.error("Failed to parse bacteria file {} : {}", rl, e.getMessage());
            }
        });

        REGISTRY = builder.build();
        LOGGER.info("Loaded {} bacteria definitions", REGISTRY.size());
    }


    /* ------- GETTERS  ------- */

    public static BacteriaData get(String id)          { return REGISTRY.get(id); }
    public static Collection<BacteriaData> all()       { return REGISTRY.values(); }
}
