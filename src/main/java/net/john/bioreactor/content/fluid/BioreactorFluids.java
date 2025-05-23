package net.john.bioreactor.content.fluid;

import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.block.BioreactorBlocks;
import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BioreactorFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Bioreactor.MOD_ID);


    /**
     * ============ 1) On déclare les variables SANS valeur ============
     */

    public static RegistryObject<FlowingFluid> GAZ_AIR_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_AIR_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_AIR_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_CH4_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_CH4_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_CH4_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_CO2_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_CO2_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_CO2_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_H2_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_H2_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_H2_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_H2O_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_H2O_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_H2O_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_H2S_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_H2S_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_H2S_PROPERTIES;

    public static RegistryObject<FlowingFluid> GAZ_N2_SOURCE;
    public static RegistryObject<FlowingFluid> GAZ_N2_FLOWING;
    public static ForgeFlowingFluid.Properties GAZ_N2_PROPERTIES;


    /**
     * ============ 2) Dans le bloc statique, on initialise tout dans le bon ordre  ============
     */

    static {

        GAZ_AIR_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_AIR_FLUIDTYPE.get(),() -> GAZ_AIR_SOURCE.get(),() -> GAZ_AIR_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_AIR_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_AIR_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_AIR_SOURCE = FLUIDS.register("gaz_air_source",() -> new ForgeFlowingFluid.Source(GAZ_AIR_PROPERTIES));
        GAZ_AIR_FLOWING = FLUIDS.register("gaz_air_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_AIR_PROPERTIES));

        GAZ_CH4_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_CH4_FLUIDTYPE.get(),() -> GAZ_CH4_SOURCE.get(),() -> GAZ_CH4_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_CH4_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_CH4_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_CH4_SOURCE = FLUIDS.register("gaz_ch4_source",() -> new ForgeFlowingFluid.Source(GAZ_CH4_PROPERTIES));
        GAZ_CH4_FLOWING = FLUIDS.register("gaz_ch4_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_CH4_PROPERTIES));

        GAZ_CO2_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_CO2_FLUIDTYPE.get(),() -> GAZ_CO2_SOURCE.get(),() -> GAZ_CO2_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_CO2_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_CO2_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_CO2_SOURCE = FLUIDS.register("gaz_co2_source",() -> new ForgeFlowingFluid.Source(GAZ_CO2_PROPERTIES));
        GAZ_CO2_FLOWING = FLUIDS.register("gaz_co2_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_CO2_PROPERTIES));

        GAZ_H2_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_H2_FLUIDTYPE.get(),() -> GAZ_H2_SOURCE.get(),() -> GAZ_H2_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_H2_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_H2_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_H2_SOURCE = FLUIDS.register("gaz_h2_source",() -> new ForgeFlowingFluid.Source(GAZ_H2_PROPERTIES));
        GAZ_H2_FLOWING = FLUIDS.register("gaz_h2_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_H2_PROPERTIES));

        GAZ_H2O_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_H2O_FLUIDTYPE.get(),() -> GAZ_H2O_SOURCE.get(),() -> GAZ_H2O_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_H2O_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_H2O_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_H2O_SOURCE = FLUIDS.register("gaz_h2o_source",() -> new ForgeFlowingFluid.Source(GAZ_H2O_PROPERTIES));
        GAZ_H2O_FLOWING = FLUIDS.register("gaz_h2o_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_H2O_PROPERTIES));

        GAZ_H2S_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_H2S_FLUIDTYPE.get(),() -> GAZ_H2S_SOURCE.get(),() -> GAZ_H2S_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_H2S_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_H2S_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_H2S_SOURCE = FLUIDS.register("gaz_h2s_source",() -> new ForgeFlowingFluid.Source(GAZ_H2S_PROPERTIES));
        GAZ_H2S_FLOWING = FLUIDS.register("gaz_h2s_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_H2S_PROPERTIES));

        GAZ_N2_PROPERTIES = new ForgeFlowingFluid.Properties(() -> BioreactorFluidTypes.GAZ_N2_FLUIDTYPE.get(),() -> GAZ_N2_SOURCE.get(),() -> GAZ_N2_FLOWING.get())
                .block(() -> BioreactorBlocks.GAZ_N2_FLUIDBLOCK.get())
                .bucket(() -> BioreactorItems.GAZ_N2_BUCKET.get())
                .slopeFindDistance(0).levelDecreasePerBlock(0);
        GAZ_N2_SOURCE = FLUIDS.register("gaz_n2_source",() -> new ForgeFlowingFluid.Source(GAZ_N2_PROPERTIES));
        GAZ_N2_FLOWING = FLUIDS.register("gaz_n2_flowing",() -> new ForgeFlowingFluid.Flowing(GAZ_N2_PROPERTIES));

    }





    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}