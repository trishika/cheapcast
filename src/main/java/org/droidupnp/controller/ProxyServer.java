package org.droidupnp.controller;

import java.io.*;
import java.net.*;

public class ProxyServer {

    private String mHost;
    private int mPort;

    public ProxyServer(String host, int port)
    {
        mHost = host;
        mPort = port;
    }

    public void start(final String sUrl) throws IOException
    {
        // listen for connection
        final ServerSocket ss = new ServerSocket(mPort);

        final byte[] request = new byte[1024];
        byte[] reply = new byte[4096];

        Thread t = new Thread() {
            public void run() {

                Socket client = null, server = null;
                try {
                    // Wait for a connection on the local port
                    client = ss.accept();

                    URL url = new URL(sUrl);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
//                    OutputStream output = new FileOutputStream(file);

//                    final InputStream streamFromClient = client.getInputStream();
//                    final OutputStream streamToClient = client.getOutputStream();
//
//                    // Connection to the file to provide
//                    try {
//                        server = new Socket(host, remoteport);
//                    } catch (IOException e) {
//                        client.close();
//                        continue;
//                    }
//
//                        URL url = new URL(urlToDownload);
//
//                    // Get server streams.
//                    final InputStream streamFromServer = server.getInputStream();
//                    final OutputStream streamToServer = server.getOutputStream();
//
//                    // a thread to read the client's requests and pass them
//                    // to the server. A separate thread for asynchronous.
//                    Thread t = new Thread() {
//                        public void run() {
//                            int bytesRead;
//                            try {
//                                while ((bytesRead = streamFromClient.read(request)) != -1) {
//                                    streamToServer.write(request, 0, bytesRead);
//                                    streamToServer.flush();
//                                }
//                            } catch (IOException e) {
//                            }
//
//                            // the client closed the connection to us, so close our
//                            // connection to the server.
//                            try {
//                                streamToServer.close();
//                            } catch (IOException e) {
//                            }
//                        }
//                    };
//
//                    // Start the client-to-server request thread running
//                    t.start();
//
//                    // Read the server's responses
//                    // and pass them back to the client.
//                    int bytesRead;
//                    try {
//                        while ((bytesRead = streamFromServer.read(reply)) != -1) {
//                            streamToClient.write(reply, 0, bytesRead);
//                            streamToClient.flush();
//                        }
//                    } catch (IOException e) {
//                    }
//
//                    // The server closed its connection to us, so we close our
//                    // connection to our client.
//                    streamToClient.close();
                } catch (IOException e) {
                    System.err.println(e);
                } finally {
                    try {
                        if (server != null)
                            server.close();
                        if (client != null)
                            client.close();
                    } catch (IOException e) {
                    }
                }

            }
        };
    }
}
