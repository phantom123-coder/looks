package com.looksmaxxing;

import com.looksmaxxing.data.PlayerStats;
import com.looksmaxxing.net.Net;
import com.looksmaxxing.net.OpenMirrorPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

/** Full-length mirror: right-click = edit your look, sneak + right-click = posture practice. */
public class MirrorBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape N = Block.box(1, 0, 14, 15, 16, 16);
    private static final VoxelShape S = Block.box(1, 0, 0, 15, 16, 2);
    private static final VoxelShape E = Block.box(0, 0, 1, 2, 16, 15);
    private static final VoxelShape W = Block.box(14, 0, 1, 16, 16, 15);

    public MirrorBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> S;
            case EAST -> E;
            case WEST -> W;
            default -> N;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer sp) {
            PlayerStats s = ServerEvents.get(sp);
            if (s == null || !s.created) {
                sp.displayClientMessage(Component.literal("Create your character first."), true);
                return InteractionResult.CONSUME;
            }
            if (sp.isShiftKeyDown()) {
                Grooming.posturePractice(sp, hand);
            } else {
                s.mirrorUntil = level.getGameTime() + 1200; // 1 minute to finish editing
                Net.CH.send(PacketDistributor.PLAYER.with(() -> sp), new OpenMirrorPacket());
            }
        }
        return InteractionResult.CONSUME;
    }
}
