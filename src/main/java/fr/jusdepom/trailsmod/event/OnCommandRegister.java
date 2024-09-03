package fr.jusdepom.trailsmod.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.jusdepom.trailsmod.trail.Trail;
import fr.jusdepom.trailsmod.trail.TrailsState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.*;

public class OnCommandRegister implements CommandRegistrationCallback {

    public static final CommandRegistrationCallback LISTENER = new OnCommandRegister();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("trails")
                .executes(this::trailsCommand));
    }

    private int trailsCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        List<Trail> trails = TrailsState.getServerTrailsManager(source.getServer()).getTrails();

        source.sendFeedback(() -> Text.literal(String.format("There are %s trails", trails.size())), false);
        StringBuilder builder = new StringBuilder();

        for (Trail trail : trails) {
            builder.append(String.format("%s (%s) ", trail.getName(), trail.getId()));
        }

        source.sendFeedback(() -> Text.literal(builder.toString()), false);

        return 0;
    }

}
