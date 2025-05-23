package net.john.bioreactor;

import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.util.ForgeSoundType;

public class BioreactorSoundTypes {
    public static final ForgeSoundType SNOW_FREEZER = new ForgeSoundType(
            1.0f, 1.0f,
            () -> BioreactorSoundEvents.SNOW_FREEZER_BREAK.get(),
            () -> SoundEvents.STONE_STEP,
            () -> BioreactorSoundEvents.SNOW_FREEZER_PLACE.get(),
            () -> SoundEvents.STONE_HIT,
            () -> SoundEvents.STONE_FALL
    );
}