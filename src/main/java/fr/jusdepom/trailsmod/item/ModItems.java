package fr.jusdepom.trailsmod.item;

import fr.jusdepom.trailsmod.Trails;
import fr.jusdepom.trailsmod.item.custom.TrailMapItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item TRAIL_MAP = registerItem("trail_map", new TrailMapItem(
            new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)
    ));

    private static void groupToolsItems(FabricItemGroupEntries entries) {
        entries.add(TRAIL_MAP);
    }

    private static Item registerItem(String id, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Trails.MOD_ID, id), item);
    }

    public static void registerItems() {
        Trails.LOGGER.info("Registering items for the Trails mod");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::groupToolsItems);
    }

}
