package at.maui.cheapcast.chromecast;

import android.util.Log;
import at.maui.cheapcast.chromecast.model.LocationFile;
import at.maui.cheapcast.chromecast.model.ProtocolMessage;
import at.maui.cheapcast.chromecast.model.State;
import at.maui.cheapcast.chromecast.model.cm.CmPing;
import at.maui.cheapcast.chromecast.model.cm.CmPong;
import at.maui.cheapcast.chromecast.model.ramp.*;
import at.maui.cheapcast.service.CheapCastService;
import fi.iki.elonen.BufferServer;
import fi.iki.elonen.ServerRunner;
import org.droidupnp.model.upnp.ARendererState;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.UPnPState;
import org.droidupnp.model.upnp.didl.SimpleDIDLItem;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class UPnPSessionSocket extends SessionSocket implements Observer {

    public static final String LOG_TAG = "UPnPSessionSocket";

    private int eventSequence = 0;
    private State state = State.IDLE;

    private Pinger pinger;

    private IRendererCommand mRendererCommand;

    private BufferServer bfServer;

    @Override
    public void update(Observable observable, Object o)
    {
        // UPnP device status update

        if(observable instanceof ARendererState)
        {
            ARendererState rendererState = (ARendererState) observable;
            Log.i(LOG_TAG, "Renderer state has changed");

            RampStatus status = new RampStatus(eventSequence, State.IDLE);
            status.setStatusType();

            if(rendererState.getState() == UPnPState.PLAY)
                status.getStatus().setState(State.PLAYING.getValue());
            else if(rendererState.getState() == UPnPState.PAUSE)
                status.getStatus().setState(State.STOPPED.getValue());

            status.getStatus().setTitle(rendererState.getTitle());
            status.getStatus().setDuration(rendererState.getDurationSeconds());
            status.getStatus().setMuted(false);
            status.getStatus().setTimeProgress(true);

        }
    }

    private class Pinger extends Thread {

        public void run(){
            try {
                try {
                    while(true)
                    {
                        sendCM(new CmPing(), CmPing.class);
                        sleep(2000);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to send ping");
                    e.printStackTrace();
                }
            } catch (InterruptedException ie) {
                Log.i(LOG_TAG, "End pinger");
            }
        }
    }

    private class MediaDownloader extends Thread {

        private String mURL;
        private String mAuthorization;

        public MediaDownloader(String url, String authorization)
        {
            mURL = url;
            mAuthorization = authorization;
        }

        public void run(){
            try
            {
                String location = null;
                {
                    String uri = mURL.replace("android.clients", "jmt17") + "&output=json";
                    Log.i(LOG_TAG, "URI : " + uri);
                    URL url = new URL(uri);
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    try {
                        conn.setRequestProperty("authorization", mAuthorization);
                        conn.setRequestProperty("referer", "https://jmt17.google.com/sjdev/cast/player");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (CrKey armv7l 1.3.14651) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.0 Safari/537.36");

                        Log.i(LOG_TAG, "Will download file !!!");

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String json_url = "", line;
                        while ((line = in.readLine()) != null)
                            json_url += line;
                        in.close();

                        Log.i(LOG_TAG, "Json file content : " + json_url);

                        LocationFile locationFile = mGson.fromJson(json_url, LocationFile.class);
                        location = locationFile.getLocation();

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        Log.e(LOG_TAG, "Code : " + conn.getResponseCode() + ", Message : " + conn.getResponseMessage());
                    }
                    finally {
                        conn.disconnect();
                    }
                    Log.i(LOG_TAG, "File downloaded !!!");
                }

//                bfServer.setUri(location);
//                String ip = Utils.getLocalV4Address(Utils.getActiveNetworkInterface()).getHostAddress();
//                mRendererCommand.launchItem(new SimpleDIDLItem("http://"+ip+":"+(START_PORT_MEDIA+id)+"/audio.mp3"));

                mRendererCommand.launchItem(new SimpleDIDLItem(location));

//                mCheapCastService.setUpnpURL(mUPnPID, url);

//                {
//                    String uri = location.replace("android.clients", "jmt17") + "&output=json";
//                    Log.i(LOG_TAG, "URI : " + uri);
//                    URL url = new URL(uri);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    try {
//                        conn.setRequestProperty("referer", "https://jmt17.google.com/sjdev/cast/player");
//                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (CrKey armv7l 1.3.14651) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.0 Safari/537.36");
//
//                        Log.i(LOG_TAG, "Will download file !!!");
//
//                        int count;
//                        byte data[] = new byte[1024];
//                        OutputStream output = new FileOutputStream("/sdcard/test.mp3");
//                        InputStream reader = conn.getInputStream();
//                        while ((count = reader.read(data)) != -1) {
//                            output.write(data, 0, count);
//                        }
//                        reader.close();
//
//                    } catch (IOException ioe) {
//                        ioe.printStackTrace();
//                        Log.e(LOG_TAG, "Code : " + conn.getResponseCode() + ", Message : " + conn.getResponseMessage());
//                    }
//                    finally {
//                        conn.disconnect();
//                    }
//                    Log.i(LOG_TAG, "File downloaded !!!");
//                }
            } catch (MalformedURLException me) {
                Log.e(LOG_TAG, "Invalid URL");
                me.printStackTrace();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Download failed");
                ioe.printStackTrace();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unexpected exception");
                e.printStackTrace();
            }
        }
    }

    public UPnPSessionSocket(CheapCastService service, App app, IRendererCommand rendererCommand)
    {
        super(service, app);
        pinger = new Pinger();
        pinger.start();

        bfServer = new BufferServer("0.0.0.0", 10000);
        ServerRunner.executeInstance(bfServer);

        mRendererCommand = rendererCommand;
        mRendererCommand.getRendererState().addObserver(this);
    }

    @Override
    public void onMessage(String s)
    {
        if(!s.contains("ping") && !s.contains("pong"))
            Log.i(LOG_TAG, "<<" + s);

        ProtocolMessage pm = mGson.fromJson(s, ProtocolMessage.class);
        if(pm == null)
        {
            Log.e(LOG_TAG, "Not a valid mesage ! " + s);
            return;
        }

        try {
            // TODO Make all this with a visitor design pattern instead

            if(pm.getProtocol().equals("cm"))
            {
                // Control message

                if(pm.getPayload() instanceof CmPing)
                {
                    Log.v(LOG_TAG, "Received ping, send a pong response");
                    sendCM(new CmPong(), CmPong.class);
                }
                else if(pm.getPayload() instanceof CmPong)
                {
                    Log.v(LOG_TAG, "Received pong");
                    // TODO Validate previous ping...
                }
                else
                {
                    Log.e(LOG_TAG, "Unknown control message type : " + s );
                }
            }
            else if(pm.getProtocol().equals("ramp"))
            {
                // Ramp message

                if(pm.getPayload() instanceof RampVolume)
                {
                    RampVolume vol = (RampVolume) pm.getPayload();
                    Log.i(LOG_TAG, "Setting volume");
                }
                else if(pm.getPayload() instanceof RampInfo)
                {
                    RampInfo info = (RampInfo) pm.getPayload();
                    Log.i(LOG_TAG, "Received info request, send response message");
                    RampStatus st = new RampStatus(eventSequence++, state);
                    st.setCmdId(info.getCmdId());
                    sendRamp(st, RampStatus.class);
//                    send("[\"ramp\",{\"cmd_id\":"+info.getCmdId()+",\"type\":\"RESPONSE\",\"status\":{\"event_sequence\":1,\"state\":0}}]");
                }
                else if(pm.getPayload() instanceof RampStatus)
                {
                    RampStatus res = (RampStatus) pm.getPayload();
                    Log.i(LOG_TAG, "Ramp response : " + res.getStatus().getImageUrl());
                }
                else if(pm.getPayload() instanceof RampPlay)
                {
                    RampPlay play = (RampPlay) pm.getPayload();
                    Log.i(LOG_TAG, "Received play command");
                }
                else if(pm.getPayload() instanceof RampStop)
                {
                    RampStop Stop = (RampStop) pm.getPayload();
                    Log.i(LOG_TAG, "Received stop command");
                }
                else if(pm.getPayload() instanceof RampLoad)
                {
                    RampLoad load = (RampLoad) pm.getPayload();
                    Log.i(LOG_TAG, "Ramp response : " + load.getSrc() + " " + load.getContentInfo().getHttpHeaders().getAuthorization());

                    Log.e(LOG_TAG, "wget --header='Authorization: "+load.getContentInfo().getHttpHeaders().getAuthorization()+"' \""+load.getSrc()+"\"");

                    MediaDownloader mediaDownloader = new MediaDownloader( load.getSrc(), load.getContentInfo().getHttpHeaders().getAuthorization());
                    mediaDownloader.start();
                }
                else
                {
                    Log.e(LOG_TAG, "Unknown ramp message type : " + s );
                }
            }
            else
            {
                Log.w(LOG_TAG, "Unknown protocol message ... protocol is " + pm.getProtocol());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
