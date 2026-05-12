package routeandriches.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Passenger;
import routeandriches.model.Route;
import routeandriches.model.Stop;
import routeandriches.model.TileType;

/**

 * Represents the PassengerSystem component.

 */

public class PassengerSystem {

    private static final double DEFAULT_SPAWN_INTERVAL_SECONDS = 4.0;

    private final Random random;
    private final Map<String, Stop> stopsById;
    private double spawnIntervalSeconds;
    private double spawnTimerSeconds;
    private int passengerSequence;

    /**
     * Creates a new PassengerSystem instance.
     */
    public PassengerSystem() {
        this.random = new Random(42L);
        this.stopsById = new LinkedHashMap<>();
        this.spawnIntervalSeconds = DEFAULT_SPAWN_INTERVAL_SECONDS;
        this.spawnTimerSeconds = 0.0;
        this.passengerSequence = 1;
    }

    /**
     * Executes update.
     */
    public void update(double deltaSeconds, GameMap map, List<Route> routes) {
        update(deltaSeconds, map, routes, null);
    }

    /**
     * Executes update.
     */
    public void update(double deltaSeconds, GameMap map, List<Route> routes, CityGrowthSystem cityGrowthSystem) {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null.");
        }

        synchronizeStops(map, routes);

        if (stopsById.size() < 2) {
            return;
        }

        spawnTimerSeconds += Math.max(0.0, deltaSeconds);

