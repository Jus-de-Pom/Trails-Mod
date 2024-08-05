package fr.jusdepom.trailsmod.item.custom;

import fr.jusdepom.trailsmod.multiblock.TrailBeacon;
import fr.jusdepom.trailsmod.utils.BeaconsHelper;
import fr.jusdepom.trailsmod.utils.VectorUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
                context.getPlayer().playSound(SoundEvents.ITEM_BUCKET_FILL, SoundCategory.MASTER, 1f, 1f);
                return ActionResult.SUCCESS;
            }

            for (Direction direction : directions) {
                TrailBeacon beacon = BeaconsHelper.getBeacon(position, context.getWorld(), direction);
                if (beacon == null) continue;

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
                context.getPlayer().playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.MASTER, 1f, 1f);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public int getColor(ItemStack stack) {
        if (hasColor(stack)) return DyeableItem.super.getColor(stack);
        return 0xFFFFFF;
    }
}
