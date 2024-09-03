package fr.jusdepom.trailsmod;

import fr.jusdepom.trailsmod.event.OnCommandRegister;
import fr.jusdepom.trailsmod.item.ModItems;
import fr.jusdepom.trailsmod.resources.BeaconResources;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trails implements ModInitializer {
	public static final String MOD_ID = "trails";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerItems();

		CommandRegistrationCallback.EVENT.register(OnCommandRegister.LISTENER);

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new BeaconResources());
	}
}