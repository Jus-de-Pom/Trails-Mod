package fr.jusdepom.trailsmod.multiblock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.jusdepom.trailsmod.json.BeaconDecoder;
import fr.jusdepom.trailsmod.utils.PropertiesHelper;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class TrailBeacon {
    public static final  List<TrailBeacon> REGISTERED_BEACONS = new LinkedList<>();

    private final int level;
    private final Predicate<BlockState>[][] blockPredicates;

    public TrailBeacon(BeaconDecoder.TrailBeaconJson json) {
        this.level = json.getLevel();
        this.blockPredicates = decodePredicates(json.getPattern(), json.getBlocks());
    }

    private Predicate<BlockState>[][] decodePredicates(String[] pattern, JsonObject blockReplacements) {
        @SuppressWarnings("unchecked")
        Predicate<BlockState>[][] toReturn = (Predicate<BlockState>[][]) new Predicate[pattern.length][pattern[0].length()];

        for (int i = 0; i < pattern.length; i++) {
            String line = pattern[i];

            for (int j = 0; j < line.length(); j++) {
                char currentChar = line.charAt(j);

                JsonObject correspondingBlock = (JsonObject) blockReplacements.get(String.valueOf(currentChar));

                Predicate<BlockState> blockPredicate = state -> true;
                Predicate<BlockState> tagPredicate = state -> true;
                Predicate<BlockState> statePredicate = state -> true;

                if (correspondingBlock == null) continue;

                if (correspondingBlock.has("block")) {
                    String blockId = correspondingBlock.get("block").getAsString();
                    blockPredicate = state -> state.isOf(Registries.BLOCK.get(new Identifier(blockId)));
                }

                if (correspondingBlock.has("tag")) {
                    String tagId = correspondingBlock.get("tag").getAsString();
                    tagPredicate = state -> state.isIn(TagKey.of(RegistryKeys.BLOCK, new Identifier(tagId)));
                }

                if (correspondingBlock.has("states")) {
                    Map<String, JsonElement> states = correspondingBlock.get("states").getAsJsonObject().asMap();
                    List<Property<?>> properties = PropertiesHelper.getProperties(states.keySet());

                    for (Property<?> property : properties) {
                        JsonElement value = states.get(property.getName());
                        if (value == null) continue;
                        statePredicate = statePredicate.and(state -> PropertiesHelper.checkPropertyJson(state, property, value));
                    }
                }

                Predicate<BlockState> finalTagPredicate = tagPredicate;
                Predicate<BlockState> finalBlockPredicate = blockPredicate;
                Predicate<BlockState> finalStatePredicate = statePredicate;
                Predicate<BlockState> finalPredicate = state -> finalTagPredicate.test(state) && finalBlockPredicate.test(state) && finalStatePredicate.test(state);

                toReturn[i][j] = finalPredicate;
            }
        }

        return toReturn;
    }

    public Pair<Integer, Integer> getStartIndices() {
        Predicate<BlockState>[] lastLine = blockPredicates[blockPredicates.length - 1];
        int index = lastLine.length == 1 ? 0 : (int) Math.ceil((double) lastLine.length / 2 - 1);

        return new Pair<>(blockPredicates.length - 1, index);
    }

    public Predicate<BlockState>[][] getBlockPredicates() {
        return blockPredicates;
    }

    public int getLevel() {
        return level;
    }
}
