package routeandriches.model;

import java.util.ArrayList;
import java.util.List;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.GameState;
import routeandriches.model.enums.VehicleType;
import routeandriches.system.CityGrowthSystem;
import routeandriches.system.GameClock;
import routeandriches.system.PassengerSystem;
import routeandriches.system.TrafficLightSystem;

/**
 * Core game aggregate that coordinates simulation state and systems.
 */
public class Game {

    private static final int STARTING_MONEY = 1000;
    private static final int DELIVERY_REWARD = 15;
    private static final int TARGET_MONEY = 5000;
    private static final int ROAD_COST = 15;
    private static final int STOP_COST = 25;
    private static final int TRAFFIC_LIGHT_COST = 50;
    private static final int BUS_COST = 100;
    private static final int TRAM_COST = 150;

    private GameState gameState;
    private final GameClock gameClock;
    private final GameMap gameMap;
    private int money;
    private final List<Vehicle> vehicles;
    private final List<Route> routes;
    private final PassengerSystem passengerSystem;
    private final TrafficLightSystem trafficLightSystem;
    private final CityGrowthSystem cityGrowthSystem;
    private String endReason;

    /**
     * Creates a new game with default map, economy and systems.
     */
    public Game() {
        this.gameState = GameState.PAUSED;
        this.gameClock = new GameClock();
        this.gameMap = new GameMap(60, 90);
        this.money = STARTING_MONEY;
        this.vehicles = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.passengerSystem = new PassengerSystem();
        this.trafficLightSystem = new TrafficLightSystem();
        this.cityGrowthSystem = new CityGrowthSystem();
        this.endReason = null;
    }

    /**
     * Executes getGameState.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Executes getGameClock.
     */
    public GameClock getGameClock() {
        return gameClock;
    }

    /**
     * Executes getGameMap.
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Executes getMoney.
     */
    public int getMoney() {
        return money;
    }

    /**
     * Executes setMoney.
     */
    public void setMoney(int money) {
        this.money = Math.max(0, money);
        evaluateEndConditions();
    }

    /**
     * Executes getTargetMoney.
     */
    public int getTargetMoney() {
        return TARGET_MONEY;
    }

    /**
     * Executes getRoadCost.
     */
    public int getRoadCost() {
        return ROAD_COST;
    }

    /**
     * Executes getStopCost.
     */
    public int getStopCost() {
        return STOP_COST;
    }

    /**
     * Executes getTrafficLightCost.
     */
    public int getTrafficLightCost() {
        return TRAFFIC_LIGHT_COST;
    }

    /**
     * Executes getBusCost.
     */
    public int getBusCost() {
        return BUS_COST;
    }

    /**
     * Executes getTramCost.
     */
    public int getTramCost() {
        return TRAM_COST;
    }

    /**
     * Executes getDeliveryReward.
     */
    public int getDeliveryReward() {
        return DELIVERY_REWARD;
    }

    /**
     * Executes getEndReason.
     */
    public String getEndReason() {
        return endReason;
    }

    /**
     * Executes canAfford.
     */
    public boolean canAfford(int amount) {
        return amount >= 0 && money >= amount;
    }

    /**
     * Executes spendMoney.
     */
    public boolean spendMoney(int amount) {
        if (!canAfford(amount)) {
            return false;
        }
        money -= amount;
        evaluateEndConditions();
        return true;
    }

    /**
     * Executes getVehicles.
     */
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Executes clearVehicles.
     */
    public void clearVehicles() {
        vehicles.clear();
    }

    /**
     * Executes addVehicle.
     */
    public void addVehicle(Vehicle vehicle) {
        if (vehicle != null) {
            vehicles.add(vehicle);
        }
    }

    /**
     * Executes getRoutes.
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Executes getPassengerSystem.
     */
    public PassengerSystem getPassengerSystem() {
        return passengerSystem;
    }

    /**
     * Executes getTrafficLightSystem.
     */
    public TrafficLightSystem getTrafficLightSystem() {
        return trafficLightSystem;
    }

    /**
     * Executes getCityGrowthSystem.
     */
    public CityGrowthSystem getCityGrowthSystem() {
        return cityGrowthSystem;
    }

    /**
     * Executes clearRoutes.
     */
    public void clearRoutes() {
        routes.clear();
    }

    /**
     * Executes addRoute.
     */
    public boolean addRoute(Route route) {
        if (route == null || !route.isValid()) {
            return false;
        }
        routes.add(route);
        return true;
    }

    /**
     * Executes buyVehicle.
     */
    public boolean buyVehicle(VehicleType type, int cost, Route route, double tileSize) {
        if (type == null || route == null || !route.isValid()) {
            return false;
        }

        if (!spendMoney(cost)) {
            return false;
        }

        Vehicle vehicle = new Vehicle(type, 0, 0);
        vehicle.assignRoute(route, tileSize);
        vehicles.add(vehicle);
        return true;
    }

    /**
     * Executes start.
     */
    public void start() {
        gameState = GameState.RUNNING;
        endReason = null;
        gameClock.setGameSpeed(GameSpeed.NORMAL);
    }