        double effectiveSpawnInterval = computeEffectiveSpawnInterval(cityGrowthSystem);
        while (spawnTimerSeconds >= effectiveSpawnInterval) {
            spawnTimerSeconds -= effectiveSpawnInterval;
            spawnPassenger(cityGrowthSystem, routes);
            effectiveSpawnInterval = computeEffectiveSpawnInterval(cityGrowthSystem);
        }
    }

    /**
     * Executes findStopByPosition.
     */
    public Stop findStopByPosition(GridPos position) {
        if (position == null) {
            return null;
        }
        String id = stopId(position);
        return stopsById.get(id);
    }

    /**
     * Executes findStopById.
     */
    public Stop findStopById(String stopId) {
        if (stopId == null || stopId.isBlank()) {
            return null;
        }
        return stopsById.get(stopId);
    }

    /**
     * Executes getStopCount.
     */
    public int getStopCount() {
        return stopsById.size();
    }

    /**
     * Executes getTotalWaitingPassengers.
     */
    public int getTotalWaitingPassengers() {
        int total = 0;
        for (Stop stop : stopsById.values()) {
            total += stop.getWaitingCount();
        }
        return total;
    }

    /**
     * Executes getStops.
     */
    public List<Stop> getStops() {
        return Collections.unmodifiableList(new ArrayList<>(stopsById.values()));
    }

    /**
     * Executes getPassengerSequence.
     */
    public int getPassengerSequence() {
        return passengerSequence;
    }

    /**
     * Executes setPassengerSequence.
     */
    public void setPassengerSequence(int passengerSequence) {
        this.passengerSequence = Math.max(1, passengerSequence);
    }

    /**
     * Executes getSpawnIntervalSeconds.
     */
    public double getSpawnIntervalSeconds() {
        return spawnIntervalSeconds;
    }

    /**
     * Executes setSpawnIntervalSeconds.
     */
    public void setSpawnIntervalSeconds(double spawnIntervalSeconds) {
        if (spawnIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException("Spawn interval must be positive.");
        }
        this.spawnIntervalSeconds = spawnIntervalSeconds;
    }

    /**
     * Executes synchronizeStops.
     */
    public void synchronizeStops(GameMap map, List<Route> routes) {
        syncStops(map, routes);
    }

    /**
     * Executes clearWaitingPassengers.
     */
    public void clearWaitingPassengers() {
        for (Stop stop : stopsById.values()) {
            while (stop.pollWaitingPassenger() != null) {
                // drain queue
            }
        }
    }

    /**
     * Executes addWaitingPassengerToStop.
     */
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

    private double computeEffectiveSpawnInterval(CityGrowthSystem cityGrowthSystem) {
        double interval = spawnIntervalSeconds;
        if (cityGrowthSystem != null) {
            double demandFactor = cityGrowthSystem.getGlobalDemandMultiplier();
            if (demandFactor > 0.0) {
                interval = spawnIntervalSeconds / demandFactor;
            }
        }
        return Math.max(0.35, interval);
    }

    private void spawnPassenger(CityGrowthSystem cityGrowthSystem, List<Route> routes) {
        List<Stop> stops = new ArrayList<>(stopsById.values());
        if (stops.size() < 2) {
            return;
        }

        Stop origin = pickStopByDemandWeight(stops, cityGrowthSystem, null);
        Stop destination = pickDestinationForOrigin(origin, stops, cityGrowthSystem, routes);
        if (destination == null || destination == origin) {
            return;
        }

        String passengerId = "P" + passengerSequence++;
        Passenger passenger = new Passenger(passengerId, origin.getId(), destination.getId());
        origin.addWaitingPassenger(passenger);
    }

    private Stop pickDestinationForOrigin(
            Stop origin,
            List<Stop> allStops,
            CityGrowthSystem cityGrowthSystem,
            List<Route> routes) {

        if (origin == null) {
            return null;
        }

        Set<String> compatibleDestinationIds = findCompatibleDestinationIds(origin.getId(), routes);
        if (!compatibleDestinationIds.isEmpty()) {
            List<Stop> compatibleStops = new ArrayList<>();
            for (Stop stop : allStops) {
                if (stop != origin && compatibleDestinationIds.contains(stop.getId())) {
                    compatibleStops.add(stop);
                }
            }

            if (!compatibleStops.isEmpty()) {
                return pickStopByDemandWeight(compatibleStops, cityGrowthSystem, null);
            }
        }

        Stop fallback = origin;
        int attempts = 0;
        while (fallback == origin && attempts < 8) {
            fallback = pickStopByDemandWeight(allStops, cityGrowthSystem, origin);
            attempts++;
        }
        return fallback == origin ? null : fallback;
    }

    private Set<String> findCompatibleDestinationIds(String originStopId, List<Route> routes) {
        Set<String> compatible = new HashSet<>();
        if (originStopId == null || originStopId.isBlank() || routes == null || routes.isEmpty()) {
            return compatible;
        }

        for (Route route : routes) {
            if (route == null) {
                continue;
            }

            List<GridPos> routeStops = route.getStops();
            if (routeStops.size() < 2) {
                continue;
            }

            boolean originOnRoute = false;
            for (GridPos stopPos : routeStops) {
                if (originStopId.equals(stopId(stopPos))) {
                    originOnRoute = true;
                    break;
                }
            }

            if (!originOnRoute) {
                continue;
            }

            for (GridPos stopPos : routeStops) {
                String candidateId = stopId(stopPos);
                if (!originStopId.equals(candidateId)) {
                    compatible.add(candidateId);
                }
            }
        }

        return compatible;
    }

    private Stop pickStopByDemandWeight(List<Stop> stops, CityGrowthSystem cityGrowthSystem, Stop excluded) {
        double totalWeight = 0.0;
        List<Double> weights = new ArrayList<>(stops.size());

        for (Stop stop : stops) {
            if (stop == excluded) {
                weights.add(0.0);
                continue;
            }

            double weight = 1.0;
            if (cityGrowthSystem != null) {
                weight = cityGrowthSystem.getDemandMultiplierAt(stop.getPosition());
            }
            weight = Math.max(0.01, weight);
            totalWeight += weight;
            weights.add(weight);
        }

        if (totalWeight <= 0.0) {
            for (Stop stop : stops) {
                if (stop != excluded) {
                    return stop;
                }
            }
            return stops.get(0);
        }

        double roll = random.nextDouble() * totalWeight;
        for (int i = 0; i < stops.size(); i++) {
            roll -= weights.get(i);
            if (roll <= 0.0) {
                return stops.get(i);
            }
        }

        for (int i = stops.size() - 1; i >= 0; i--) {
            if (stops.get(i) != excluded) {
                return stops.get(i);
            }
        }
        return stops.get(0);
    }

    private String stopId(GridPos pos) {
        return "S_" + pos.getRow() + "_" + pos.getCol();
    }
}

