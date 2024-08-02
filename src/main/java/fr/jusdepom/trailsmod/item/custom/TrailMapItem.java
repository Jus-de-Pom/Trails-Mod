package fr.jusdepom.trailsmod.item.custom;

import fr.jusdepom.trailsmod.multiblock.TrailBeacon;
import fr.jusdepom.trailsmod.utils.VectorUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class TrailMapItem extends Item implements DyeableItem {

    public static final String BEACONS_NBT = "trails.beacons";

    private static final Direction[] directions = new Direction[] {
            Direction.WEST,
            Direction.EAST,
            Direction.NORTH,
            Direction.SOUTH
    };

    public TrailMapItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            BlockPos position = context.getBlockPos();

            BlockState state = context.getWorld().getBlockState(position);
            if (state.isOf(Blocks.WATER_CAULDRON)) {
                setColor(context.getStack(), 0xFFFFFF);
                return ActionResult.SUCCESS;
            }

            for (Direction direction : directions) {
                boolean foundBeacon = isBeacon(position, context.getWorld(), direction);
                if (!foundBeacon) continue;

                ItemStack stack = context.getStack();
                assert stack != null;

                NbtCompound compound = stack.getOrCreateNbt();
                Integer[] positionsArray = Arrays.stream(compound.getIntArray(BEACONS_NBT)).boxed().toArray(Integer[]::new);
                List<Integer> positions = new LinkedList<>();
                Collections.addAll(positions, positionsArray);

                List<Vector3i> vectorPositions = VectorUtils.toVectorList(positions);
                for (Vector3i vector : vectorPositions) {
                    if (vector.equals(position.getX(), position.getY(), position.getZ())) return ActionResult.PASS;
                }

                positions.add(position.getX());
                positions.add(position.getY());
                positions.add(position.getZ());

                compound.putIntArray(BEACONS_NBT, positions);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private boolean isBeacon(BlockPos pos, World world, Direction direction) {
        BlockState state = world.getBlockState(pos);

        for (TrailBeacon beacon : TrailBeacon.REGISTERED_BEACONS) {
            Pair<Integer, Integer> startIndices = beacon.getStartIndices();
            Predicate<BlockState>[][] predicates = beacon.getBlockPredicates();
            Predicate<BlockState> startPredicate = predicates[startIndices.getLeft()][startIndices.getRight()];

            if (!startPredicate.test(state)) continue;

            boolean foundBeacon = true;

            for (int i = 0; i < predicates.length; i++) {
                Predicate<BlockState>[] line = predicates[i];

                for (int j = 0; j < line.length; j++) {
                    Predicate<BlockState> currentPredicate = line[j];
                    int horizontalOffset = j - startIndices.getRight();
                    int verticalOffset = startIndices.getLeft() - i;

                    BlockPos currentBlockPos = pos.offset(direction, horizontalOffset).up(verticalOffset);
                    BlockState currentState = world.getBlockState(currentBlockPos);

                    if (currentPredicate == null) continue;

                    if (!currentPredicate.test(currentState)) {
                        foundBeacon = false;
                        break;
                    }
                }

                if (!foundBeacon) break;
            }

            if (foundBeacon) return true;
        }

        return false;
    }

    @Override
    public int getColor(ItemStack stack) {
        if (hasColor(stack)) return DyeableItem.super.getColor(stack);
        return 0xFFFFFF;
    }
}
