package fr.jusdepom.trailsmod.trail;

import fr.jusdepom.trailsmod.utils.BeaconsHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class Trail {

    private final String id;
    private final String name;
    private final List<BlockPos> beaconPositions;
    private final List<TrailBeaconObject> beacons;

    public Trail(String id, String name, List<BlockPos> beaconPositions) {
        this.id = id;
        this.name = name;
        this.beaconPositions = beaconPositions;
        this.beacons = new LinkedList<>();
    }

    public void validate(World world) {
        beaconPositions.forEach(pos -> beacons.add(TrailBeaconObject.getInstance(pos, world)));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<BlockPos> getBeaconPositions() {
        return beaconPositions;
    }

    public List<TrailBeaconObject> getBeacons() {
        return beacons;
    }
}
