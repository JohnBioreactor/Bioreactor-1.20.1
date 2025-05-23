package net.john.bioreactor.content.block;

import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.john.bioreactor.BioreactorCreativeTab;
import net.john.bioreactor.BioreactorSoundTypes;
import net.john.bioreactor.content.fluid.*;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlock;
import net.john.bioreactor.content.kinetics.Snow_Freezer.SnowFreezerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static net.john.bioreactor.Bioreactor.MY_REGISTRATE;

public class BioreactorBlocks {

    static {
        MY_REGISTRATE.setCreativeTab(BioreactorCreativeTab.BIOREACTOR_TAB);
    }

    /**
     * ============ GAZ ============
     */

    public static final BlockEntry<gaz_air_fluidblock> GAZ_AIR_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_air",
                    p -> new gaz_air_fluidblock(
                            () -> BioreactorFluids.GAZ_AIR_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();

    public static final BlockEntry<gaz_ch4_fluidblock> GAZ_CH4_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_ch4",
                    p -> new gaz_ch4_fluidblock(
                            () -> BioreactorFluids.GAZ_CH4_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();

    public static final BlockEntry<gaz_co2_fluidblock> GAZ_CO2_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_co2",
                    p -> new gaz_co2_fluidblock(
                            () -> BioreactorFluids.GAZ_CO2_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();

    public static final BlockEntry<gaz_h2_fluidblock> GAZ_H2_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_h2",
                    p -> new gaz_h2_fluidblock(
                            () -> BioreactorFluids.GAZ_H2_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();

    public static final BlockEntry<gaz_h2o_fluidblock> GAZ_H2O_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_h2o",
                    p -> new gaz_h2o_fluidblock(
                            () -> BioreactorFluids.GAZ_H2O_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();
    public static final BlockEntry<gaz_h2s_fluidblock> GAZ_H2S_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_h2s",
                    p -> new gaz_h2s_fluidblock(
                            () -> BioreactorFluids.GAZ_H2S_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();

    public static final BlockEntry<gaz_n2_fluidblock> GAZ_N2_FLUIDBLOCK = MY_REGISTRATE
            .block("gaz_n2",
                    p -> new gaz_n2_fluidblock(
                            () -> BioreactorFluids.GAZ_N2_SOURCE.get(),
                            BlockBehaviour.Properties
                                    .copy(Blocks.WATER)
                                    .noOcclusion()
                    )
            )
            .register();


    /**
     * ============ BLOCKS ============
     */


    public static final BlockEntry<AnaerobicChamberBlock> ANAEROBIC_CHAMBER = MY_REGISTRATE
            .block("anaerobic_chamber", AnaerobicChamberBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .item()
            .build().register();




    // thanks a lot Destroy mod <3
    //https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/block/DestroyBlocks.java
    //src/main/java/com/petrolpark/destroy/block/DestroyBlocks.java
    public static final BlockEntry<SnowFreezerBlock> SNOW_FREEZER = MY_REGISTRATE
            .block("snow_freezer", SnowFreezerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_GRAY)
                    .noOcclusion()
                    .sound(BioreactorSoundTypes.SNOW_FREEZER)
            ).transform(TagGen.pickaxeOnly())
            .item()
            .transform(customItemModel())
            .register();



    public static void register() {}
}
