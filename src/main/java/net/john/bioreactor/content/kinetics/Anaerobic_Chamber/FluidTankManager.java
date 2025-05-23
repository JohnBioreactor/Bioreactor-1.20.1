package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import net.john.bioreactor.content.fluid.BioreactorFluids;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTankManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int TOTAL_CAPACITY = 1000;
    private static final int INITIAL_TANK_CAPACITY = 1000; // Capacité initiale par tank, ajustée dynamiquement

    private final AnaerobicChamberBlockEntity entity;
    private final SmartFluidTankBehaviour tankA;
    private final SmartFluidTankBehaviour tankB;
    private final SmartFluidTankBehaviour tankC;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private boolean initialized = false;

    public FluidTankManager(AnaerobicChamberBlockEntity entity) {
        this.entity = entity;
        //LOGGER.info("Starting FluidTankManager initialization for entity at " + entity.getBlockPos());

        try {
            //LOGGER.info("Creating tankA with initial capacity " + INITIAL_TANK_CAPACITY);
            tankA = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.TYPE, entity, INITIAL_TANK_CAPACITY, INITIAL_TANK_CAPACITY, false);
            //LOGGER.info("tankA created successfully");
        } catch (Exception e) {
            //LOGGER.error("Failed to create tankA: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize tankA", e);
        }

        try {
            //LOGGER.info("Creating tankB with initial capacity " + INITIAL_TANK_CAPACITY);
            tankB = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.TYPE, entity, INITIAL_TANK_CAPACITY, INITIAL_TANK_CAPACITY, false);
            //LOGGER.info("tankB created successfully");
        } catch (Exception e) {
            //LOGGER.error("Failed to create tankB: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize tankB", e);
        }

        try {
            //LOGGER.info("Creating tankC with initial capacity " + INITIAL_TANK_CAPACITY);
            tankC = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.TYPE, entity, INITIAL_TANK_CAPACITY, INITIAL_TANK_CAPACITY, false);
            //LOGGER.info("tankC created successfully");
        } catch (Exception e) {
            //LOGGER.error("Failed to create tankC: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize tankC", e);
        }

        try {
            //LOGGER.info("Creating fluidHandler LazyOptional");
            fluidHandler = LazyOptional.of(() -> new ControlledCombinedTankWrapper(
                    new IFluidHandler[]{tankA.getPrimaryHandler(), tankB.getPrimaryHandler(), tankC.getPrimaryHandler()}
            ));
            //LOGGER.info("fluidHandler created successfully");
        } catch (Exception e) {
            //LOGGER.error("Failed to create fluidHandler: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize fluidHandler", e);
        }

        //LOGGER.info("FluidTankManager initialization completed for entity at " + entity.getBlockPos());
    }

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        //LOGGER.info("Adding behaviours for FluidTankManager at " + entity.getBlockPos());
        behaviours.add(tankA); // Ajout correct de SmartFluidTankBehaviour
        behaviours.add(tankB);
        behaviours.add(tankC);
    }

    public void onLoad() {
        if (!initialized && entity.getLevel() != null && !entity.getLevel().isClientSide && BioreactorFluids.GAZ_AIR_SOURCE.get() != null) {
            //LOGGER.info("Initializing tanks with GAZ_AIR_SOURCE at " + entity.getBlockPos());
            FluidStack airStack = new FluidStack(BioreactorFluids.GAZ_AIR_SOURCE.get(), TOTAL_CAPACITY);
            fill(airStack, IFluidHandler.FluidAction.EXECUTE);
            initialized = true;
            syncFluidTanks();
            //LOGGER.info("Tanks initialized successfully with " + TOTAL_CAPACITY + " mB of GAZ_AIR_SOURCE");
        } else {
            //LOGGER.warn("Skipping tank initialization: initialized=" + initialized + ", level=" + (entity.getLevel() != null) + ", isClientSide=" + (entity.getLevel() != null && entity.getLevel().isClientSide()) + ", GAZ_AIR_SOURCE=" + (BioreactorFluids.GAZ_AIR_SOURCE.get() != null));
        }
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return LazyOptional.empty();
    }

    public void write(CompoundTag tag, boolean clientPacket) {
        tag.put("TankA", tankA.getPrimaryHandler().writeToNBT(new CompoundTag()));
        tag.put("TankB", tankB.getPrimaryHandler().writeToNBT(new CompoundTag()));
        tag.put("TankC", tankC.getPrimaryHandler().writeToNBT(new CompoundTag()));
        tag.putBoolean("Initialized", initialized);
    }

    public void read(CompoundTag tag, boolean clientPacket) {
        tankA.getPrimaryHandler().readFromNBT(tag.getCompound("TankA"));
        tankB.getPrimaryHandler().readFromNBT(tag.getCompound("TankB"));
        tankC.getPrimaryHandler().readFromNBT(tag.getCompound("TankC"));
        initialized = tag.getBoolean("Initialized");
    }

    private int getTotalFluidAmount() {
        return tankA.getPrimaryHandler().getFluidInTank(0).getAmount() +
                tankB.getPrimaryHandler().getFluidInTank(0).getAmount() +
                tankC.getPrimaryHandler().getFluidInTank(0).getAmount();
    }

    void syncFluidTanks() {
        if (entity.getLevel() != null && !entity.getLevel().isClientSide) {
            entity.setChanged();
            entity.getLevel().sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
        }
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) { // Méthode publique pour le remplissage initial
        return fluidHandler.map(handler -> handler.fill(resource, action)).orElse(0);
    }

    private class ControlledCombinedTankWrapper extends CombinedTankWrapper {
        public ControlledCombinedTankWrapper(IFluidHandler[] tanks) {
            super(tanks);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int totalAmount = getTotalFluidAmount();
            int remainingCapacity = TOTAL_CAPACITY - totalAmount;
            if (remainingCapacity <= 0) {
                //LOGGER.debug("No remaining capacity for fill at " + entity.getBlockPos() + ": total=" + totalAmount);
                return 0;
            }

            int toFill = Math.min(resource.getAmount(), remainingCapacity);
            FluidStack toInsert = resource.copy();
            toInsert.setAmount(toFill);

            //LOGGER.debug("Attempting to fill " + toFill + " mB of " + resource.getFluid().getFluidType().getDescriptionId() + " at " + entity.getBlockPos());

            for (IFluidHandler tank : new IFluidHandler[]{tankA.getPrimaryHandler(), tankB.getPrimaryHandler(), tankC.getPrimaryHandler()}) {
                if (tank.getFluidInTank(0).isEmpty() || tank.getFluidInTank(0).isFluidEqual(resource)) {
                    int filled = tank.fill(toInsert, action);
                    if (filled > 0) {
                        if (action.execute()) syncFluidTanks();
                        //LOGGER.debug("Filled " + filled + " mB into tank at " + entity.getBlockPos());
                        return filled;
                    }
                }
            }
            //LOGGER.debug("No tank accepted the fluid at " + entity.getBlockPos());
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (IFluidHandler tank : new IFluidHandler[]{
                    tankA.getPrimaryHandler(),
                    tankB.getPrimaryHandler(),
                    tankC.getPrimaryHandler()
            }) {
                if (tank.getFluidInTank(0).isFluidEqual(resource)) {
                    FluidStack drained = tank.drain(resource, action);
                    if (!drained.isEmpty()) {
                        if (action.execute()) {
                            syncFluidTanks();
                        }
                        return drained;
                    }
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (IFluidHandler tank : new IFluidHandler[]{
                    tankA.getPrimaryHandler(),
                    tankB.getPrimaryHandler(),
                    tankC.getPrimaryHandler()
            }) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    if (action.execute()) {
                        syncFluidTanks();
                    }
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

    }

    public SmartFluidTankBehaviour getTankA() { return tankA; }
    public SmartFluidTankBehaviour getTankB() { return tankB; }
    public SmartFluidTankBehaviour getTankC() { return tankC; }
}