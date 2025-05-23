package net.john.bioreactor.integration.jei.metabolism;

import mezz.jei.api.gui.drawable.IDrawable;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;

public class LayoutHelper {
    //region 1) ----------------- Logo electron -----------------
    public static void drawElectronLogos(GuiGraphics g,
                                         IDrawable electronLogo,
                                         float logoSize,
                                         int[][] coords) {
        float scaleX = logoSize / 32f;
        float scaleY = logoSize / 32f;
        for (int[] pos : coords) {
            int x = pos[0], y = pos[1];
            g.pose().pushPose();
            // translation au coin supérieur gauche voulu, z=5 pour être au‑dessus
            g.pose().translate((float)x, (float)y, 5f);
            // réduction 32×32 → logoSize×logoSize
            g.pose().scale(scaleX, scaleY, 1f);
            electronLogo.draw(g, 0, 0);
            g.pose().popPose();
        }
    }
    //endregion

    //region 2) ----------------- quantité d'un Fluidstack avec "xxMb" -----------------
    /**
     * Dessine la quantité d'un fluidStack avec unité "Mb" :
     * - en blanc, avec ombre noire
     * - quantité à demi‑taille, unité en gras
     * - ancrée à droite du slot, 12px à partir du bord droit non‑scalé
     * - position verticale slotY+9, et z=5 pour être au‑dessus
     */
    public static void drawFluidAmount(GuiGraphics g,
                                       Font font,
                                       IDrawable slotBackground,
                                       int slotX,
                                       int slotY,
                                       FluidStack fs) {
        String amountStr = String.valueOf(fs.getAmount());
        String unitStr   = "Mb";
        float scale      = 0.5f;

        // mesurer avant scale
        int amountW = font.width(amountStr);
        int unitW   = font.width(unitStr);
        int slotW   = slotBackground.getWidth(); // normalement ~18
        float anchorX = slotW + 12f;

        g.pose().pushPose();
        // 1) translation au coin du slot, +z pour être au premier plan
        g.pose().translate(slotX, slotY + 9, 5f);
        // 2) mise à l’échelle demi‑taille
        g.pose().scale(scale, scale, 1f);

        // 3) unité "Mb" en gras, alignée à droite, 1px plus bas
        g.drawString(
                font,
                Component.literal(unitStr).withStyle(ChatFormatting.BOLD),
                (int)(anchorX - unitW),
                1,
                0xFFFFFF,
                true
        );
        // 4) quantité, immédiatement à gauche
        g.drawString(
                font,
                Component.literal(amountStr),
                (int)(anchorX - unitW - amountW),
                0,
                0xFFFFFF,
                true
        );

        g.pose().popPose();
    }
    //endregion

