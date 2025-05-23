package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.john.bioreactor.Bioreactor;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkCapabilityAttacher {

    private static final ResourceLocation CAP_ID =
            new ResourceLocation(Bioreactor.MOD_ID, "water_data_cap");

    @SubscribeEvent
    public static void onAttachCapabilitiesToChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        if (chunk != null) {
            // System.out.println("[ChunkCapabilityAttacher] Attaching WaterChunkCapability to " + chunk.getPos());

            event.addCapability(CAP_ID, new WaterChunkCapability.Provider());
        }
    }
}
