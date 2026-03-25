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
 *
 * @author dell
 */
public class TrafficLightSystem {
    private final List<TrafficLight> trafficLights;

    public TrafficLightSystem() {
        this.trafficLights = new ArrayList<>();
    }

    public void addTrafficLight(TrafficLight trafficLight) {
        if (trafficLight == null) {
            throw new IllegalArgumentException("Traffic light cannot be null.");
        }
        trafficLights.add(trafficLight);
    }

    public boolean removeTrafficLightAt(GridPos position) {
        return trafficLights.removeIf(light -> light.getPosition().equals(position));
    }

    public TrafficLight getTrafficLightAt(GridPos position) {
        for (TrafficLight light : trafficLights) {
            if (light.getPosition().equals(position)) {
                return light;
            }
        }
        return null;
    }

    public boolean hasTrafficLightAt(GridPos position) {
        return getTrafficLightAt(position) != null;
    }

    public boolean isRedAt(GridPos position) {
        TrafficLight light = getTrafficLightAt(position);
        return light != null && light.getState() == TrafficLightState.RED;
    }

    public boolean isGreenAt(GridPos position) {
        TrafficLight light = getTrafficLightAt(position);
        return light != null && light.getState() == TrafficLightState.GREEN;
    }

    public void update(double deltaSeconds) {
        for (TrafficLight light : trafficLights) {
            light.update(deltaSeconds);
        }
    }

    public List<TrafficLight> getTrafficLights() {
        return Collections.unmodifiableList(trafficLights);
    }
}
