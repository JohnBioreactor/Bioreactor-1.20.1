package net.john.bioreactor.content.kinetics.Anaerobic_Chamber;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import com.simibubi.create.foundation.block.IBE;
import net.john.bioreactor.content.entity.BioreactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnaerobicChamberBlock extends Block implements IBE<AnaerobicChamberBlockEntity>, IWrenchable {

    public AnaerobicChamberBlock(Properties properties) {
        super(properties
                .noOcclusion()
                .sound(SoundType.GLASS)
                .isViewBlocking((state, reader, pos)-> false));
    }


    // visual stuff //
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(box(0, 0, 0, 2, 18, 2), box(14, 0, 0, 16, 18, 2), box(0, 0, 14, 2, 18, 16), box(14, 0, 14, 16, 18, 16), box(0, 0, 2, 2, 18, 14), box(2, 0, 14, 14, 18, 16), box(14, 0, 2, 16, 18, 14), box(2, 0, 0, 14, 18, 2));
    }



    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }




        // Block entity setup through IBE interface
    @Override
    public Class<AnaerobicChamberBlockEntity> getBlockEntityClass() {
        return AnaerobicChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AnaerobicChamberBlockEntity> getBlockEntityType() {
        return BioreactorBlockEntity.ANAEROBIC_CHAMBER.get();
    }

    // Prevent pathfinding through this block
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }


//    @Override
//    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
//        IBE.onRemove(state, worldIn, pos, newState);
//    };


    // Remove the block, get it back if not in creative mod. Do not forget to add "IWrenchable" in class creation (above)
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {

        IBE.onRemove(state, worldIn, pos, newState);  // Call IBE.onRemove to handle block entity removal and cleanup

        if (!state.is(newState.getBlock())) { // Check if the block is actually being removed, not just replaced
            // Retrieve the nearest player to check game mode
            Player player = worldIn.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5.0, false);

            if (player == null || !player.isCreative()) { // Only drop the block if the player is not in Creative mode
                worldIn.addFreshEntity(new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this)));
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving); // Call the superclass method to ensure proper cleanup
    }





    // Add hover text for the block item in the player’s inventory
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
//        tooltip.add(Component.translatable("tooltip.bioreactor.liquid_container").withStyle(ChatFormatting.GOLD));
//    }





}
