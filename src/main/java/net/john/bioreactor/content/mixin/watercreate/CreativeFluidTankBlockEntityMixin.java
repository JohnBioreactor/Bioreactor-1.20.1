package net.john.bioreactor.content.mixin.watercreate;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.john.bioreactor.foundation.waterphsalinity.create.BioreactorCreativeFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CreativeFluidTankBlockEntity.class)
public abstract class CreativeFluidTankBlockEntityMixin {

    @Overwrite(remap = false)
    protected SmartFluidTank createInventory() {
        Level level = ((BlockEntity)(Object)this).getLevel();
        BlockPos pos = ((BlockEntity)(Object)this).getBlockPos();

        var accessor = (FluidTankBlockEntityAccessorMixin)(Object)this;
        return new BioreactorCreativeFluidTank(
                CreativeFluidTankBlockEntity.getCapacityMultiplier(),
                fluidStack -> accessor.bioreactor_callOnFluidStackChanged(fluidStack),
                level,
                pos
        );
    }
}
