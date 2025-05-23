package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Bioreactor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SmallBioreactorRenderer {
    private static ItemStack BACTERIA_ICON = null;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        if (BACTERIA_ICON == null) {
            Item bacteriaItem = BioreactorItems.BACTERIA_ESCHERICHIA_COLI.get();
            if (bacteriaItem == null) {
                return;
            }
            BACTERIA_ICON = new ItemStack(bacteriaItem);
        }

        if (BACTERIA_ICON == null) {
            return;
        }

        ItemStack helmet = player.getInventory().armor.get(3);
        if (!helmet.is(AllItems.GOGGLES.get())) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            //System.out.println("Not hitting a block");
            return;
        }

        BlockPos targetPos = blockHit.getBlockPos();
        BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
        if (blockEntity == null) {
            //System.out.println("No block entity at " + targetPos);
            // Allow Blaze Burner check even if entity is null
        }
        //System.out.println("Block entity at " + targetPos + ": " + (blockEntity != null ? blockEntity.getClass().getSimpleName() : "null"));

        List<Component> tooltip = new ArrayList<>();

        BlockPos chamberPos = findChamberPosition(targetPos, blockEntity);
        if (chamberPos == null) {
            //System.out.println("Structure not found at " + targetPos);
            return;
        }

        AnaerobicChamberBlockEntity chamber = (AnaerobicChamberBlockEntity) mc.level.getBlockEntity(chamberPos);
        BasinBlockEntity basin = (BasinBlockEntity) mc.level.getBlockEntity(chamberPos.below());

        populateTooltip(tooltip, targetPos, blockEntity, chamber, basin);

        if (tooltip.isEmpty()) {
            //System.out.println("No tooltip information for " + targetPos);
            return;
        }

        renderOverlay(event.getGuiGraphics(), tooltip, mc);
    }

    private static BlockPos findChamberPosition(BlockPos targetPos, BlockEntity targetEntity) {
        Minecraft mc = Minecraft.getInstance();
        if (targetEntity instanceof AnaerobicChamberBlockEntity) {
            return isStructureComplete(targetPos, mc) ? targetPos : null;
        } else if (targetEntity instanceof BasinBlockEntity) {
            BlockPos above = targetPos.above();
            if (mc.level.getBlockEntity(above) instanceof AnaerobicChamberBlockEntity) {
                return isStructureComplete(above, mc) ? above : null;
            }
        } else if (mc.level.getBlockState(targetPos).is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get())) { // Check block state
            BlockPos aboveBasin = targetPos.above(2);
            if (mc.level.getBlockEntity(targetPos.above()) instanceof BasinBlockEntity &&
                    mc.level.getBlockEntity(aboveBasin) instanceof AnaerobicChamberBlockEntity) {
                return isStructureComplete(aboveBasin, mc) ? aboveBasin : null;
            }
        } else if (targetEntity instanceof MechanicalMixerBlockEntity) {
            BlockPos below = targetPos.below();
            if (mc.level.getBlockEntity(below) instanceof AnaerobicChamberBlockEntity) {
                return isStructureComplete(below, mc) ? below : null;
            }
        }
        return null;
    }

    private static boolean isStructureComplete(BlockPos chamberPos, Minecraft mc) {
        BlockEntity basinEntity = mc.level.getBlockEntity(chamberPos.below());
        BlockEntity blazeBurnerEntity = mc.level.getBlockEntity(chamberPos.below(2));
        BlockEntity mixerEntity = mc.level.getBlockEntity(chamberPos.above());

        boolean hasBlazeBurner = mc.level.getBlockState(chamberPos.below(2)).is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get());
        boolean isComplete = basinEntity instanceof BasinBlockEntity &&
                hasBlazeBurner &&
                mixerEntity instanceof MechanicalMixerBlockEntity;

        if (!isComplete) {
            //System.out.println("Structure incomplete at chamberPos: " + chamberPos);
            //System.out.println("Basin: " + (basinEntity != null ? basinEntity.getClass().getSimpleName() : "null") + " at " + chamberPos.below());
            //System.out.println("Blaze Burner Block: " + mc.level.getBlockState(chamberPos.below(2)).getBlock().getDescriptionId() + " Entity: " + (blazeBurnerEntity != null ? blazeBurnerEntity.getClass().getSimpleName() : "null") + " at " + chamberPos.below(2));
            //System.out.println("Mixer: " + (mixerEntity != null ? mixerEntity.getClass().getSimpleName() : "null") + " at " + chamberPos.above());
        }

        return isComplete;
    }

    private static void populateTooltip(List<Component> tooltip, BlockPos targetPos, BlockEntity targetEntity, AnaerobicChamberBlockEntity chamber, BasinBlockEntity basin) {

        // --------- Mixer ---------
        if (targetEntity instanceof MechanicalMixerBlockEntity) {
            int currentRPM = chamber.getLastCheckedRPM();
            int abs_currentRPM = Math.abs(currentRPM); // Use absolute value for comparison
            int minRPM = 120;
            int maxRPM = 200;

            if (abs_currentRPM > maxRPM) {
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.rpm_too_high_lign1"));
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.rpm_too_high_lign2").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.max_rpm").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(maxRPM)).withStyle(ChatFormatting.AQUA)));
            }

            if (abs_currentRPM < minRPM) {
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.rpm_too_low_lign1"));
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.rpm_too_low_lign2").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.min_rpm").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(minRPM)).withStyle(ChatFormatting.AQUA)));
            }
        }

        // --------- Basin ---------
        else if (targetEntity instanceof BasinBlockEntity) {

            // Add water info
            FluidStack water = basin.getTanks().getFirst().getPrimaryHandler().getFluidInTank(0);
            if (!water.isEmpty()) {
                int amount = water.getAmount();
                int salinity = water.getOrCreateTag().getInt("salinity");
                int pH = water.getOrCreateTag().getInt("pH");

                String waterKey = salinity == 0 ? "tooltip.bioreactor.freshwater" : "tooltip.bioreactor.seawater";
                ChatFormatting waterColor = salinity == 0 ? ChatFormatting.BLUE : ChatFormatting.GRAY;
                ChatFormatting phColor;

                if (pH >= 1 && pH <= 3) { phColor = ChatFormatting.RED; }
                else if (pH >= 4 && pH <= 5) { phColor = ChatFormatting.GOLD; }
                else if (pH >= 6 && pH <= 8) { phColor = ChatFormatting.GREEN; }
                else if (pH >= 9 && pH <= 11) { phColor = ChatFormatting.DARK_AQUA; }
                else { phColor = ChatFormatting.DARK_PURPLE; }

                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.water_info",
                        Component.literal(String.valueOf(amount) + "mB").withStyle(ChatFormatting.BLUE),
                        Component.literal("of"),
                        Component.translatable(waterKey).withStyle(waterColor),
                        Component.literal(String.valueOf(pH)).withStyle(phColor)));
            }