    //region 3) ----------------- Metabolism specificity : delta pH, o2 requirement, Light requirement -----------------
    /**
     * Dessine les spécificités communes à toutes les recettes :
     * 7.1) cadre de spécificité
     * 7.2) requireLight (logo + tooltip)
     * 7.3) oxygenRequirement (logo + tooltip)
     * 7.4) ΔpH (label + tooltip)
     *
     * @param g                    le GuiGraphics
     * @param font                 la font Minecraft
     * @param metabolism           la métabolisme courant
     * @param mouseX               position souris X
     * @param mouseY               position souris Y
     * @param requireLightFalse    icône « no light »
     * @param requireLightTrue     icône « requires light »
     * @param o2AerobicIcon        icône AEROBIC
     * @param o2MicroIcon          icône MICROAEROPHILIC
     * @param o2AnaerobicIcon      icône ANAEROBIC
     * @param specFrame            drawable du cadre de spécificité
     */
    public static void drawMetabolismSpecificities(
            GuiGraphics g,
            Font font,
            Metabolism metabolism,
            double mouseX,
            double mouseY,
            IDrawable requireLightFalse,
            IDrawable requireLightTrue,
            IDrawable o2AerobicIcon,
            IDrawable o2MicroIcon,
            IDrawable o2AnaerobicIcon,
            IDrawable specFrame
    ) {
        Minecraft mc = Minecraft.getInstance();

        // 7.1) cadre de spécificité
        {
            float scale = 0.9f;
            int x = 139, y = 32;
            g.pose().pushPose();
            g.pose().scale(scale, scale, 1f);
            specFrame.draw(g, (int)(x/scale), (int)(y/scale));
            g.pose().popPose();
        }

        // 7.2) requireLight
        {
            boolean req = metabolism.getRequireLight();
            float scale = 0.6f;
            int x = 143, y = 35;
            IDrawable icon = req ? requireLightTrue : requireLightFalse;

            // dessiner l’icône
            g.pose().pushPose();
            g.pose().scale(scale, scale, 1f);
            icon.draw(g, (int)(x/scale), (int)(y/scale));
            g.pose().popPose();

            // hit‑box + tooltip
            int w = icon.getWidth();
            int h = icon.getHeight();
            float sw = w * scale, sh = h * scale;
            if (mouseX >= x && mouseX <= x + sw
                    && mouseY >= y && mouseY <= y + sh) {
                Component rawTip = req
                        ? Component.translatable("jei.bioreactor.tooltip.require_light.true")
                        : Component.translatable("jei.bioreactor.tooltip.require_light.false");
                List<FormattedCharSequence> lines =
                        mc.font.split(rawTip, 150);
                g.renderTooltip(mc.font, lines, (int)mouseX, (int)mouseY);
            }
        }

        // 7.3) oxygenRequirement
        {
            String oxy = String.valueOf(metabolism.getOxygenRequirement());
            if (oxy != null && !oxy.isEmpty()) {
                float scale = 0.5f;
                int x = 142, y = 48;
                IDrawable icon = switch (oxy.toUpperCase(Locale.ROOT)) {
                    case "AEROBIC"         -> o2AerobicIcon;
                    case "MICROAEROPHILIC" -> o2MicroIcon;
                    case "ANAEROBIC"       -> o2AnaerobicIcon;
                    default                -> null;
                };
                if (icon != null) {
                    // dessiner l’icône
                    g.pose().pushPose();
                    g.pose().scale(scale, scale, 1f);
                    icon.draw(g, (int)(x/scale), (int)(y/scale));
                    g.pose().popPose();

                    // hit‑box + tooltip
                    int w = icon.getWidth(), h = icon.getHeight();
                    float sw = w * scale, sh = h * scale;
                    if (mouseX >= x && mouseX <= x + sw
                            && mouseY >= y && mouseY <= y + sh) {
                        Component tip = Component.translatable(
                                "jei.bioreactor.tooltip.o2." + oxy.toLowerCase(Locale.ROOT)
                        );
                        List<FormattedCharSequence> lines =
                                mc.font.split(tip, 150);
                        g.renderTooltip(mc.font, lines, (int)mouseX, (int)mouseY);
                    }
                }
            }
        }

        // 7.4) ΔpH
        {
            int adj = metabolism.getpHAdjustment();
            if (adj != 0) {
                String label = "ΔpH";
                float scale = 0.85f;
                int x = 142, y = 61;
                int textW = (int)(font.width(label) * scale);
                int textH = (int)(font.lineHeight    * scale);

                // dessiner le label
                g.pose().pushPose();
                g.pose().scale(scale, scale, 1f);
                g.drawString(
                        font,
                        Component.literal(label)
                                .withStyle(ChatFormatting.DARK_GRAY),
                        (int)(x/scale),
                        (int)(y/scale),
                        0x404040,
                        false
                );
                g.pose().popPose();

                // hit‑box + tooltip
                if (mouseX >= x && mouseX <= x + textW
                        && mouseY >= y-2 && mouseY <= y + textH) {
                    Component number = Component.literal(String.valueOf(Math.abs(adj)))
                            .withStyle(
                                    adj < 0 ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE,
                                    ChatFormatting.BOLD
                            );
                    Component tip = adj < 0
                            ? Component.translatable("jei.bioreactor.tooltip.ph.acidified",   number)
                            : Component.translatable("jei.bioreactor.tooltip.ph.alkalized",  number);
                    g.renderTooltip(font, tip, (int)mouseX, (int)mouseY);
                }
            }
        }
    }


    //endregion

}
