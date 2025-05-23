package net.john.bioreactor;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;


//thanks a lot Destroy mod <3
//https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/block/model/DestroyPartials.java
//src/main/java/com/petrolpark/destroy/block/model/DestroyPartials.java
public class BioreactorPartials {

    public static final PartialModel


            SNOW_GOLEM_SKULL = block("cooler/skull");



    private static PartialModel block(String path) { //copied from Create source code
        return
                PartialModel.of(Bioreactor.asResource("block/"+path));
    };

    public static void init() {};
}