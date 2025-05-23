package net.john.bioreactor;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BioreactorSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "bioreactor");

    public static final RegistryObject<SoundEvent> SNOW_FREEZER_BREAK = SOUND_EVENTS.register("snow_freezer_break",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bioreactor", "snow_freezer_break")));
    public static final RegistryObject<SoundEvent> SNOW_FREEZER_PLACE = SOUND_EVENTS.register("snow_freezer_place",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bioreactor", "snow_freezer_place")));

    // Méthode d'enregistrement des sons, appelez ceci dans l'initialisation de votre mod principal
    public static void registerSounds() {
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
