package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.john.bioreactor.Bioreactor;

/**
 * Applies damage or effects based on pH from WaterPHHelper.
 * This ensures the same pH is used as in bubble & overlay logic.
 */
@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterDamageHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side.isClient()) return; // server side only

        Player player = event.player;
        Level level = player.level();
        BlockPos feetPos = player.blockPosition();

        int pH = WaterPHHelper.getPH(level, feetPos);
        if (pH == -1) {
            return; // not water
        }

        // Extreme pH
        if (pH <= 2 || pH >= 13) {
            player.hurt(level.damageSources().magic(), 2.0F);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 2, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 1, true, false, false));
        }
        // moderate pH
        else if ((pH >= 3 && pH <= 5) || (pH >= 9 && pH <= 12)) {
            player.hurt(level.damageSources().magic(), 0.5F);
        }
        // pH 6..8 => no effect
    }
}
