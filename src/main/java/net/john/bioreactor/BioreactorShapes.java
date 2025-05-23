package net.john.bioreactor;

import com.simibubi.create.AllShapes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

//thanks a lot Destroy mod <3
//https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/block/shape/DestroyShapes.java
//src/main/java/com/petrolpark/destroy/block/shape/DestroyShapes.java
public class BioreactorShapes {

    public static final VoxelShape

            BLOCK = shape(0, 0, 0, 16, 16, 16)
            .build(),

    SNOW_FREEZER = shape(1, -2, 1, 15, 14, 15)
            .build();

    public static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AllShapes.Builder(Block.box(x1, y1, z1, x2, y2, z2));
    };
}