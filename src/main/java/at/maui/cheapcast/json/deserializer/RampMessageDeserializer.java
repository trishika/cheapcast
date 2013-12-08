/*
 * Copyright 2013 Sebastian Mauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.maui.cheapcast.json.deserializer;

import android.util.Log;
import at.maui.cheapcast.chromecast.model.ramp.*;
import com.google.gson.*;

import java.lang.reflect.Type;

public class RampMessageDeserializer implements JsonDeserializer<RampMessage> {

    public static final String LOG_TAG = "RampMessageDeserializer";

    @Override
    public RampMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String t = obj.getAsJsonPrimitive("type").getAsString();

            if(t.equals("VOLUME")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampVolume.class);
            } else if(t.equals("STATUS") || t.equals("RESPONSE")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampStatus.class);
            } else if(t.equals("PLAY")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampPlay.class);
            } else if(t.equals("STOP")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampStop.class);
            } else if(t.equals("LOAD")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampLoad.class);
            } else if(t.equals("INFO")) {
                return jsonDeserializationContext.deserialize(jsonElement, RampInfo.class);
            }
            Log.w(LOG_TAG, "Ramp message not handle : " + t);
        }

        return null;
    }
}
