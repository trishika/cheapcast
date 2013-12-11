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

package at.maui.cheapcast.chromecast;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import at.maui.cheapcast.chromecast.model.ProtocolMessage;
import at.maui.cheapcast.chromecast.model.cm.CmMessage;
import at.maui.cheapcast.chromecast.model.ramp.RampMessage;
import at.maui.cheapcast.chromecast.model.ramp.RampVolume;
import at.maui.cheapcast.json.deserializer.CmMessageDeserializer;
import at.maui.cheapcast.json.deserializer.ProtocolMessageDeserializer;
import at.maui.cheapcast.json.deserializer.ProtocolMessageSerializer;
import at.maui.cheapcast.json.deserializer.RampMessageDeserializer;
import at.maui.cheapcast.service.CheapCastService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.WebSocket;

import java.io.IOException;
import java.lang.reflect.Type;

public class SessionSocket implements WebSocket, WebSocket.OnTextMessage {

    public static final String LOG_TAG = "SessionSocket";

    protected FrameConnection mFrameConnection;
    protected Connection mConnection;
    protected Gson mGson;
    protected CheapCastService mService;
    protected App mApp;

    protected Context mContext;
    private AudioManager mAudioManager;

    public SessionSocket(CheapCastService service, App app) {
        mService = service;
        mApp = app;
        mContext = service;
        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

        mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ProtocolMessage.class, new ProtocolMessageDeserializer())
                .registerTypeAdapter(ProtocolMessage.class, new ProtocolMessageSerializer())
                .registerTypeAdapter(RampMessage.class, new RampMessageDeserializer())
                .registerTypeAdapter(CmMessage.class, new CmMessageDeserializer())
                .create();
    }

    public FrameConnection getConnection()
    {
        return mFrameConnection;
    }

    public void close() {
        if(mConnection != null)
            mConnection.close();
    }

    @Override
    public void onMessage(String s) {
        Log.i(LOG_TAG, "<<" + s);

        if(s.contains("ping")) {
            try {
                send("[\"cm\",{\"type\":\"pong\"}]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            ProtocolMessage pm = mGson.fromJson(s, ProtocolMessage.class);
            if(pm != null && pm.getProtocol().equals("ramp")) {
                if(pm.getPayload() instanceof RampVolume) {
                    RampVolume vol = (RampVolume) pm.getPayload();
                    Log.i(LOG_TAG, "Setting volume");
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(vol.getVolume() * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),0);
                }
            }
            ReceiverSocket receiver = mApp.getReceiver();
            if(receiver == null)
                mApp.getMessageBuffer().push(s);
            else {
                try {
                    receiver.send(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendCM(CmMessage msg, Type msgType) throws IOException
    {
        ProtocolMessage pm = new ProtocolMessage();
        pm.setProtocol("cm");
        pm.setPayload(msg);

        send(mGson.toJson(pm, ProtocolMessage.class).toString());
    }

    public void sendRamp(RampMessage msg, Type msgType) throws IOException
    {
        ProtocolMessage pm = new ProtocolMessage();
        pm.setProtocol("ramp");
        pm.setPayload(msg);

        send(mGson.toJson(pm, ProtocolMessage.class).toString());
    }

    public void send(String s) throws IOException {
        if(mConnection != null) {
            mConnection.sendMessage(s);
            if(!s.contains("ping") && !s.contains("pong"))
                Log.i(LOG_TAG, ">>" + s);
        } else {
            Log.d(LOG_TAG, "Could not send, already closed.");
        }
    }

    @Override
    public void onOpen(Connection connection) {
        connection.setMaxTextMessageSize(64*1024);
        connection.setMaxBinaryMessageSize(64 * 1024);
        connection.setMaxIdleTime(Integer.MAX_VALUE);
        mConnection = connection;
        mApp.addRemote(this);
        Log.d(LOG_TAG, "onOpen();");
    }

    @Override
    public void onClose(int i, String s) {
        Log.d(LOG_TAG, String.format("onClose(%d, %s)",i,s));
        mConnection = null;
        mApp.removeRemote(this);
    }
}