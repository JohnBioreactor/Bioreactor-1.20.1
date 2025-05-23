package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.john.bioreactor.Bioreactor;

/**
 * Spawns bubble effect near any water with pH <=2 or >=13.
 * We call WaterPHHelper to unify partial or source water logic.
 */
@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterBubbleEffect {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null) return;
        Level level = mc.level;
        if (level == null) return;

        BlockPos center = mc.player.blockPosition();
        RandomSource rand = level.random;

        int radius = 7;
        // 6 attempts
        for (int i = 0; i < 6; i++) {
            int dx = rand.nextInt(-radius, radius+1);
            int dz = rand.nextInt(-radius, radius+1);

            BlockPos pos = center.offset(dx, 0, dz);
            int yStart = center.getY() + 4;
            int yEnd = center.getY() - 6;

            BlockPos waterPos = null;
            for (int y = yStart; y >= yEnd; y--) {
                BlockPos check = pos.atY(y);
                int pH = WaterPHHelper.getPH(level, check);
                if (pH != -1) {
                    // means it's water or partial water
                    waterPos = check;
                    break;
                }
            }
            if (waterPos == null) continue;

            int pH = WaterPHHelper.getPH(level, waterPos);
            if (pH <= 2 || pH >= 13) {
                // spawn bubble/smoke
                for (int j = 0; j < 3; j++) {
                    double px = waterPos.getX() + 0.5 + rand.nextFloat() * 0.4 - 0.2;
                    double py = waterPos.getY() + 1.0 + rand.nextFloat() * 0.2;
                    double pz = waterPos.getZ() + 0.5 + rand.nextFloat() * 0.4 - 0.2;

                    double vy = 0.05 + rand.nextFloat() * 0.05;
                    level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, vy, 0.0);
                }
            }
        }
    }
}
