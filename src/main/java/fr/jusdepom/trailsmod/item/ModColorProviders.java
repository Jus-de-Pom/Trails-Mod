package fr.jusdepom.trailsmod.item;

import fr.jusdepom.trailsmod.item.custom.TrailMapItem;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class ModColorProviders {

    public static void registerColorProviders() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            TrailMapItem map = (TrailMapItem) stack.getItem();
            return map.getColor(stack);
        }, ModItems.TRAIL_MAP);
    }

}
