package local.flavor_immersed_daily_spoofchicken;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class ChickenBlock extends Block {
    public static final MapCodec<ChickenBlock> CODEC = simpleCodec(ChickenBlock::new);
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 2);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0.5, 0, 0.5, 15.5, 6.5, 15.5);

    public ChickenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BITES, 0).setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<ChickenBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BITES, FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected boolean isPathfindable(BlockState state, PathComputationType type) { return false; }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Also allow slabs, counters and other non-air supporting furniture.
        return !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        eat(state, level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        eat(state, level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    private void eat(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide || player.isSpectator() || !player.mayBuild()) return;
        // Reject interactions that were queued for a previous bite or an already removed plate.
        if (!state.is(this) || !level.getBlockState(pos).equals(state)) return;
        // Always edible for visual testing, including when hunger is full.
        player.getFoodData().eat(4, 0.4F);
        level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 1.0F);
        level.gameEvent(player, GameEvent.EAT, pos);
        int bites = state.getValue(BITES);
        // Bite changes only need client synchronization; the shape and support do not change.
        if (bites < 2) level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_CLIENTS);
        else if (level.removeBlock(pos, false)) {
            ItemEntity bowl = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.2,
                pos.getZ() + 0.5, new ItemStack(Items.BOWL));
            bowl.setDefaultPickUpDelay();
            level.addFreshEntity(bowl);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
    }
}
