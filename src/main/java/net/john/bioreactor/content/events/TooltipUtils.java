package net.john.bioreactor.content.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Une seule source de vérité pour les tooltips NBT
 * (Appelé à la fois par Forge et par JEI).
 */
public final class TooltipUtils {

    private TooltipUtils() {}

    public static void appendNBTTooltips(@NotNull ItemStack stack, List<Component> tooltip) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return;

        if (tag.getBoolean("sterile"))
            tooltip.add(Component.translatable("tooltip.bioreactor.sterile_material")
                    .withStyle(ChatFormatting.GREEN));

        if (tag.getBoolean("oxic"))
            tooltip.add(Component.translatable("tooltip.bioreactor.oxygen.oxic")
                    .withStyle(ChatFormatting.AQUA));

        if (tag.getBoolean("anoxic"))
            tooltip.add(Component.translatable("tooltip.bioreactor.oxygen.anoxic")
                    .withStyle(ChatFormatting.DARK_GREEN));



//        /* Exemple bucket pH / salinité — adapte si besoin ------------------ */
//        if (tag.contains("pH")) {
//            int pH = tag.getInt("pH");
//            ChatFormatting col =   pH <= 3 ? ChatFormatting.RED
//                    : pH <= 5 ? ChatFormatting.GOLD
//                    : pH <= 8 ? ChatFormatting.GREEN
//                    : pH <=11 ? ChatFormatting.DARK_AQUA
//                    : ChatFormatting.DARK_PURPLE;
//            tooltip.add(Component.literal("pH: " + pH).withStyle(col));
//        }



//        if (tag.contains("salinity")) {
//            tooltip.add(Component.literal(tag.getInt("salinity") == 1 ? "Seawater" : "Freshwater")
//                    .withStyle(ChatFormatting.AQUA));
//        }



    }
}
