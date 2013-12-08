package at.maui.cheapcast.json.deserializer;

import at.maui.cheapcast.chromecast.model.ProtocolMessage;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ProtocolMessageSerializer implements JsonSerializer<ProtocolMessage> {

    @Override
    public JsonElement serialize(ProtocolMessage protocolMessage, Type type, JsonSerializationContext jsonSerializationContext)
    {
        JsonArray res = new JsonArray();
        res.add(new JsonPrimitive(protocolMessage.getProtocol()));
        res.add(jsonSerializationContext.serialize(protocolMessage.getPayload(), protocolMessage.getPayload().getClass()));
        return res;
    }
}
