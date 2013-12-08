package at.maui.cheapcast.json.deserializer;

import android.util.Log;
import at.maui.cheapcast.chromecast.model.cm.CmMessage;
import at.maui.cheapcast.chromecast.model.cm.CmPing;
import at.maui.cheapcast.chromecast.model.cm.CmPong;
import com.google.gson.*;

import java.lang.reflect.Type;

public class CmMessageDeserializer  implements JsonDeserializer<CmMessage> {

    public static final String LOG_TAG = "CmMessageDeserializer";

    @Override
    public CmMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String t = obj.getAsJsonPrimitive("type").getAsString();

            if(t.equals("ping")) {
                return jsonDeserializationContext.deserialize(jsonElement, CmPing.class);
            } else if(t.equals("pong")) {
                return jsonDeserializationContext.deserialize(jsonElement, CmPong.class);
            }
            Log.w(LOG_TAG, "CM message not handle : " + t);
        }

        return null;
    }
}