    /**
     * Executes pause.
     */
    public void pause() {
        gameState = GameState.PAUSED;
        gameClock.setGameSpeed(GameSpeed.PAUSED);
    }

    /**
     * Executes resume.
     */
    public void resume() {
        gameState = GameState.RUNNING;
        if (gameClock.getGameSpeed() == GameSpeed.PAUSED) {
            gameClock.setGameSpeed(GameSpeed.NORMAL);
        }
    }

    /**
     * Executes stop.
     */
    public void stop() {
        gameState = GameState.STOPPED;
        gameClock.setGameSpeed(GameSpeed.PAUSED);
    }

    /**
     * Executes setSpeed.
     */
    public void setSpeed(GameSpeed speed) {
        gameClock.setGameSpeed(speed);
    }

    /**
     * Updates the simulation using the default tile size.
     *
     * @param deltaSeconds elapsed real time in seconds
     */
    public void update(double deltaSeconds) {
        update(deltaSeconds, 24);
    }

    /**
     * Updates simulation systems and vehicle movement.
     *
     * @param deltaSeconds elapsed real time in seconds
     * @param tileSize visual tile size for route movement interpolation
     */
    public void update(double deltaSeconds, double tileSize) {
        if (deltaSeconds <= 0.0 || tileSize <= 0.0) {
            return;
        }
        if (gameState != GameState.RUNNING) {
            return;
        }

        gameClock.update(deltaSeconds);

        double scaledDelta = deltaSeconds * gameClock.getGameSpeed().getMultiplier();
        trafficLightSystem.update(scaledDelta);
        cityGrowthSystem.update(scaledDelta, gameMap, routes, vehicles);
        passengerSystem.update(scaledDelta, gameMap, routes, cityGrowthSystem);

        for (Vehicle vehicle : vehicles) {
            int previousPathIndex = vehicle.getPathIndex();

            if (shouldWaitAtRedLight(vehicle)) {
                continue;
            }

            vehicle.update(scaledDelta, tileSize);

            if (vehicle.getPathIndex() != previousPathIndex) {
                processStopArrival(vehicle);
            }
        }

        evaluateEndConditions();
    }

    /**
     * Executes earnMoney.
     */
    public void earnMoney(int amount) {
        if (amount > 0) {
            money += amount;
            if ("LOSE".equals(endReason) && gameState == GameState.STOPPED) {
                endReason = null;
            }
        }
    }

    private void processStopArrival(Vehicle vehicle) {
        GridPos position = vehicle.getCurrentPathPosition();
        if (position == null) {
            return;
        }

        Stop stop = passengerSystem.findStopByPosition(position);
        if (stop == null) {
            return;
        }

        int deliveredCount = vehicle.dropOffPassengersAt(stop.getId());
        if (deliveredCount > 0) {
            earnMoney(deliveredCount * DELIVERY_REWARD);
        }

        int boardedCount = 0;
        int waitingToEvaluate = stop.getWaitingCount();
        while (vehicle.hasFreeSeat() && waitingToEvaluate > 0) {
            waitingToEvaluate--;
            Passenger waiting = stop.pollWaitingPassenger();
            if (waiting == null) {
                break;
            }

            if (!isPassengerCompatibleWithVehicle(waiting, vehicle)) {
                stop.addWaitingPassenger(waiting);
                continue;
            }

            if (!vehicle.boardPassenger(waiting)) {
                stop.addWaitingPassenger(waiting);
                break;
            }
            boardedCount++;
        }

        if (deliveredCount > 0 || boardedCount > 0) {
            cityGrowthSystem.registerTransportActivity(stop.getPosition(), boardedCount, deliveredCount);
        }
    }

    private boolean shouldWaitAtRedLight(Vehicle vehicle) {
        GridPos next = vehicle.getNextPathPosition();
        return next != null && trafficLightSystem.isRedAt(next);
    }

    private boolean isPassengerCompatibleWithVehicle(Passenger passenger, Vehicle vehicle) {
        if (passenger == null || vehicle == null || vehicle.getAssignedRoute() == null) {
            return false;
        }

        String destinationStopId = passenger.getDestinationStopId();
        if (destinationStopId == null || destinationStopId.isBlank()) {
            return false;
        }

        for (GridPos stopPos : vehicle.getAssignedRoute().getStops()) {
            if (destinationStopId.equals(stopId(stopPos))) {
                return true;
            }
        }

        return false;
    }

    private String stopId(GridPos pos) {
        return "S_" + pos.getRow() + "_" + pos.getCol();
    }

    private void evaluateEndConditions() {
        if ("WIN".equals(endReason)) {
            return;
        }

        if (money >= TARGET_MONEY) {
            endReason = "WIN";
            stop();
            return;
        }

        boolean cannotAffordAnyCoreAction = money < ROAD_COST
                && money < STOP_COST
                && money < TRAFFIC_LIGHT_COST
                && money < BUS_COST
                && money < TRAM_COST;

        if (cannotAffordAnyCoreAction && vehicles.isEmpty()) {
            endReason = "LOSE";
            stop();
        }
    }
}
