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

package at.maui.cheapcast.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import at.maui.cheapcast.Const;
import at.maui.cheapcast.Installation;
import at.maui.cheapcast.R;
import at.maui.cheapcast.Utils;
import at.maui.cheapcast.activity.CastActivity;
import at.maui.cheapcast.activity.PreferenceActivity;
import at.maui.cheapcast.chromecast.*;
import at.maui.cheapcast.chromecast.model.AppRegistration;
import at.maui.cheapcast.ssdp.SSDP;
import fi.iki.elonen.BufferServer;

import com.google.gson.Gson;

import org.droidupnp.controller.cling.ServiceController;
import org.droidupnp.controller.cling.ServiceListener;
import org.droidupnp.controller.upnp.IUpnpServiceController;
import org.droidupnp.model.upnp.ARendererState;
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.IUpnpDevice;
import org.droidupnp.model.upnp.RendererDiscovery;
import org.droidupnp.model.upnp.didl.SimpleDIDLItem;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.fourthline.cling.android.AndroidUpnpService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;

public class CheapCastService extends Service implements IDeviceDiscoveryObserver {

    public static final String LOG_TAG = "CheapCastService";
    private NotificationManager mNotificationManager;
    private HashMap<String, App> mRegisteredApps;

    private WifiManager mWifiManager;
    private NetworkInterface mNetIf;
    private WifiManager.MulticastLock mMulticastLock;

    private SSDP mSsdp;
    private ArrayList<Server> mServer;
    public static final int START_PORT = 8008;
    private ArrayList<IUpnpDevice> mUpnpDevices;
    private ArrayList<BufferServer> mMediaServer;
    public static final int START_PORT_MEDIA = START_PORT + 1000;
    private IUpnpServiceController mServiceController;

    private Gson mGson;
    private SharedPreferences mPreferences;
    private boolean mRunning = false;
    private ICheapCastCallback mCallback;
    private App mLastApp;

    private final ICheapCastService.Stub mBinder = new ICheapCastService.Stub() {
        @Override
        public void addListener(ICheapCastCallback cb) throws RemoteException {
            mCallback = cb;
        }

        @Override
        public void removeListener() throws RemoteException {
            mCallback = null;
        }

        @Override
        public void setUpnpURL(int id, String url) {

            try {

                // Start the media server if not started yet
//                if(mMediaServer.get(id) == null)
//                {
//                    mMediaServer.set(id, new BufferServer(null, START_PORT_MEDIA+id));
//                }
//                mMediaServer.get(id).setUri(url);
//                mMediaServer.get(id).start();
                String ip = Utils.getLocalV4Address(mNetIf).getHostAddress();
                IUpnpDevice device = mUpnpDevices.get(id);
                ARendererState rs = PreferenceActivity.factory.createRendererState();
                IRendererCommand mDeviceCommand = PreferenceActivity.factory.createRendererCommand(mServiceController, device, rs);
//                mDeviceCommand.launchItem(new SimpleDIDLItem("http://"+ip+":"+(START_PORT_MEDIA+id)+"/audio.mp3"));
                mDeviceCommand.launchItem(new SimpleDIDLItem(url));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "An exception occured in setUpnpURL" );
            }

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mNetIf = Utils.getActiveNetworkInterface();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPreferences = getSharedPreferences("cheapcast", MODE_PRIVATE | MODE_MULTI_PROCESS);
        } else {
            mPreferences = getSharedPreferences("cheapcast", MODE_PRIVATE);
        }
        Log.d(LOG_TAG, String.format("Starting up: friendlyName: %s", mPreferences.getString("friendly_name","CheapCasto")));
        mGson = new Gson();

        mRegisteredApps = new HashMap<String, App>();
        registerApp(new App("ChromeCast", "https://www.gstatic.com/cv/receiver.html?$query"));
        registerApp(new App("YouTube", "https://www.youtube.com/tv?$query"));
        registerApp(new App("PlayMovies", "https://play.google.com/video/avi/eureka?$query", new String[]{"play-movies", "ramp"}));
        registerApp(new App("GoogleMusic", "https://play.google.com/music/cast/player"));

        registerApp(new App("GoogleCastSampleApp", "http://anzymrcvr.appspot.com/receiver/anzymrcvr.html"));
        registerApp(new App("GoogleCastPlayer", "https://www.gstatic.com/eureka/html/gcp.html"));
        registerApp(new App("Fling", "$query"));
        registerApp(new App("TicTacToe", "http://www.gstatic.com/eureka/sample/tictactoe/tictactoe.html", new String[]{"com.google.chromecast.demo.tictactoe"}));

