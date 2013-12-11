package org.droidupnp.model.upnp;

public enum UPnPState {
    PLAY(0), PAUSE(1), STOP(2);

    private int mValue;
    public int getValue() { return mValue; }
    private UPnPState(int value) { mValue = value; }
}
