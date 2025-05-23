package net.john.bioreactor.content.entity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.john.bioreactor.content.block.BioreactorBlocks;
import net.john.bioreactor.content.kinetics.Anaerobic_Chamber.AnaerobicChamberBlockEntity;
import net.john.bioreactor.content.kinetics.Snow_Freezer.SnowFreezerBlockEntity;

import static net.john.bioreactor.Bioreactor.MY_REGISTRATE;

public class BioreactorBlockEntity {

        public static final BlockEntityEntry<AnaerobicChamberBlockEntity> ANAEROBIC_CHAMBER = MY_REGISTRATE
                .blockEntity("anaerobic_chamber", AnaerobicChamberBlockEntity::new)
                .validBlocks(BioreactorBlocks.ANAEROBIC_CHAMBER) // This now matches the correct BlockEntry type
                .register();


        // thanks a lot Destroy mod <3
        //https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/block/entity/DestroyBlockEntityTypes.java
        //src/main/java/com/petrolpark/destroy/block/entity/DestroyBlockEntityTypes.java
        public static final BlockEntityEntry<SnowFreezerBlockEntity> SNOW_FREEZER = MY_REGISTRATE
                .blockEntity("snow_freezer", SnowFreezerBlockEntity::new)
                .validBlocks(BioreactorBlocks.SNOW_FREEZER)
                .register();



        public static void register() {}
}
