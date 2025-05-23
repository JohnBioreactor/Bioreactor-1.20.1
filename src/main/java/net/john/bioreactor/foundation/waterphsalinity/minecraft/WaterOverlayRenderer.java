package net.john.bioreactor.foundation.waterphsalinity.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an overlay showing water pH & sal.
 *
 * 1) If pH=-1 => Not recognized as water => skip
 * 2) Otherwise => show "pH=?, Freshwater or Seawater"
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "bioreactor", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterOverlayRenderer {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // Must wear Create goggles
        ItemStack helmet = player.getInventory().armor.get(3);
        if (!helmet.is(AllItems.GOGGLES.get())) {
            return;
        }

        // Ray trace ~20 blocks
        HitResult fluidHit = player.pick(20.0F, event.getPartialTick(), true);
        if (fluidHit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) fluidHit;
        BlockPos targetPos = blockHit.getBlockPos();
        BlockState blockState = mc.level.getBlockState(targetPos);

        // Get final pH/sal from WaterPHHelper
        int[] arr = WaterPHHelper.getPHAndSal(mc.level, targetPos);
        int ph = arr[0];
        int sal = arr[1];
        if (ph == -1) {
            return; // not recognized as water
        }

        // Build lines
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("pH: " + ph));
        lines.add(Component.literal((sal == 1) ? "Seawater" : "Freshwater"));

        // Render tooltip
        renderOverlay(event, lines, mc);
    }

    private static void renderOverlay(RenderGuiOverlayEvent.Pre event, List<Component> lines, Minecraft mc) {
        GuiGraphics graphics = event.getGuiGraphics();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int iconSize = 16;
        int padding = 4;
        int borderThickness = 2;
        int offsetFromLeft = 10;
        int verticalOffset = 30;

        // compute tooltip size
        int tooltipWidth = 0;
        for (Component line : lines) {
            int lineWidth = mc.font.width(line);
            tooltipWidth = Math.max(tooltipWidth, lineWidth);
        }
        tooltipWidth += iconSize + padding * 3;
        int tooltipHeight = lines.size() * mc.font.lineHeight + padding * 2;

        int startX = offsetFromLeft;
        int startY = verticalOffset;

        // Colors
        Color backgroundColor = new Color(44, 127, 44, 100);
        Color borderColor = new Color(10, 37, 10, 150);

        // background
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.fill(startX, startY, startX + tooltipWidth, startY + tooltipHeight, backgroundColor.getRGB());

        // border
        graphics.fill(startX, startY, startX + tooltipWidth, startY + borderThickness, borderColor.getRGB());
        graphics.fill(startX, startY + tooltipHeight - borderThickness, startX + tooltipWidth, startY + tooltipHeight, borderColor.getRGB());
        graphics.fill(startX, startY, startX + borderThickness, startY + tooltipHeight, borderColor.getRGB());
        graphics.fill(startX + tooltipWidth - borderThickness, startY, startX + tooltipWidth, startY + tooltipHeight, borderColor.getRGB());

        // icon
        ItemStack iconStack = new ItemStack(BioreactorItems.BACTERIA_ESCHERICHIA_COLI.get());
        graphics.renderItem(iconStack, startX + padding, startY + padding);

        // lines
        int textX = startX + iconSize + padding * 2;
        int textY = startY + padding;
        for (Component line : lines) {
            graphics.drawString(mc.font, line, textX, textY, 0xFFFFFF, false);
            textY += mc.font.lineHeight;
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
