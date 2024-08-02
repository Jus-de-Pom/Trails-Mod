package fr.jusdepom.trailsmod.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.jusdepom.trailsmod.multiblock.TrailBeacon;

public class BeaconDecoder {

    public static void decodeBeacon(String jsonContent) {
        Gson gson = new Gson();
        TrailBeaconJson json = gson.fromJson(jsonContent, TrailBeaconJson.class);
        TrailBeacon newBeacon = new TrailBeacon(json);
        TrailBeacon.REGISTERED_BEACONS.add(newBeacon);
    }

    public static class TrailBeaconJson {
        @SuppressWarnings("unused")
        private int level;

        @SuppressWarnings("unused")
        private String[] pattern;

        @SuppressWarnings("unused")
        private JsonObject blocks;

        @Override
        public String toString() {
            return this.blocks.toString();
        }

        public int getLevel() {
            return this.level;
        }

        public String[] getPattern() {
            return pattern;
        }

        public JsonObject getBlocks() {
            return blocks;
        }
    }

}
