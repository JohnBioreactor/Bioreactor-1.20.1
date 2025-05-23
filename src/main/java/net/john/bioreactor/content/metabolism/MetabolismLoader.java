package net.john.bioreactor.content.metabolism;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.john.bioreactor.Bioreactor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MetabolismLoader extends SimpleJsonResourceReloadListener {
    public static final List<String> METABOLISM_HIERARCHY = Lists.newArrayList(
            "glucose_x_o2",
            "glucose_x_sulfate",
            "glucose_x_fermentation"
    );

    public enum o2_Requirement {
        ANAEROBIC,
        MICROAEROPHILIC,
        AEROBIC
    }

    private static final Map<String, Metabolism> METABOLISMS = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final String METABOLISMS_PATH = "metabolisms";

    public MetabolismLoader() {
        super(GSON, METABOLISMS_PATH);
    }
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        METABOLISMS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();
            Metabolism metabolism = Metabolism.fromJson(json);
            String metabolismName = json.get("metabolism_ID").getAsString();
            METABOLISMS.put(metabolismName, metabolism);
        }
        LOGGER.info("Loaded metabolisms: " + METABOLISMS.keySet());

    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MetabolismLoader());
    }

    public static Map<String, Metabolism> getMetabolisms() {
        return new HashMap<>(METABOLISMS);
    }
}