        mServer = new ArrayList<Server>();
        mUpnpDevices = new ArrayList<IUpnpDevice>();
        mMediaServer = new ArrayList<BufferServer>();
    }

    private void registerApp(App app) {
        mRegisteredApps.put(app.getName(), app);
        Log.d(LOG_TAG, String.format("Registered app: %s",app.getName()));
    }

    public void renderAppStatus(HttpServletResponse httpServletResponse, App app) throws IOException {

        String appDesc = Const.APP_INFO;
        appDesc = appDesc.replaceAll("#name#", app.getName());
        appDesc = appDesc.replaceAll("#connectionSvcURL#", app.getConnectionSvcURL());
        appDesc = appDesc.replaceAll("#protocols#", app.getProtocols());
        appDesc = appDesc.replaceAll("#state#", app.getState());
        appDesc = appDesc.replaceAll("#link#", app.getLink());

        httpServletResponse.setContentType("application/xml;charset=utf-8");
        httpServletResponse.setHeader("Access-Control-Allow-Method", "GET, POST, DELETE, OPTIONS");
        httpServletResponse.setHeader("Access-Control-Expose-Headers", "Location");
        httpServletResponse.setHeader("Cache-control", "no-cache, must-revalidate, no-store");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.getWriter().print(appDesc);
    }

    public App getApp(String appName) {
        return mRegisteredApps.get(appName);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");

        Notification n = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.Builder mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_service)
                .setContentTitle("CheapCast")
                .setContentText("Service enabled.")
                .setOngoing(true)
                .addAction(R.drawable.ic_reload, getString(R.string.restart_service), PendingIntent.getBroadcast(this, 1, new Intent(Const.ACTION_RESTART), PendingIntent.FLAG_ONE_SHOT))
                .addAction(R.drawable.ic_stop, getString(R.string.stop_service), PendingIntent.getBroadcast(this, 2, new Intent(Const.ACTION_STOP), PendingIntent.FLAG_ONE_SHOT));

            Intent i = new Intent(this, PreferenceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            mBuilder.setContentIntent(pi);
            n = mBuilder.build();
        } else {
            Intent i = new Intent(this, PreferenceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_service)
                .setContentTitle("CheapCast")
                .setContentText("Service enabled.")
                .setOngoing(true)
                .setContentIntent(pi);
            n = mBuilder.getNotification();
        }

        startForeground(1337, n);

        if(!mRunning)
            initService();

        mServiceController = new ServiceController(this);
        mServiceController.resume();
        RendererDiscovery rendererDiscovery = new RendererDiscovery(mServiceController.getServiceListener());
        rendererDiscovery.addObserver(this);
        rendererDiscovery.resume();
        //mServiceController.getServiceListener().addListener(this);

        return START_STICKY;
    }

    private synchronized void createServer(String name){

        int port = START_PORT + mServer.size();

        Log.d(LOG_TAG, String.format("Creating server %s on port %d", name, port));

        if(mWifiManager != null){
            mMulticastLock = mWifiManager.createMulticastLock("SSDP");
            mMulticastLock.acquire();
        }

        try {
            Server server = new Server(port);
            server.setSendDateHeader(true);
            server.setSendServerVersion(false);

            CustomWebSocketHandler wsHandler = new CustomWebSocketHandler(port);
            server.setHandler(wsHandler);
            CastRESTHandler castRESTHandler = new CastRESTHandler(name, port);
            wsHandler.setHandler(castRESTHandler);
            server.start();

            mServer.add(server);

            Log.d(LOG_TAG, "Initialized HTTP/WS Server");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            mSsdp = new SSDP(this, port);
            mSsdp.start();
            Log.d(LOG_TAG, "Initialized SSDP/DIAL Discovery");
        } catch (IOException e) {
            Log.e(LOG_TAG, "SSDP Init failed", e);
        }
    }

    private void initService() {
        createServer(mPreferences.getString("friendly_name", getString(R.string.cheapcast) + "_" + Build.MODEL));
        mRunning = true;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");

        mSsdp.shutdown();

        try {
            for(Server server : mServer)
                server.stop();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        stopForeground(true);
    }

    @Override
    public void addedDevice(IUpnpDevice device) {
        Log.d(LOG_TAG, String.format("Add device %s", device.getFriendlyName()));
        createServer(device.getFriendlyName());
        mUpnpDevices.add(device);
        mMediaServer.add(null);
    }

    @Override
    public void removedDevice(IUpnpDevice device) {
        Log.d(LOG_TAG, String.format("Remove device %s", device.getFriendlyName()));
        int i = mUpnpDevices.indexOf(device);
        mUpnpDevices.remove(device);
        mMediaServer.remove(i);
        mServer.remove(i+1);
    }

    private class CustomWebSocketHandler extends WebSocketHandler {

        private int mPort;

        public CustomWebSocketHandler(int port){
            this.mPort = port;
        }

        @Override
        public WebSocket doWebSocketConnect(HttpServletRequest httpServletRequest, String protocol) {
            Log.d(LOG_TAG,"WS Requested "+ httpServletRequest.getPathInfo());

            if(httpServletRequest.getPathInfo().equals("/system/control")) {
                return new SystemControlSocket(CheapCastService.this);
            } else if(httpServletRequest.getPathInfo().equals("/connection")) {
                return new ConnectionSocket(CheapCastService.this);
            } else if(httpServletRequest.getPathInfo().startsWith("/session/")) {
                String appName = httpServletRequest.getPathInfo().replace("/session/","");
                if(mPort-START_PORT-1 == -1)
                {
                    return new SessionSocket(CheapCastService.this, getApp(appName));
                }
                else
                {
                    IUpnpDevice device = mUpnpDevices.get(mPort-START_PORT);
                    ARendererState rs = PreferenceActivity.factory.createRendererState();
                    IRendererCommand deviceCommand = PreferenceActivity.factory.createRendererCommand(
                            mServiceController, device, rs);
                    return new UPnPSessionSocket(CheapCastService.this, getApp(appName), deviceCommand);
                }
            } else if(httpServletRequest.getPathInfo().startsWith("/receiver/")) {
                String appName = httpServletRequest.getPathInfo().replace("/receiver/","");
                return new ReceiverSocket(CheapCastService.this, getApp(appName));
            } else {
                Log.e(LOG_TAG, "WS FAIL");
            }

            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public class CastRESTHandler extends AbstractHandler {

        int mPort;
        String mName;

        public CastRESTHandler(String name, int port)
        {
            mName = name;
            mPort = port;
        }

        @Override
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
            String server = Utils.getLocalV4Address(mNetIf).getHostAddress();
            httpServletResponse.setHeader("Access-Control-Allow-Origin","*");

            if(httpServletRequest.getPathInfo().startsWith("/ssdp/device-desc.xml") && httpServletRequest.getMethod().equals("GET"))
            {
                Log.d(LOG_TAG, String.format(":%d GET /ssdp/device-desc.xml from %s, %s", mPort,
                    httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent")));

                String deviceDesc = Const.DEVICE_DESC;
                deviceDesc = deviceDesc.replaceAll("#uuid#", Installation.id(CheapCastService.this, mPort-START_PORT));
                deviceDesc = deviceDesc.replaceAll("#friendlyname#", mName);
                deviceDesc = deviceDesc.replaceAll("#base#", "http://"+server+":"+mPort);

                httpServletResponse.setHeader("Access-Control-Allow-Method", "GET, POST, DELETE, OPTIONS");
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "Location");

                httpServletResponse.setContentType("application/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.addHeader("Application-URL", "http://"+server+":"+mPort+"/apps");
                httpServletResponse.getWriter().print(deviceDesc);
            }
            else if(httpServletRequest.getPathInfo().equals("/apps") && httpServletRequest.getMethod().equals("GET"))
            {
                // Active app polling
                App activeApp = null;
                for(App app : mRegisteredApps.values()) {
                    if(app.getState().equals("running")) {
                        activeApp = app;
                        break;
                    }
                }

                if(activeApp != null) {
                    Log.d(LOG_TAG, String.format(":%d GET /apps: Redirecting to %s", mPort, activeApp.getName()));
                    httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    httpServletResponse.addHeader("Location", String.format("http://%s:"+mPort+"/apps/%s", server, activeApp.getName()));
                } else {
                    Log.d(LOG_TAG, String.format(":%d GET /apps: SC_NO_CONTENT at /apps", mPort));
                    httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    httpServletResponse.setContentType("application/xml;charset=utf-8");
                    httpServletResponse.setHeader("Access-Control-Allow-Method", "GET, POST, DELETE, OPTIONS");
                    httpServletResponse.setHeader("Access-Control-Expose-Headers", "Location");
                }
            }
            else if(httpServletRequest.getPathInfo().startsWith("/apps/") && httpServletRequest.getMethod().equals("GET"))
            {
                String appName = httpServletRequest.getPathInfo().replace("/apps/","");
                Log.i(LOG_TAG, String.format(":%d GET /apps/%s", mPort, appName));
                App app = mRegisteredApps.get(appName);
                renderAppStatus(httpServletResponse, app);
            }
            else if(httpServletRequest.getPathInfo().startsWith("/apps/") && httpServletRequest.getMethod().equals("DELETE"))
            {
                // Stop app
                String appName = httpServletRequest.getPathInfo().replace("/apps/","").replace("/web-1","");
                App app = mRegisteredApps.get(appName);

                if(app != null) {
                    app.stop();

                    if(mCallback != null && app.getReceivers().size() == 0)
                        try {
                            mCallback.onAppStopped(appName);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    renderAppStatus(httpServletResponse, app);
                }
            }
            else if(httpServletRequest.getPathInfo().startsWith("/apps/") && httpServletRequest.getMethod().equals("POST"))
            {
                // Start app
                String appName = httpServletRequest.getPathInfo().replace("/apps/","");

                Log.i(LOG_TAG, String.format(":%d POST /apps/%s", mPort, appName));
                App app = mRegisteredApps.get(appName);

                if(app != null)
                {
                    app.setLink("<link rel='run' href='web-1'/>");
                    app.setConnectionSvcURL(String.format("http://%s:"+mPort+"/connection/%s", server, appName));
                    app.addProtocol("ramp");
                    app.setState("running");

                    String params = Utils.readerToString(httpServletRequest.getReader());

                    Log.d(LOG_TAG, "Addtl. App params: "+ params);
                    String appUrl = app.getReceiverUrl().replace("$query", params);

                    if(mPort-START_PORT-1 == -1)
                    {
                        // Local device, start the display activity
                        Intent i = new Intent(CheapCastService.this, CastActivity.class);
                        i.setData(Uri.parse(appUrl));
                        i.putExtra(Const.APP_EXTRA, app.getName());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // TODO Remove this...
                        i.putExtra("upnp", mPort-START_PORT-1);
                        startActivity(i);
                    }

                    mLastApp = app;

                    httpServletResponse.setContentType("text/html; charset=utf-8");
                    httpServletResponse.setHeader("Location", String.format("http://%s:"+mPort+"/apps/%s/web-1", server, appName));
                    httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
                }
            }
            else if(httpServletRequest.getPathInfo().startsWith("/connection/") && httpServletRequest.getMethod().equals("POST"))
            {
                String appName = httpServletRequest.getPathInfo().replace("/connection/","");
                Log.d(LOG_TAG, String.format(":%d POST /connection/%s",mPort, appName));
                App app = mRegisteredApps.get(appName);

                if(app != null) {
                    httpServletResponse.setHeader("Access-Control-Allow-Method", "POST, OPTIONS");
                    httpServletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    httpServletResponse.getWriter().print(String.format("{\"URL\":\"ws://%s:"+mPort+"/session/%s?%d\"," +
                        "\"pingInterval\":3}", server, appName, app.getRemotes().size()));
                }
            }
            else if(httpServletRequest.getPathInfo().equals("/registerApp") && httpServletRequest.getMethod().equals("POST"))
            {
                Log.d(LOG_TAG, String.format(":%d POST /registerApp/", mPort));

                if(mPreferences.getBoolean("allow_custom_apps", false)) {
                    String rawBody = Utils.readerToString(httpServletRequest.getReader());
                    AppRegistration reg = mGson.fromJson(rawBody, AppRegistration.class);

                    if(reg != null) {
                        registerApp(new App(reg.getAppName(), reg.getAppUrl(), reg.getProtocols()));
                        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                        httpServletResponse.setContentType("application/json; charset=utf-8");
                        httpServletResponse.getWriter().print("{\"msg\":\"OK\"}");
                    } else {
                        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }

            }
            else
            {
                Log.w(LOG_TAG,"Requested "+ httpServletRequest.getPathInfo());
                Log.w(LOG_TAG,"The princess is in another castle");
                httpServletResponse.setContentType("text/html");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().println("<h1>This is CheapCast :D</h1>");

                if(mPreferences.getBoolean("allow_custom_apps", false)) {
                    httpServletResponse.getWriter().println("<h3>Registered Apps:</h3>");
                    httpServletResponse.getWriter().print("<ul>");
                    for(App app : mRegisteredApps.values()) {
                        httpServletResponse.getWriter().println(String.format("<li>%s - %s, Protocols: %s</li>", app.getName(), app.getReceiverUrl(), app.getProtocolList()));
                    }
                    httpServletResponse.getWriter().print("</ul>");
                }
            }

            AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
            String ct = connection.getResponseFields().getStringField("Content-Type");
            if(ct.contains(";")) {
                AbstractHttpConnection.getCurrentConnection().getResponseFields().put("Content-Type", ct.split(";")[0]);
            }

            ((Request) httpServletRequest).setHandled(true);
        }
    };

}
