package fi.iki.elonen;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class BufferServer extends NanoHTTPD {

    public static final String LOG_TAG = "BufferServer";

    private String mUri;
    private String mime = "application/octet-stream";

    public BufferServer(String host, int port) {
        super(host, port);
        Log.i(LOG_TAG, "Create server for port " + port);
    }

    public void setUri(String uri)
    {
        mUri = uri;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files)
    {
        Log.i(LOG_TAG, method + " '" + uri + "' ");

        Response res = null;

        try {
            // Calculate etag
            String etag = Integer.toHexString(mUri.hashCode());

//            URL url = new URL("http://www.android.com/");
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            readStream(in);

            URL url = new URL(mUri);
            URLConnection connection = url.openConnection();
//            connection.connect();
            long fileLen = connection.getContentLength();
//            InputStream input = url.openStream();
            InputStream input = new BufferedInputStream(connection.getInputStream());

            Log.i(LOG_TAG, "Connected to remote server, file is size " + fileLen);

            // Support (simple) skipping:
//            long startFrom = 0;
//            long endAt = -1;
//            String range = header.get("range");
//            if (range != null) {
//                if (range.startsWith("bytes=")) {
//                    range = range.substring("bytes=".length());
//                    int minus = range.indexOf('-');
//                    try {
//                        if (minus > 0) {
//                            startFrom = Long.parseLong(range.substring(0, minus));
//                            endAt = Long.parseLong(range.substring(minus + 1));
//                        }
//                    } catch (NumberFormatException ignored) {
//                    }
//                }
//            }
//
//            // Change return code and add Content-Range header when skipping is requested
////            long fileLen = f.length();
//            if (range != null && startFrom >= 0) {
//                if (startFrom >= fileLen) {
//                    res = new Response(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
//                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
//                    res.addHeader("ETag", etag);
//                } else {
//                    if (endAt < 0) {
//                        endAt = fileLen - 1;
//                    }
//                    long newLen = endAt - startFrom + 1;
//                    if (newLen < 0) {
//                        newLen = 0;
//                    }
//
//                    final long dataLen = newLen;
//                    InputStream stream = new BufferedInputStream(input) {
//                        @Override
//                        public int available() throws IOException {
//                            return (int) dataLen;
//                        }
//                    };
//                    stream.skip(startFrom);
//
//                    res = new Response(Response.Status.PARTIAL_CONTENT, mime, stream);
//                    res.addHeader("Content-Length", "" + dataLen);
//                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            } else {
//                if (etag.equals(header.get("if-none-match")))
//                    res = new Response(Response.Status.NOT_MODIFIED, mime, "");
//                else {
                    res = new Response(Response.Status.OK, mime, input);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
//                }
//            }
        } catch (IOException ioe) {
            res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }
}
