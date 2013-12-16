package at.maui.cheapcast.chromecast.model.ramp;

import at.maui.cheapcast.chromecast.model.State;


public class RampStatusInternal {

    public RampStatusInternal(int seq, State state){
        this.state = state.getValue();
        this.eventSequence = seq;
    }

    private int eventSequence, state;
    private String contentId, title, imageUrl;
    private double duration, currentTime;
    private double volume;
    private boolean muted, timeProgress;

    public double getCurrentTime() { return currentTime; }
    public void setCurrentTime(double currentTime) { this.currentTime = currentTime; }

    public int getEventSequence() {
            return eventSequence;
        }
    public void setEventSequence(int eventSequence) {
            this.eventSequence = eventSequence;
        }

    public int getState() {
            return state;
        }
    public void setState(int state) {
            this.state = state;
        }

    public String getContentId() {
            return contentId;
        }
    public void setContentId(String contentId) {
            this.contentId = contentId;
        }

    public String getTitle() {
            return title;
        }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
            return imageUrl;
        }
    public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

    public double getDuration() {
            return duration;
        }
    public void setDuration(double duration) {
            this.duration = duration;
        }

    public double getVolume() {
            return volume;
        }
    public void setVolume(double volume) {
            this.volume = volume;
        }

    public boolean isMuted() {
            return muted;
        }
    public void setMuted(boolean muted) {
            this.muted = muted;
        }

    public boolean isTimeProgress() {
            return timeProgress;
        }
    public void setTimeProgress(boolean timeProgress) {
            this.timeProgress = timeProgress;
        }

    private RampContentInfo contentInfo;

    public RampContentInfo getContentInfo() {
        return contentInfo;
    }
    public void setContentInfo(RampContentInfo contentInfo) {
        this.contentInfo = contentInfo;
    }

}
