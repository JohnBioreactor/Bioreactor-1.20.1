package net.john.bioreactor.foundation.advancement;

import net.john.bioreactor.Bioreactor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BioreactorAdvancements {
    public static final ResourceLocation ROOT = new ResourceLocation(Bioreactor.MOD_ID, "root");
    public static final ResourceLocation SAMPLING = new ResourceLocation(Bioreactor.MOD_ID, "sampling");

    public static final ResourceLocation SAMPLING_FIRST_TIME = new ResourceLocation(Bioreactor.MOD_ID, "sampling_first_time");
    public static void triggerSamplingFirstTime(ServerPlayer player) {awardAdvancement(player, SAMPLING_FIRST_TIME);}


    public static final ResourceLocation SAMPLING_HIGH_ALTITUDE =new ResourceLocation(Bioreactor.MOD_ID, "sampling_high_altitude");
    public static void triggerSamplingHighAltitude(ServerPlayer player) {awardAdvancement(player, SAMPLING_HIGH_ALTITUDE);}






    private static void awardAdvancement(ServerPlayer player, ResourceLocation advId) {
        Advancement adv = player.server
                .getAdvancements()
                .getAdvancement(advId);
        if (adv == null) return;
        AdvancementProgress prog = player
                .getAdvancements()
                .getOrStartProgress(adv);
        if (!prog.isDone()) {
            for (String crit : prog.getRemainingCriteria()) {
                player.getAdvancements().award(adv, crit);
            }
        }
    }
}
