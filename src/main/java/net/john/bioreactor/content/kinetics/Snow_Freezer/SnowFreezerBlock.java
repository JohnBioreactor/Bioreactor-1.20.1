package net.john.bioreactor.content.kinetics.Snow_Freezer;

//import com.petrolpark.destroy.block.entity.CoolerBlockEntity;
import net.john.bioreactor.BioreactorShapes;

//import com.petrolpark.destroy.block.entity.CoolerBlockEntity.ColdnessLevel;
import net.john.bioreactor.content.kinetics.Snow_Freezer.SnowFreezerBlockEntity.ColdnessLevel;

//import com.petrolpark.destroy.block.entity.DestroyBlockEntityTypes;
import net.john.bioreactor.content.entity.BioreactorBlockEntity;

//import com.petrolpark.destroy.block.shape.DestroyShapes;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// thanks a lot Destroy mod <3
// Based on https://github.com/petrolpark/Destroy/blob/1.20.1/src/main/java/com/petrolpark/destroy/block/CoolerBlock.java
// src/main/java/com/petrolpark/destroy/block/CoolerBlock.java

public class SnowFreezerBlock extends Block implements IBE<SnowFreezerBlockEntity>, IWrenchable {

    public static final EnumProperty<ColdnessLevel> COLD_LEVEL = EnumProperty.create("breeze", ColdnessLevel.class);

    public SnowFreezerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COLD_LEVEL, ColdnessLevel.IDLE)
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.NONE));
    };

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLD_LEVEL, BlazeBurnerBlock.HEAT_LEVEL);
    };

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        withBlockEntityDo(level, pos, be -> be.updateHeatLevel(state.getValue(COLD_LEVEL)));

        if (level.isClientSide()) return;
        BlockEntity blockEntity = level.getBlockEntity(pos.above()); // Check for a Basin
        if (!(blockEntity instanceof BasinBlockEntity)) return;
        BasinBlockEntity basin = (BasinBlockEntity) blockEntity;
        basin.notifyChangeOfContents(); // Let the Basin know there's now a SnowFreezer
    };

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (AllItems.CREATIVE_BLAZE_CAKE.isIn(stack)) {
            withBlockEntityDo(world, pos, snow_freezer -> {
                if (getColdnessLevelOf(state) == ColdnessLevel.FROSTING) {
                    snow_freezer.coolingTicks = 0;
                    snow_freezer.setColdnessOfBlock(ColdnessLevel.IDLE);
                } else  {
                    snow_freezer.coolingTicks = Integer.MAX_VALUE;
                    snow_freezer.setColdnessOfBlock(ColdnessLevel.FROSTING);
                };
            });
            if (!player.isCreative()) stack.shrink(1);
            player.setItemInHand(hand, stack);
            return InteractionResult.sidedSuccess(world.isClientSide());
        };
        return InteractionResult.PASS;
    };

    public static InteractionResultHolder<ItemStack> tryInsert(BlockState state, Level world, BlockPos pos, ItemStack stack, boolean doNotConsume, boolean forceOverflow, boolean simulate) { //this is an override
        return InteractionResultHolder.fail(ItemStack.EMPTY);
    };

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    };

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BioreactorShapes.SNOW_FREEZER;
    };

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context == CollisionContext.empty()) return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
        return getShape(blockState, level, pos, context);
    };

    public static ColdnessLevel getColdnessLevelOf(BlockState blockState) {
        return blockState.getValue(COLD_LEVEL);
    };

    @Override
    public Class<SnowFreezerBlockEntity> getBlockEntityClass() {
        return SnowFreezerBlockEntity.class;
    };

    @Override
    public BlockEntityType<? extends SnowFreezerBlockEntity> getBlockEntityType() {
        return BioreactorBlockEntity.SNOW_FREEZER.get();
    };

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    };

};
