package at.maui.cheapcast.chromecast.model.ramp;

public class RampPlay extends RampMessage {

    public RampPlay(){
        this.setType("PLAY");
    }

    private double position;

    public double getPosition() {
        return position;
    }
    public void setPosition(double position) {
        this.position = position;
    }
}
