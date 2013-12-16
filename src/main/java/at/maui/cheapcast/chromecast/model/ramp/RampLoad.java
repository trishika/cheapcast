package at.maui.cheapcast.chromecast.model.ramp;

public class RampLoad extends RampMessage {

    public RampLoad(){
        this.setType("LOAD");
    }

    private String title;
    private boolean autoplay;
    private RampContentInfo contentInfo;
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

    public RampContentInfo getContentInfo() {
        return contentInfo;
    }
}
