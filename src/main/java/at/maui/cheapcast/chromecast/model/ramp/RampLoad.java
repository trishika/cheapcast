package at.maui.cheapcast.chromecast.model.ramp;

import com.google.gson.annotations.SerializedName;

public class RampLoad extends RampMessage {

    public RampLoad(){
        this.setType("LOAD");
    }

    private String title;
    private boolean autoplay;
    private ContentInfo contentInfo;
    private String src;
    private String imageURL;

    public boolean isAutoplay() {
        return autoplay;
    }

    public String getTitle() {
        return title;
    }

    public String getSrc() {
        return src;
    }

    public String getImageURL() {
        return imageURL;
    }

    public ContentInfo getContentInfo() {
        return contentInfo;
    }

    public class ContentInfo {
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
}
