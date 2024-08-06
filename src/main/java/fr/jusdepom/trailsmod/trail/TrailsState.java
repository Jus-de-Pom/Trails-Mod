package fr.jusdepom.trailsmod.trail;

import fr.jusdepom.trailsmod.Trails;
import fr.jusdepom.trailsmod.utils.BlockPosUtils;
import fr.jusdepom.trailsmod.utils.VectorUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TrailsState extends PersistentState {

    public static final String TRAILS_NBT = "trails";
    public static final String TRAIL_NAME_NBT = "name";
    public static final String TRAIL_BEACONS_NBT = "beacons";

    public static final String MAPPED_TRAIL_NBT = "trail";

    private final List<Trail> trails = new LinkedList<>();

    public void addTrail(Trail trail) {
        trails.add(trail);
    }

    public static TrailsState readNbt(NbtCompound nbt) {
        TrailsState state = new TrailsState();
        NbtCompound element = (NbtCompound) nbt.get(TRAILS_NBT);
        if (element == null) {
            nbt.put(TRAILS_NBT, new NbtCompound());
            return state;
        }

        for (String key : element.getKeys()) {
            NbtCompound trailData = (NbtCompound) element.get(key);

            assert trailData != null;
            String name = trailData.getString(TRAIL_NAME_NBT);
            int[] beacons = trailData.getIntArray(TRAIL_BEACONS_NBT);

            List<Vector3i> beaconVectors = VectorUtils.toVectorList(beacons);
            List<BlockPos> beaconPositions = new LinkedList<>();
            beaconVectors.forEach(vector -> beaconPositions.add(VectorUtils.toBlockPos(vector)));

            state.addTrail(new Trail(key, name, beaconPositions));
        }

        return state;
    }

    public void validateTrails(World world) {
        trails.forEach(trail -> trail.validate(world));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound trailsNbt = new NbtCompound();

        for (Trail trail : trails) {
            NbtCompound currentNbt = new NbtCompound();
            currentNbt.putString(TRAIL_NAME_NBT, trail.getName());
            currentNbt.putIntArray(TRAIL_BEACONS_NBT, BlockPosUtils.toIntArray(trail.getBeaconPositions()));

            trailsNbt.put(trail.getId(), currentNbt);
        }

        nbt.put(TRAILS_NBT, trailsNbt);
        return nbt;
    }

    public static @NotNull TrailsState getServerTrailsManager(@NotNull MinecraftServer server) {
        PersistentStateManager persistentStateManager = Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getPersistentStateManager();
        TrailsState state = persistentStateManager.getOrCreate(TrailsState::readNbt, TrailsState::new, Trails.MOD_ID);

        state.markDirty();

        return state;
    }

    public String getDefaultTrailName() {
        return "trail".concat(String.valueOf(trails.size() + 1));
    }

    public boolean trailExists(String name) {
        boolean toReturn = false;

        for (Trail trail : trails) {
            if (trail.getName().equalsIgnoreCase(name)) {
                toReturn = true;
                break;
            }
        }

        return toReturn;
    }

    public void printTrails() {
        trails.forEach(trail -> {
            Trails.LOGGER.info("Trail {} ; id: {}", trail.getName(), trail.getId());
        });
    }
}
