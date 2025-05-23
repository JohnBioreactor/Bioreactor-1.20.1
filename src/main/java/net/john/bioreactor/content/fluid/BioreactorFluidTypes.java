package net.john.bioreactor.content.fluid;

//import com.mojang.math.Vector3f;
import net.john.bioreactor.Bioreactor;
import org.joml.Vector3f;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BioreactorFluidTypes {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Bioreactor.MOD_ID);
    public static final ResourceLocation WATER_STILL_RL = new ResourceLocation("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = new ResourceLocation("block/water_flow");


    // For HEX color, may see : https://www.color-hex.com/color/333333
    // Couleur RGBA ~ 0x80FFA500 => (128,255,165,0) = orange semi-transp
    // 4 premiers caractères transparence : 0xFF, 0xE6, 0xCC, 0xB3, 0x99, 0x80, 0x66, 0x4D, 0x33, 0x1A, 0xOO
    //                      %transparence   100    90    80    70    60    50    40    30    20    10    0 (invisible)
    //Puis couleur en HEX, ex FFA500 pour Orange

    // Liste de couleurs
    // orange, FFA500 // gris sombre ++ 333333 // violet 6a329f // vert 5bcb29 // rouge f44336 // bleu 2086e3


    /**
     * ==== LIQUIDS ====
     */
    // EXEMPLE for fluids -->>  with overlay
//    public static final ResourceLocation GAZ_CH4_OVERLAY = new ResourceLocation(Bioreactor.MOD_ID, "misc/gaz_ch4_overlay");
//    public static final RegistryObject<FluidType> GAZ_CH4_FLUIDTYPE = register("gaz_ch4_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
//            0xCC333333, new Vector3f(0.2f, 0.2f, 0.2f), // Fog color ~ gris sombre
//            GAZ_CH4_OVERLAY
//    );


    /**
     * ==== GAZ ====
     */
    public static final RegistryObject<FluidType> GAZ_AIR_FLUIDTYPE = register("gaz_air_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0xE6ffffff, new Vector3f(0.2f, 0.2f, 0.2f), // blanc-gris
            null);

    public static final RegistryObject<FluidType> GAZ_CH4_FLUIDTYPE = register("gaz_ch4_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0xCC6a329f, new Vector3f(0.2f, 0.2f, 0.2f), // violet
            null);

    public static final RegistryObject<FluidType> GAZ_CO2_FLUIDTYPE = register("gaz_co2_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0xCC333333, new Vector3f(0.2f, 0.2f, 0.2f), // gris-foncé
            null);

    public static final RegistryObject<FluidType> GAZ_H2_FLUIDTYPE = register("gaz_h2_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0xCCf44336, new Vector3f(0.2f, 0.2f, 0.2f), // Rouge
            null);

    public static final RegistryObject<FluidType> GAZ_H2O_FLUIDTYPE = register("gaz_h2o_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0x992086e3, new Vector3f(0.2f, 0.2f, 0.2f), // bleu
            null);

    public static final RegistryObject<FluidType> GAZ_H2S_FLUIDTYPE = register("gaz_h2s_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0xCCFFA500, new Vector3f(1.0f, 0.5f, 0.0f), // orange
            null);

    public static final RegistryObject<FluidType> GAZ_N2_FLUIDTYPE = register("gaz_n2_fluidtype",FluidType.Properties.create().lightLevel(2).density(15).viscosity(5),
            0x995bcb29, new Vector3f(1.0f, 0.5f, 0.0f), // vert
            null);



    /**
     * Méthode de registre : on factorise la création du BioreactorBaseFluidType
     */
    private static RegistryObject<FluidType> register(String name,
                                                      FluidType.Properties properties,
                                                      int tintColor,
                                                      Vector3f fogColor,
                                                      ResourceLocation overlay) {
        return FLUID_TYPES.register(name, () ->
                new BioreactorBaseFluidType(
                        WATER_STILL_RL,
                        WATER_FLOWING_RL,
                        overlay,
                        tintColor,
                        fogColor,
                        properties
                )
        );
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}