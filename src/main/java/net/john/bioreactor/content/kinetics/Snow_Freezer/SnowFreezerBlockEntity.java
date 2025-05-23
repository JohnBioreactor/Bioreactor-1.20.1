package net.john.bioreactor.content.kinetics.Snow_Freezer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SnowFreezerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final int TANK_CAPACITY = 1000;
    private static final int ITEM_COOLING_TICKS = 400;
    private static final int FLUID_COOLING_TICKS = 200;
    private static final int MAX_COOLING_TICKS = 12000;

    private SmartFluidTankBehaviour tank;
    private ItemStack inventoryStack = ItemStack.EMPTY;  // Stack pour stocker les carottes
    private final LazyOptional<IFluidHandler> fluidHandler;
    private final LazyOptional<IItemHandler> itemHandler;
    private final LerpedFloat headAnimation;
    private final LerpedFloat headAngle;
    protected int coolingTicks;

    public SnowFreezerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        fluidHandler = LazyOptional.of(() -> tank.getPrimaryHandler());
        itemHandler = LazyOptional.of(() -> new SnowFreezerItemHandler(this));

        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
    }

    public ItemStack getInventoryStack() {
        return inventoryStack;
    }

    public void setInventoryStack(ItemStack stack) {
        this.inventoryStack = stack;
    }

    public void clearInventory() {
        this.inventoryStack = ItemStack.EMPTY;
    }

    public void consumeCarrot() {
        if (!inventoryStack.isEmpty() && inventoryStack.is(Items.CARROT)) {
            setColdnessOfBlock(ColdnessLevel.FROSTING);  // Passe à l'état FROSTING
            coolingTicks += ITEM_COOLING_TICKS;  // Ajoute les ticks de refroidissement
            inventoryStack.shrink(1);  // Réduit le nombre de carottes de 1
            notifyUpdate();  // Notifie le changement
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, TANK_CAPACITY, true)
                .whenFluidUpdates(this::consumeFluid)
                .forbidExtraction();
        behaviours.add(tank);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            if (coolingTicks <= 0) {
                consumeCoolingResource();
            }
            updateCoolingTicks();
        }

        if (getLevel().isClientSide()) {
            tickAnimation();
            spawnParticles(getColdnessFromBlock());
        }
    }

    private void consumeCoolingResource() {
        consumeFluid();
        consumeCarrot();
    }

    private void consumeFluid() {
        var fluidStack = tank.getPrimaryHandler().getFluid();
        if (fluidStack.getFluid().is(FluidTags.WATER) && fluidStack.getAmount() >= 100) {
            setColdnessOfBlock(ColdnessLevel.FROSTING);
            coolingTicks += FLUID_COOLING_TICKS;
            tank.getPrimaryHandler().drain(100, IFluidHandler.FluidAction.EXECUTE);
            notifyUpdate();
        }
    }

    private void updateCoolingTicks() {
        if (coolingTicks > 0) {
            coolingTicks--; // Diminue le temps de refroidissement chaque tick
            if (coolingTicks <= 0) {
                setColdnessOfBlock(ColdnessLevel.NONE); // Réinitialise l'état lorsque le temps est écoulé
            } else if (coolingTicks < MAX_COOLING_TICKS) {
                tank.allowInsertion();
            }
            sendData(); // Actualise les données du bloc
        }
    }

    public LerpedFloat getHeadAnimation() {
        return headAnimation;
    }

    public LerpedFloat getHeadAngle() {
        return headAngle;
    }

    @SuppressWarnings("null")
    public void updateHeatLevel(ColdnessLevel coldnessLevel) {
        if (!hasLevel()) return;
        BlockState newState = getBlockState().setValue(BlazeBurnerBlock.HEAT_LEVEL, coldnessLevel == ColdnessLevel.FROSTING ? HeatLevel.valueOf("FROSTING") : HeatLevel.NONE);
        if (!newState.equals(getBlockState())) {
            getLevel().setBlockAndUpdate(getBlockPos(), newState);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation() {
        float target = 0;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !player.isInvisible()) {
            double x = player.getX();
            double z = player.getZ();
            double dx = x - (getBlockPos().getX() + 0.5);
            double dz = z - (getBlockPos().getZ() + 0.5);
            target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
        }
        target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
        headAngle.chase(target, 0.25f, LerpedFloat.Chaser.exp(5));
        headAngle.tickChaser();
        headAnimation.chase(validBlockAbove() ? 1 : 0, 0.25f, LerpedFloat.Chaser.exp(0.25f));
        headAnimation.tickChaser();
    }

    @SuppressWarnings("null")
    protected void spawnParticles(ColdnessLevel coldnessLevel) {
        if (!hasLevel() || coldnessLevel == ColdnessLevel.NONE) return;
        RandomSource r = getLevel().getRandom();
        Vec3 c = VecHelper.getCenterOf(getBlockPos());
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f).multiply(1, 0, 1));
        if (r.nextInt(coldnessLevel == ColdnessLevel.IDLE ? 32 : 2) != 0) return;

        boolean empty = level.getBlockState(getBlockPos().above()).getCollisionShape(getLevel(), getBlockPos().above()).isEmpty();
        if (empty || r.nextInt(8) == 0) getLevel().addParticle(ParticleTypes.SNOWFLAKE, v.x, v.y, v.z, 0, 0.07D, 0);
    }

    private boolean validBlockAbove() {
        if (!hasLevel()) return false;
        BlockState blockState = getLevel().getBlockState(worldPosition.above());
        return blockState.is(AllBlocks.BASIN.get()) || blockState.getBlock() instanceof FluidTankBlock;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        fluidHandler.invalidate();
        itemHandler.invalidate();
    }

    public ColdnessLevel getColdnessFromBlock() {
        return SnowFreezerBlock.getColdnessLevelOf(getBlockState());
    }

    public void setColdnessOfBlock(ColdnessLevel coldnessLevel) {
        if (!hasLevel()) return;
        getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(SnowFreezerBlock.COLD_LEVEL, coldnessLevel));
        updateHeatLevel(coldnessLevel);
    }

    public enum ColdnessLevel implements StringRepresentable {
        NONE, IDLE, FROSTING;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    // Temps de refroidissement restant
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Calcul du temps restant en minutes et secondes
        int totalSeconds = coolingTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeRemaining = (minutes > 0 ? minutes + "m " : "") + seconds + "s";

        // Ajouter un texte aligné pour le temps restant
        tooltip.add(Component.literal("    Time Remaining: " + timeRemaining).withStyle(ChatFormatting.GRAY));

        return true;
    }

}
