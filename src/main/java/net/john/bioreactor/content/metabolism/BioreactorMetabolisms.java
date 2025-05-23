package net.john.bioreactor.content.metabolism;

import net.john.bioreactor.Bioreactor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BioreactorMetabolisms {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        LOGGER.info("BioreactorMetabolisms initialized");
    }
    public static Map<String, Metabolism> getMetabolisms() {
        return MetabolismLoader.getMetabolisms();
    }

}