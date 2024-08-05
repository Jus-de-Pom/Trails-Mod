package fr.jusdepom.trailsmod.utils;

import fr.jusdepom.trailsmod.multiblock.TrailBeacon;
import net.minecraft.block.BlockState;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.function.Predicate;

public final class BeaconsHelper {

    public static TrailBeacon getBeacon(BlockPos pos, World world, Direction direction) {
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

            if (foundBeacon) return beacon;
        }

        return null;
    }

}
