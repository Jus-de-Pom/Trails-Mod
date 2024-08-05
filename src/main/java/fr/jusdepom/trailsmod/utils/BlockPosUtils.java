package fr.jusdepom.trailsmod.utils;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlockPosUtils {

    public static int[] toIntArray(List<BlockPos> positions) {
        int[] toReturn = new int[positions.size() * 3];

        for (int i = 0; i < positions.size(); i++) {
            BlockPos current = positions.get(i);

            toReturn[3 * i] = current.getX();
            toReturn[3 * i + 1] = current.getY();
            toReturn[3 * i + 2] = current.getZ();
        }

        return toReturn;
    }

}