//            if (chamber.getRecipeProcessor() != null && chamber.getRecipeProcessor().isBasinOutputFull()) {
//                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.basin_output_full"));
//            }
//            if (chamber.getRecipeProcessor() != null && chamber.getRecipeProcessor().isBasinInputInsufficient()) {
//                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.basin_input_insufficient"));
//            }
        }

        // --------- AnaerobicChamber ---------
//        else if (targetEntity instanceof AnaerobicChamberBlockEntity) {
//            if (chamber.getRecipeProcessor() != null && chamber.getRecipeProcessor().isMissingAnaerobicChamberInputFluids()) {
//                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.missing_anaerobic_chamber_input_fluids"));
//            }
//            if (chamber.getRecipeProcessor() != null && chamber.getRecipeProcessor().isInSufficientAnaerobicChamberOutputSpace()) {
//                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.insufficient_anaerobic_chamber_output_space.line1"));
//                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.insufficient_anaerobic_chamber_output_space.line2"));
//            }
//            FluidTankManager tankManager = (FluidTankManager) chamber.getTankManager();
//            tooltip.add(Component.translatable("tooltip.bioreactor.tank_content", "A", getFluidInfo(tankManager.getTankA().getPrimaryHandler())));
//            tooltip.add(Component.translatable("tooltip.bioreactor.tank_content", "B", getFluidInfo(tankManager.getTankB().getPrimaryHandler())));
//            tooltip.add(Component.translatable("tooltip.bioreactor.tank_content", "C", getFluidInfo(tankManager.getTankC().getPrimaryHandler())));
//            tooltip.add(Component.translatable("tooltip.bioreactor.total_capacity", "1000 mB"));
//        }

        // --------- Blaze Burner ---------
        else if (Minecraft.getInstance().level.getBlockState(targetPos).is(com.simibubi.create.AllBlocks.BLAZE_BURNER.get())) {
            BlazeBurnerBlock.HeatLevel heatLevel = chamber.getLastCheckedHeatLevel();
            //System.out.println("Heat Level at " + targetPos + ": " + heatLevel);
            if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE) {
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.actual_temperature").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("20°C").withStyle(ChatFormatting.WHITE)));
            }
            else if (heatLevel == BlazeBurnerBlock.HeatLevel.SMOULDERING) {
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.actual_temperature").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("37°C").withStyle(ChatFormatting.GOLD)));
            }
            else if (heatLevel == BlazeBurnerBlock.HeatLevel.KINDLED) {
                tooltip.add(Component.translatable("tooltip.bioreactor.small_bioreactor.actual_temperature").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("50°C").withStyle(ChatFormatting.DARK_RED)));
            }
        }
    }

    private static void renderOverlay(GuiGraphics graphics, List<Component> lines, Minecraft mc) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int padding = 4;
        int borderThickness = 2;
        int iconSize = 16;
        int maxTextWidth = 200;

        List<Component> wrappedLines = new ArrayList<>();
        for (Component line : lines) {
            if (mc.font.width(line) <= maxTextWidth) {
                wrappedLines.add(line);
            } else {
                List<FormattedCharSequence> splitLines = mc.font.split(line, maxTextWidth);
                for (FormattedCharSequence splitLine : splitLines) {
                    StringBuilder text = new StringBuilder();
                    splitLine.accept((index, style, codePoint) -> {
                        text.append(Character.toString(codePoint));
                        return true;
                    });
                    wrappedLines.add(Component.literal(text.toString()).setStyle(line.getStyle()));
                }
            }
        }

        int tooltipWidth = 0;
        for (Component line : wrappedLines) {
            int lineWidth = mc.font.width(line);
            if (lineWidth > tooltipWidth) tooltipWidth = lineWidth;
        }
        tooltipWidth += iconSize + padding * 3;

        int minHeight = iconSize + 2 * padding;
        int textHeight = wrappedLines.size() * mc.font.lineHeight + padding * 2;
        int tooltipHeight = Math.max(minHeight, textHeight);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int offsetX = -tooltipWidth - 20;
        int offsetY = 0;

        int x = centerX + offsetX;
        int y = centerY + offsetY - (tooltipHeight / 2);

        if (x < 10) x = 10;
        if (y < 10) y = 10;
        if (y + tooltipHeight > screenHeight - 10) y = screenHeight - tooltipHeight - 10;

        graphics.fill(x, y, x + tooltipWidth, y + tooltipHeight, 0xAA2C7F2C);
        graphics.fill(x, y, x + tooltipWidth, y + borderThickness, 0xAA0A250A);
        graphics.fill(x, y + tooltipHeight - borderThickness, x + tooltipWidth, y + tooltipHeight, 0xAA0A250A);
        graphics.fill(x, y, x + borderThickness, y + tooltipHeight, 0xAA0A250A);
        graphics.fill(x + tooltipWidth - borderThickness, y, x + tooltipWidth, y + tooltipHeight, 0xAA0A250A);

        int iconX = x + tooltipWidth - iconSize - padding;
        int iconY = y + padding;
        graphics.renderItem(BACTERIA_ICON, iconX, iconY);

        int textY = y + padding;
        for (Component line : wrappedLines) {
            int lineWidth = mc.font.width(line);
            int textX = iconX - lineWidth - padding;
            graphics.drawString(mc.font, line, textX, textY, -1);
            textY += mc.font.lineHeight;
        }

        poseStack.popPose();
    }

    private static Component getFluidInfo(IFluidHandler tank) {
        FluidStack fluidStack = tank.getFluidInTank(0);
        return fluidStack.isEmpty() ? Component.translatable("tooltip.bioreactor.tank_empty")
                : Component.literal(fluidStack.getDisplayName().getString() + ": " + fluidStack.getAmount() + " mB");
    }
}