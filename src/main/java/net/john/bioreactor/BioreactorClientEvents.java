package net.john.bioreactor;

import net.john.bioreactor.content.entity.BioreactorBlockEntity;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.Snow_Freezer.SnowFreezerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BioreactorClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register the renderer for the SnowFreezerBlockEntity
        event.registerBlockEntityRenderer(BioreactorBlockEntity.SNOW_FREEZER.get(), SnowFreezerRenderer::new);

        System.out.println("Successfully registered SnowFreezerRenderer for SnowFreezerBlockEntity");
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BioreactorItems.registerItemProperties();
        });
    }
}
