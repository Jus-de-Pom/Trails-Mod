package fr.jusdepom.trailsmod.utils;

import net.minecraft.util.math.BlockPos;
import org.joml.Vector3i;

import java.util.LinkedList;
import java.util.List;

public class VectorUtils {

    public static List<Vector3i> toVectorList(List<Integer> integers) {
        List<Vector3i> vectors = new LinkedList<>();

        for (int i = 0; i < integers.size(); i += 3) {
            vectors.add(new Vector3i(integers.get(i), integers.get(i + 1), integers.get(i + 2)));
        }

        return vectors;
    }

    public static List<Vector3i> toVectorList(int[] integers) {
        List<Vector3i> vectors = new LinkedList<>();
        if (integers.length < 3) return vectors;

        for (int i = 0; i < integers.length - 2; i += 3) {
            vectors.add(new Vector3i(integers[i], integers[i + 1], integers[i + 2]));
        }

        return vectors;
    }

    public static BlockPos toBlockPos(Vector3i vector) {
        return new BlockPos(vector.x, vector.y, vector.z);
    }

    public static List<BlockPos> toBlockPos(List<Vector3i> vectors) {
        List<BlockPos> toReturn = new LinkedList<>();
        vectors.forEach(vector -> toReturn.add(toBlockPos(vector)));

        return toReturn;
    }

}
