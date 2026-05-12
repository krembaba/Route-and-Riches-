/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.model;

/**
 * Represents a traffic light placed on one map tile.
 */
public class TrafficLight {
    private final GridPos position;
    private TrafficLightState state;
    private double stateTimer;
    private double greenDuration;
    private double redDuration;

    /**
     * Creates a traffic light at a given position with default cycle durations.
     *
     * @param position map position
     */
    public TrafficLight(GridPos position) {
        this(position, TrafficLightState.RED, 3.0, 3.0);
    }

    /**
     * Creates a traffic light with explicit initial settings.
     *
     * @param position map position
     * @param initialState starting state
     * @param greenDuration seconds spent in green state
     * @param redDuration seconds spent in red state
     */
    public TrafficLight(GridPos position, TrafficLightState initialState, double greenDuration, double redDuration) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null.");
        }
        if (initialState == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }
        if (greenDuration <= 0.0 || redDuration <= 0.0) {
            throw new IllegalArgumentException("Durations must be positive.");
        }
        this.position = position;
        this.state = initialState;
        this.greenDuration = greenDuration;
        this.redDuration = redDuration;
        this.stateTimer = 0.0;
    }

    /**
     * @return immutable map position of this light
     */
    public GridPos getPosition() {
        return position;
    }

    /**
     * @return current state
     */
    public TrafficLightState getState() {
        return state;
    }

    /**
     * Sets state and resets state timer to zero.
     *
     * @param state new state
     */
    public void setState(TrafficLightState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }
        this.state = state;
        this.stateTimer = 0.0;
    }

    /**
     * @return elapsed seconds inside the current state
     */
    public double getStateTimer() {
        return stateTimer;
    }

    /**
     * Sets the state timer.
     *
     * @param stateTimer seconds elapsed in current state
     */
    public void setStateTimer(double stateTimer) {
        this.stateTimer = Math.max(0.0, stateTimer);
    }

    /**
     * @return green duration in seconds
     */
    public double getGreenDuration() {
        return greenDuration;
    }

    /**
     * @param greenDuration green duration in seconds, must be positive
     */
    public void setGreenDuration(double greenDuration) {
        if (greenDuration <= 0.0) {
            throw new IllegalArgumentException("Green duration must be positive.");
        }
        this.greenDuration = greenDuration;
    }

    /**
     * @return red duration in seconds
     */
    public double getRedDuration() {
        return redDuration;
    }

    /**
     * @param redDuration red duration in seconds, must be positive
     */
    public void setRedDuration(double redDuration) {
        if (redDuration <= 0.0) {
            throw new IllegalArgumentException("Red duration must be positive.");
        }
        this.redDuration = redDuration;
    }

    /**
     * Advances the internal timer and toggles state when needed.
     *
     * @param deltaSeconds elapsed simulation time
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0.0) {
            return;
        }
        stateTimer += deltaSeconds;

        if (state == TrafficLightState.GREEN && stateTimer >= greenDuration) {
            state = TrafficLightState.RED;
            stateTimer = 0.0;
        } else if (state == TrafficLightState.RED && stateTimer >= redDuration) {
            state = TrafficLightState.GREEN;
            stateTimer = 0.0;
        }
    }

    /**
     * @return {@code true} if state is red
     */
    public boolean isRed() {
        return state == TrafficLightState.RED;
    }

    /**
     * @return {@code true} if state is green
     */
    public boolean isGreen() {
        return state == TrafficLightState.GREEN;
    }
}
