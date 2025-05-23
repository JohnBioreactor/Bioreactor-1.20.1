package net.john.bioreactor.content.events;

import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = "bioreactor", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BioreactorTooltipHandler {




    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        //region  ─────────── GLOBAL to ALL ITEM ───────────
        // Si NBT data tag “sterile” est présent
        // Si NBT data tag “oxic” est présent
        // Si NBT data tag “anoxic” est présent

        TooltipUtils.appendNBTTooltips(event.getItemStack(), event.getToolTip());

        //endregion


        //region ─────────── spécifique d'1/+ items ───────────

        //Enriched bacteria multiple
        if (stack.is(BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get())) {
            event.getToolTip().add(Component.translatable("tooltip.bioreactor.enriched_bacteria_multiple_1"));

            if (stack.hasTag() && stack.getTag().contains("remaining_bacteria")) {String remainingBacteria = stack.getTag().getString("remaining_bacteria");
                int bacteriaCount = remainingBacteria.isEmpty() ? 0 : (int) Arrays.stream(remainingBacteria.split(",")).filter(s -> !s.isEmpty()).count();

                event.getToolTip().add(
                        Component.translatable("tooltip.bioreactor.enriched_bacteria_multiple_2",
                        Component.literal(String.valueOf(bacteriaCount)).withStyle(ChatFormatting.GOLD)));
            } else {
                event.getToolTip().add(
                        Component.translatable("tooltip.bioreactor.enriched_bacteria_multiple_2",
                        Component.literal("0").withStyle(ChatFormatting.GOLD)));
            }
        }


        // Water bucket : pH & salinity
        if (stack.is(Items.WATER_BUCKET)) {
            if (stack.hasTag() && stack.getTag().contains("pH") && stack.getTag().contains("salinity")) {
                int ph = stack.getTag().getInt("pH");
                int sal = stack.getTag().getInt("salinity");

                // Determine pH color
                ChatFormatting phColor;
                if (ph >= 1 && ph <= 3) {phColor = ChatFormatting.RED;}
                else if (ph >= 4 && ph <= 5) {phColor = ChatFormatting.GOLD;}
                else if (ph >= 6 && ph <= 8) {phColor = ChatFormatting.GREEN;}
                else if (ph >= 9 && ph <= 11) {phColor = ChatFormatting.DARK_AQUA;}
                else {phColor = ChatFormatting.DARK_PURPLE;}

                // Display pH with the appropriate color
                event.getToolTip().add(Component.literal("pH: " + ph).withStyle(phColor));
                // Display "Freshwater" or "Seawater"
                String salString = (sal == 1) ? "Seawater" : "Freshwater";
                event.getToolTip().add(
                        Component.literal(salString).withStyle(ChatFormatting.AQUA)
                );
            }
        }
        //endregion
    }
}
