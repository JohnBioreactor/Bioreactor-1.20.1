package net.john.bioreactor.content.mixin.heatlevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

  // thanks a lot Destroy mod <3
  //https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/mixin/BlazeBurnerBlockEntityMixin.java
  //src/main/java/com/petrolpark/destroy/mixin/BlazeBurnerBlockEntityMixin.java

@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin {

    @Inject(
            method = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;applyCreativeFuel()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;setBlockHeat(Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock$HeatLevel;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false,
            cancellable = true
    )
    public void inApplyCreativeFuel(CallbackInfo ci, HeatLevel next) {
        if ("FROSTING".equals(next.name())) {
            ci.cancel();
            invokeSetBlockHeat(HeatLevel.SMOULDERING);
        };
    };

    @Invoker("setBlockHeat")
    public abstract void invokeSetBlockHeat(HeatLevel heat);
};