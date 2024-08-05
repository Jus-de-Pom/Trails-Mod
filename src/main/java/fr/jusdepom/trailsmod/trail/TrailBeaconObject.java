package fr.jusdepom.trailsmod.trail;

import fr.jusdepom.trailsmod.multiblock.TrailBeacon;
import fr.jusdepom.trailsmod.utils.BeaconsHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record TrailBeaconObject(TrailBeacon beacon, BlockPos position, Direction direction) {

    private static Direction[] DIRECTIONS = new Direction[] {
            Direction.WEST,
            Direction.EAST,
            Direction.NORTH,
            Direction.SOUTH,
    };

    public boolean isValid(World world) {
        return BeaconsHelper.getBeacon(position, world, direction) != null;
    }

    public static @Nullable TrailBeaconObject getInstance(BlockPos pos, World world) {
        for (Direction dir : DIRECTIONS) {
            TrailBeacon trailBeacon = BeaconsHelper.getBeacon(pos, world, dir);
            if (trailBeacon != null) return new TrailBeaconObject(trailBeacon, pos, dir);
        }

        return null;
    }

}
