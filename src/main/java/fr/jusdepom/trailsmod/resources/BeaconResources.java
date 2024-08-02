package fr.jusdepom.trailsmod.resources;

import fr.jusdepom.trailsmod.Trails;
import fr.jusdepom.trailsmod.json.BeaconDecoder;
import fr.jusdepom.trailsmod.multiblock.TrailBeacon;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BeaconResources implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return new Identifier(Trails.MOD_ID, "beacons");
    }

    @Override
    public void reload(ResourceManager manager) {
        TrailBeacon.REGISTERED_BEACONS.clear();
        Map<Identifier, Resource> beacons = manager.findResources("beacons", id -> id.getPath().endsWith(".json"));

        for (Resource resource : beacons.values()) {
            try (InputStream resourceStream = resource.getInputStream()) {
                String resourceContent = IOUtils.toString(resourceStream, StandardCharsets.UTF_8);

                BeaconDecoder.decodeBeacon(resourceContent);
            } catch (IOException e) {
                Trails.LOGGER.error("Error getting input stream from resource !");
            }
        }
    }
}
