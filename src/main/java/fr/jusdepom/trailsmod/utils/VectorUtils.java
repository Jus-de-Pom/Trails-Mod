package fr.jusdepom.trailsmod.utils;

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

}
