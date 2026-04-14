package routeandriches.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Passenger;
import routeandriches.model.Route;
import routeandriches.model.Stop;
import routeandriches.model.TileType;

public class PassengerSystem {

    private static final double DEFAULT_SPAWN_INTERVAL_SECONDS = 4.0;

    private final Random random;
    private final Map<String, Stop> stopsById;
    private double spawnIntervalSeconds;
    private double spawnTimerSeconds;
    private int passengerSequence;

    public PassengerSystem() {
        this.random = new Random(42L);
        this.stopsById = new LinkedHashMap<>();
        this.spawnIntervalSeconds = DEFAULT_SPAWN_INTERVAL_SECONDS;
        this.spawnTimerSeconds = 0.0;
        this.passengerSequence = 1;
    }

    public void update(double deltaSeconds, GameMap map, List<Route> routes) {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null.");
        }

        synchronizeStops(map, routes);

        if (stopsById.size() < 2) {
            return;
        }

        spawnTimerSeconds += Math.max(0.0, deltaSeconds);

        while (spawnTimerSeconds >= spawnIntervalSeconds) {
            spawnTimerSeconds -= spawnIntervalSeconds;
            spawnPassenger();
        }
    }

    public Stop findStopByPosition(GridPos position) {
        if (position == null) {
            return null;
        }
        String id = stopId(position);
        return stopsById.get(id);
    }

    public Stop findStopById(String stopId) {
        if (stopId == null || stopId.isBlank()) {
            return null;
        }
        return stopsById.get(stopId);
    }

    public int getStopCount() {
        return stopsById.size();
    }

    public int getTotalWaitingPassengers() {
        int total = 0;
        for (Stop stop : stopsById.values()) {
            total += stop.getWaitingCount();
        }
        return total;
    }

    public List<Stop> getStops() {
        return Collections.unmodifiableList(new ArrayList<>(stopsById.values()));
    }

    public int getPassengerSequence() {
        return passengerSequence;
    }

    public void setPassengerSequence(int passengerSequence) {
        this.passengerSequence = Math.max(1, passengerSequence);
    }

    public double getSpawnIntervalSeconds() {
        return spawnIntervalSeconds;
    }

    public void setSpawnIntervalSeconds(double spawnIntervalSeconds) {
        if (spawnIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException("Spawn interval must be positive.");
        }
        this.spawnIntervalSeconds = spawnIntervalSeconds;
    }

    public void synchronizeStops(GameMap map, List<Route> routes) {
        syncStops(map, routes);
    }

    public void clearWaitingPassengers() {
        for (Stop stop : stopsById.values()) {
            while (stop.pollWaitingPassenger() != null) {
                // drain queue
            }
        }
    }

    public boolean addWaitingPassengerToStop(String stopId, Passenger passenger) {
        Stop stop = findStopById(stopId);
        if (stop == null || passenger == null) {
            return false;
        }
        stop.addWaitingPassenger(passenger);
        return true;
    }

    private void syncStops(GameMap map, List<Route> routes) {
        Map<String, GridPos> discovered = new HashMap<>();

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col).getType() == TileType.STOP) {
                    GridPos pos = new GridPos(row, col);
                    discovered.put(stopId(pos), pos);
                }
            }
        }

        if (routes != null) {
            for (Route route : routes) {
                if (route == null) {
                    continue;
                }
                for (GridPos stopPos : route.getStops()) {
                    if (stopPos == null || !map.isWithinBounds(stopPos.getRow(), stopPos.getCol())) {
                        continue;
                    }
                    discovered.put(stopId(stopPos), stopPos);
                }
            }
        }

        stopsById.keySet().removeIf(id -> !discovered.containsKey(id));

        for (Map.Entry<String, GridPos> entry : discovered.entrySet()) {
            stopsById.computeIfAbsent(entry.getKey(),
                    id -> new Stop(id, "Stop " + id.substring(2), entry.getValue()));
        }
    }

    private void spawnPassenger() {
        List<Stop> stops = new ArrayList<>(stopsById.values());
        if (stops.size() < 2) {
            return;
        }

        Stop origin = stops.get(random.nextInt(stops.size()));
        Stop destination = origin;

        while (destination == origin) {
            destination = stops.get(random.nextInt(stops.size()));
        }

        String passengerId = "P" + passengerSequence++;
        Passenger passenger = new Passenger(passengerId, origin.getId(), destination.getId());
        origin.addWaitingPassenger(passenger);
    }

    private String stopId(GridPos pos) {
        return "S_" + pos.getRow() + "_" + pos.getCol();
    }
}
