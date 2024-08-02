package fr.jusdepom.trailsmod;

import fr.jusdepom.trailsmod.item.ModColorProviders;
import net.fabricmc.api.ClientModInitializer;

public class TrailsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Trails.LOGGER.info("Initializing client !");

        ModColorProviders.registerColorProviders();
    }

}
