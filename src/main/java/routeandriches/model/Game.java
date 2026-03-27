package routeandriches.model;

import java.util.ArrayList;
import java.util.List;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.GameState;
import routeandriches.model.enums.VehicleType;
import routeandriches.system.GameClock;

public class Game {

    private static final int STARTING_MONEY = 1000;

    private GameState gameState;
    private final GameClock gameClock;
    private final GameMap gameMap;
    private int money;
    private final List<Vehicle> vehicles;
    private final List<Route> routes;

    public Game() {
        this.gameState = GameState.PAUSED;
        this.gameClock = new GameClock();
        this.gameMap = new GameMap(60, 90);
        this.money = STARTING_MONEY;
        this.vehicles = new ArrayList<>();
        this.routes = new ArrayList<>();
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
    }

    public boolean canAfford(int amount) {
        return amount >= 0 && money >= amount;
    }

    public boolean spendMoney(int amount) {
        if (!canAfford(amount)) {
            return false;
        }
        money -= amount;
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
        if (gameState == GameState.RUNNING) {
            gameClock.update(deltaSeconds);

            double scaledDelta = deltaSeconds * gameClock.getGameSpeed().getMultiplier();

            for (Vehicle vehicle : vehicles) {
                vehicle.update(scaledDelta, tileSize);
            }
        }
    }
}