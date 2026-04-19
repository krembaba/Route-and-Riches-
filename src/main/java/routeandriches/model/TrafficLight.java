/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.model;

/**
 *
 * @author dell
 */
public class TrafficLight {
    private final GridPos position;
    private TrafficLightState state;
    private double stateTimer;
    private double greenDuration;
    private double redDuration;

    public TrafficLight(GridPos position) {
        this(position, TrafficLightState.RED, 3.0, 3.0);
    }

    public TrafficLight(GridPos position, TrafficLightState initialState, double greenDuration, double redDuration) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null.");
        }
        this.position = position;
        this.state = initialState;
        this.greenDuration = greenDuration;
        this.redDuration = redDuration;
        this.stateTimer = 0.0;
    }

    public GridPos getPosition() {
        return position;
    }

    public TrafficLightState getState() {
        return state;
    }

    public void setState(TrafficLightState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }
        this.state = state;
        this.stateTimer = 0.0;
    }

    public double getStateTimer() {
        return stateTimer;
    }

    public double getGreenDuration() {
        return greenDuration;
    }

    public void setGreenDuration(double greenDuration) {
        this.greenDuration = greenDuration;
    }

    public double getRedDuration() {
        return redDuration;
    }

    public void setRedDuration(double redDuration) {
        this.redDuration = redDuration;
    }

    public void update(double deltaSeconds) {
        stateTimer += deltaSeconds;

        if (state == TrafficLightState.GREEN && stateTimer >= greenDuration) {
            state = TrafficLightState.RED;
            stateTimer = 0.0;
        } else if (state == TrafficLightState.RED && stateTimer >= redDuration) {
            state = TrafficLightState.GREEN;
            stateTimer = 0.0;
        }
    }

    public boolean isRed() {
        return state == TrafficLightState.RED;
    }

    public boolean isGreen() {
        return state == TrafficLightState.GREEN;
    }
}
