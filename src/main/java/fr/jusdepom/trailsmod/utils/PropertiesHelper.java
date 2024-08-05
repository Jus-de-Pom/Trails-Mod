package fr.jusdepom.trailsmod.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fr.jusdepom.trailsmod.Trails;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PropertiesHelper {

    public static List<Property<?>> getProperties(Collection<String> keys) {
        List<Property<?>> toReturn = new LinkedList<>();
        Field[] properties = Properties.class.getDeclaredFields();

        for (Field field : properties) {
            try {
                Object obj = field.get(null);
                if (obj instanceof Property<?> property) toReturn.add(property);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return toReturn;
    }

    public static boolean checkProperty(BlockState state, Property<?> property, Object value) {
        if (!state.getProperties().contains(property)) return true;

        if (property instanceof EnumProperty<?> enumProperty && value instanceof String string) {
            return state.get(enumProperty).asString().equalsIgnoreCase(string);
        } else {
            return state.get(property) == value;
        }
    }

    public static boolean checkPropertyJson(BlockState state, Property<?> property, JsonElement value) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();

        if (primitive.isBoolean()) return checkProperty(state, property, value.getAsBoolean());
        else if (primitive.isNumber()) return checkProperty(state, property, value.getAsInt());
        else if (primitive.isString()) return checkProperty(state, property, value.getAsString());
        else return true;
    }

}
