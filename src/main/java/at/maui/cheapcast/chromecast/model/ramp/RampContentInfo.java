package at.maui.cheapcast.chromecast.model.ramp;

import com.google.gson.annotations.SerializedName;

public class RampContentInfo {
    private String albumTitle;
    private String artist;

    @SerializedName("android.media.intent.extra.HTTP_HEADERS")
    private HTTPHeaders httpHeaders;

    public String getAlbumTitle() {
        return albumTitle;
    }

    public String getArtist() {
        return artist;
    }

    public HTTPHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public class HTTPHeaders {
        @SerializedName("Authorization")
        private String authorization;

        public String getAuthorization() {
            return authorization;
        }
    }
}