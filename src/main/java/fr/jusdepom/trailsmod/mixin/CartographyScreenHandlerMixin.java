package fr.jusdepom.trailsmod.mixin;

import fr.jusdepom.trailsmod.item.ModItems;
import fr.jusdepom.trailsmod.item.custom.TrailMapItem;
import fr.jusdepom.trailsmod.trail.Trail;
import fr.jusdepom.trailsmod.trail.TrailsState;
import fr.jusdepom.trailsmod.utils.VectorUtils;
import net.minecraft.block.MapColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(CartographyTableScreenHandler.class)
public abstract class CartographyScreenHandlerMixin extends ScreenHandler {
    @Shadow @Final public Inventory inventory;
    @Shadow @Final private ScreenHandlerContext context;
    @Shadow @Final private CraftingResultInventory resultInventory;
    @Shadow long lastTakeResultTime;

    protected CartographyScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/CartographyTableScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
            ordinal = 0
    ))
    private @Nullable Slot trailMapSlot(CartographyTableScreenHandler instance, Slot slot) {
        return addSlot(new Slot(inventory, 0, 15, 15) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.FILLED_MAP) || stack.isOf(ModItems.TRAIL_MAP);
            }
        });
    }

    @Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/CartographyTableScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
            ordinal = 1
    ))
    private @Nullable Slot filledMapSlot(CartographyTableScreenHandler instance, Slot slot) {
        return this.addSlot(new Slot(this.inventory, 1, 15, 52) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.PAPER) || stack.isOf(Items.MAP) || stack.isOf(Items.GLASS_PANE) || stack.isOf(Items.FILLED_MAP);
            }
        });
    }

    @Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/CartographyTableScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
            ordinal = 2
    ))
    private @Nullable Slot resultSlot(CartographyTableScreenHandler instance, Slot slot) {
        return addSlot(new Slot(this.resultInventory, 2, 145, 39) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                if (!slots.get(0).getStack().isOf(ModItems.TRAIL_MAP)) slots.get(0).takeStack(1);
                slots.get(1).takeStack(1);
                stack.getItem().onCraft(stack, player.getWorld(), player);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (lastTakeResultTime != l) {
                        world.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void registerRecipe(ItemStack map, ItemStack item, ItemStack oldResult, CallbackInfo ci) {
        AtomicBoolean valid = new AtomicBoolean(false);

        context.run((world, blockPos) -> {
            if (world.isClient()) return;
            if (!map.isOf(ModItems.TRAIL_MAP) && !item.isOf(Items.FILLED_MAP)) return;

            MapState mapState = FilledMapItem.getMapState(item, world);
            if (mapState == null) return;

            ItemStack result = item.copyWithCount(1);
            NbtCompound nbt = map.getNbt();
            if (nbt == null) return;

            Integer[] positionsInt = Arrays.stream(nbt.getIntArray(TrailMapItem.BEACONS_NBT)).boxed().toArray(Integer[]::new);
            List<Integer> positionsIntList = List.of(positionsInt);
            List<Vector3i> beaconPositions = VectorUtils.toVectorList(positionsIntList);

            int blocksPerPixel = (int) Math.pow(2, mapState.scale);
            int mapSize = 128 * blocksPerPixel;

            result.getOrCreateNbt().putBoolean("map_to_lock", true);
            this.sendContentUpdates();

            TrailMapItem mapItem = (TrailMapItem) map.getItem();
            String targetColor = Integer.toHexString(mapItem.getColor(map));
            int targetRed = Integer.parseInt(targetColor.substring(0, 1), 16);
            int targetGreen = Integer.parseInt(targetColor.substring(2, 3), 16);
            int targetBlue = Integer.parseInt(targetColor.substring(4, 5), 16);

            MapColor mapColor = MapColor.CLEAR;
            float deltaMin = 999999;

            for (int i = 0; i < 64; i++) {
                MapColor currentColor = MapColor.get(i);
                String hexColor = Integer.toHexString(currentColor.color);
                hexColor = "0".repeat(6 - hexColor.length()) + hexColor;

                int red = Integer.parseInt(hexColor.substring(0, 1), 16);
                int green = Integer.parseInt(hexColor.substring(2, 3), 16);
                int blue = Integer.parseInt(hexColor.substring(4, 5), 16);

                Vector3f deltaVector = new Vector3f(targetRed - red, targetGreen - green, targetBlue - blue);
                float delta = deltaVector.length();

                if (delta < deltaMin) {
                    deltaMin = delta;
                    mapColor = currentColor;
                }
            }

            boolean mappedBeacons = false;
            for (Vector3i pos : beaconPositions) {
                int xOffset = pos.x - mapState.centerX;
                int zOffset = pos.z - mapState.centerZ;

                if (Math.abs(xOffset) > mapSize / 2 || Math.abs(zOffset) > mapSize / 2) continue;

                int x = xOffset + mapSize / 2; x /= blocksPerPixel;
                int z = zOffset + mapSize / 2; z /= blocksPerPixel;

                mapState.putColor(x, z, mapColor.getRenderColorByte(MapColor.Brightness.HIGH));

                if (Math.abs(x + 1) < 128) mapState.putColor(x + 1, z, mapColor.getRenderColorByte(MapColor.Brightness.NORMAL));
                if (Math.abs(x - 1) < 128) mapState.putColor(x - 1, z, mapColor.getRenderColorByte(MapColor.Brightness.NORMAL));
                if (Math.abs(z + 1) < 128) mapState.putColor(x, z + 1, mapColor.getRenderColorByte(MapColor.Brightness.NORMAL));
                if (Math.abs(z - 1) < 128) mapState.putColor(x, z - 1, mapColor.getRenderColorByte(MapColor.Brightness.NORMAL));

                mappedBeacons = true;
            }

            if (mappedBeacons) {
                assert world.getServer() != null;
                TrailsState state = TrailsState.getServerTrailsManager(world.getServer());

                String name;
                if (map.hasCustomName() && !state.trailExists(map.getName().getString())) name = map.getName().getString();
                else return;

                String id = name.toLowerCase().replaceAll(" ", "_");
                List<BlockPos> beaconBlockPositions = VectorUtils.toBlockPos(beaconPositions);
                Trail trail = new Trail(id, name, beaconBlockPositions);
                trail.validate(world);

                state.addTrail(trail);

                result.getOrCreateNbt().putString(TrailsState.MAPPED_TRAIL_NBT, id);
            }

            this.sendContentUpdates();

            if (!ItemStack.areEqual(result, oldResult)) {
                this.resultInventory.setStack(2, result);
                this.sendContentUpdates();
            }

            valid.set(true);
        });

        if (!valid.get()) return;
        ci.cancel();
    }
}
