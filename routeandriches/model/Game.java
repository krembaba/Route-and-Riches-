package routeandriches.model;

import java.util.ArrayList;
import java.util.List;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.GameState;
import routeandriches.model.enums.VehicleType;
import routeandriches.system.GameClock;
import routeandriches.system.PassengerSystem;
import routeandriches.system.TrafficLightSystem;

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
    private String endReason;

    public Game() {
        this.gameState = GameState.PAUSED;
        this.gameClock = new GameClock();
        this.gameMap = new GameMap(60, 90);
        this.money = STARTING_MONEY;
        this.vehicles = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.passengerSystem = new PassengerSystem();
        this.trafficLightSystem = new TrafficLightSystem();
        this.endReason = null;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameClock getGameClock() {
        return gameClock;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = Math.max(0, money);
        evaluateEndConditions();
    }

    public int getTargetMoney() {
        return TARGET_MONEY;
    }

    public int getRoadCost() {
        return ROAD_COST;
    }

    public int getStopCost() {
        return STOP_COST;
    }

    public int getTrafficLightCost() {
        return TRAFFIC_LIGHT_COST;
    }

    public int getBusCost() {
        return BUS_COST;
    }

    public int getTramCost() {
        return TRAM_COST;
    }

    public int getDeliveryReward() {
        return DELIVERY_REWARD;
    }

    public String getEndReason() {
        return endReason;
    }

    public boolean canAfford(int amount) {
        return amount >= 0 && money >= amount;
    }

    public boolean spendMoney(int amount) {
        if (!canAfford(amount)) {
            return false;
        }
        money -= amount;
        evaluateEndConditions();
        return true;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void clearVehicles() {
        vehicles.clear();
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle != null) {
            vehicles.add(vehicle);
        }
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public PassengerSystem getPassengerSystem() {
        return passengerSystem;
    }

    public TrafficLightSystem getTrafficLightSystem() {
        return trafficLightSystem;
    }

    public void clearRoutes() {
        routes.clear();
    }

    public boolean addRoute(Route route) {
        if (route == null || !route.isValid()) {
            return false;
        }
        routes.add(route);
        return true;
    }

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

    public void start() {
        gameState = GameState.RUNNING;
        endReason = null;
        gameClock.setGameSpeed(GameSpeed.NORMAL);
    }

    public void pause() {
        gameState = GameState.PAUSED;
        gameClock.setGameSpeed(GameSpeed.PAUSED);
    }

    public void resume() {
        gameState = GameState.RUNNING;
        if (gameClock.getGameSpeed() == GameSpeed.PAUSED) {
            gameClock.setGameSpeed(GameSpeed.NORMAL);
        }
    }

    public void stop() {
        gameState = GameState.STOPPED;
        gameClock.setGameSpeed(GameSpeed.PAUSED);
    }

    public void setSpeed(GameSpeed speed) {
        gameClock.setGameSpeed(speed);
    }

    public void update(double deltaSeconds) {
        update(deltaSeconds, 24);
    }

    public void update(double deltaSeconds, double tileSize) {
        if (gameState != GameState.RUNNING) {
            return;
        }

        gameClock.update(deltaSeconds);

        double scaledDelta = deltaSeconds * gameClock.getGameSpeed().getMultiplier();
        trafficLightSystem.update(scaledDelta);
        passengerSystem.update(scaledDelta, gameMap, routes);

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

        while (vehicle.hasFreeSeat()) {
            Passenger waiting = stop.pollWaitingPassenger();
            if (waiting == null) {
                break;
            }

            if (!vehicle.boardPassenger(waiting)) {
                stop.addWaitingPassenger(waiting);
                break;
            }
        }
    }

    private boolean shouldWaitAtRedLight(Vehicle vehicle) {
        GridPos next = vehicle.getNextPathPosition();
        return next != null && trafficLightSystem.isRedAt(next);
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
