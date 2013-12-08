package at.maui.cheapcast.chromecast.model;

public enum State {
    IDLE(0), STOPPED(1), PLAYING(2);

    private int mValue;
    public int getValue() { return mValue; }
    private State(int value) { mValue = value; }
}
