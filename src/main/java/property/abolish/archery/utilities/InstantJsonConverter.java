package property.abolish.archery.utilities;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantJsonConverter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return jsonElement.isJsonNull() ? null : Instant.ofEpochMilli(jsonElement.getAsLong());
    }

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext ctx) {
        return instant == null ? JsonNull.INSTANCE : new JsonPrimitive(instant.toEpochMilli());
    }
}
