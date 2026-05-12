/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import routeandriches.model.GridPos;
import routeandriches.model.TrafficLight;
import routeandriches.model.TrafficLightState;

/**
 * Manages all traffic lights in the current game session.
 */
public class TrafficLightSystem {
    private final List<TrafficLight> trafficLights;

    /**
     * Creates a new TrafficLightSystem instance.
     */
    public TrafficLightSystem() {
        this.trafficLights = new ArrayList<>();
    }

    /**
     * Adds a new traffic light if the tile does not already contain one.
     *
     * @param trafficLight light to add
     */
    public void addTrafficLight(TrafficLight trafficLight) {
        if (trafficLight == null) {
            throw new IllegalArgumentException("Traffic light cannot be null.");
        }
        if (hasTrafficLightAt(trafficLight.getPosition())) {
            return;
        }
        trafficLights.add(trafficLight);
    }

    /**
     * Removes all managed traffic lights.
     */
    public void clear() {
        trafficLights.clear();
    }

    /**
     * Removes a light by position.
     *
     * @param position target position
     * @return {@code true} if an existing light was removed
     */
    public boolean removeTrafficLightAt(GridPos position) {
        if (position == null) {
            return false;
        }
        return trafficLights.removeIf(light -> light.getPosition().equals(position));
    }

    /**
     * Returns a light at the target position.
     *
     * @param position target position
     * @return light instance or {@code null}
     */
    public TrafficLight getTrafficLightAt(GridPos position) {
        if (position == null) {
            return null;
        }
        for (TrafficLight light : trafficLights) {
            if (light.getPosition().equals(position)) {
                return light;
            }
        }
        return null;
    }

    /**
     * @param position target position
     * @return {@code true} when a light exists at the target position
     */
    public boolean hasTrafficLightAt(GridPos position) {
        return getTrafficLightAt(position) != null;
    }

    /**
     * @param position target position
     * @return {@code true} if there is a red light at this tile
     */
    public boolean isRedAt(GridPos position) {
        TrafficLight light = getTrafficLightAt(position);
        return light != null && light.getState() == TrafficLightState.RED;
    }

    /**
     * @param position target position
     * @return {@code true} if there is a green light at this tile
     */
    public boolean isGreenAt(GridPos position) {
        TrafficLight light = getTrafficLightAt(position);
        return light != null && light.getState() == TrafficLightState.GREEN;
    }

    /**
     * Advances all managed lights.
     *
     * @param deltaSeconds elapsed simulation time
     */
    public void update(double deltaSeconds) {
        for (TrafficLight light : trafficLights) {
            light.update(deltaSeconds);
        }
    }

    /**
     * @return unmodifiable view of all traffic lights
     */
    public List<TrafficLight> getTrafficLights() {
        return Collections.unmodifiableList(trafficLights);
    }
}
