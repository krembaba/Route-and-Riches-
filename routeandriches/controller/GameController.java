package routeandriches.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.SnapshotParameters;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import routeandriches.model.DecorationType;
import routeandriches.model.Game;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Passenger;
import routeandriches.model.RoadShape;
import routeandriches.model.Route;
import routeandriches.model.Stop;
import routeandriches.model.TileType;
import routeandriches.model.TrafficLight;
import routeandriches.model.Vehicle;
import routeandriches.model.enums.GameState;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.VehicleType;
import routeandriches.persistence.GameSnapshot;
import routeandriches.persistence.SaveService;
import routeandriches.system.MinimapSystem;
import routeandriches.ui.MapRenderer;
import routeandriches.ui.MinimapRenderer;

public class GameController {

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final long FRAME_INTERVAL_NANOS = 33_000_000L; // ~30 FPS
    private static final long MINIMAP_INTERVAL_NANOS = 100_000_000L; // ~10 FPS

    private InteractionMode interactionMode = InteractionMode.BUILD_ROAD;

    private final SaveService saveService = new SaveService();
    private final MapRenderer mapRenderer = new MapRenderer(24);
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();
    private final Game game = new Game();
    private final MinimapSystem minimapSystem = new MinimapSystem(60, 90);

    private Vehicle focusedVehicle;
    private Route activeRoute;
    private final List<GridPos> pendingRouteStops = new ArrayList<>();
    private int hoveredRow = -1;
    private int hoveredCol = -1;
    private long lastMinimapRenderNanos = 0L;
    private boolean mapStaticDirty = true;
    private boolean minimapDirty = true;
    private Canvas mapStaticCanvas;
    private WritableImage mapStaticImage;

    @FXML private Canvas mapCanvas;
    @FXML private Canvas minimapCanvas;
    @FXML private ScrollPane mapScrollPane;
    @FXML private Label stateLabel;
    @FXML private Label speedLabel;
    @FXML private Label timeLabel;
    @FXML private Label buildModeLabel;
    @FXML private Label hintLabel;
    @FXML private Button buildRoadButton;
    @FXML private Button placeStopButton;
    @FXML private Button placeTrafficLightButton;
    @FXML private Button createRouteButton;
    @FXML private Button spawnBusButton;
    @FXML private Button spawnTramButton;

    @FXML private Label moneyLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label routeCountLabel;
    @FXML private Label stopCountLabel;
    @FXML private Label passengerWaitingLabel;
    @FXML private Label passengerOnboardLabel;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;

    @FXML
    public void initialize() {
        mapCanvas.setWidth(game.getGameMap().getCols() * mapRenderer.getTileSize());
        mapCanvas.setHeight(game.getGameMap().getRows() * mapRenderer.getTileSize());
        mapStaticCanvas = new Canvas(mapCanvas.getWidth(), mapCanvas.getHeight());
        mapStaticDirty = true;

        setupGameLoop();
        setupMouseInput();
        updateInteractionModeUI();
        render();
        updateLabels();
        updateHud();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    return;
                }

                long elapsedNanos = now - lastUpdate;
                if (elapsedNanos < FRAME_INTERVAL_NANOS) {
                    return;
                }

                double deltaSeconds = elapsedNanos / 1_000_000_000.0;
                lastUpdate = now;

                game.update(deltaSeconds, mapRenderer.getTileSize());

                render(now);
                updateLabels();
                updateHud();
            }
        };
        gameLoop.start();
    }

    private void setupMouseInput() {
        mapCanvas.setOnMouseMoved(e -> {
            GridPos pointerPos = resolveGridPositionFromEvent(e);
            if (pointerPos == null) {
                hoveredRow = -1;
                hoveredCol = -1;
            } else {
                hoveredRow = pointerPos.getRow();
                hoveredCol = pointerPos.getCol();
            }
            render();
        });

        mapCanvas.setOnMouseExited(e -> {
            hoveredRow = -1;
            hoveredCol = -1;
            render();
        });

        mapCanvas.setOnMouseClicked(e -> {
            GridPos pointerPos = resolveGridPositionFromEvent(e);
            if (pointerPos == null) {
                return;
            }

            int row = pointerPos.getRow();
            int col = pointerPos.getCol();

            GameMap map = game.getGameMap();
            if (!map.isWithinBounds(row, col)) {
                return;
            }

            switch (interactionMode) {
                case BUILD_ROAD -> {
                    int cost = game.getRoadCost();
                    if (!map.canPlaceRoad(row, col)) {
                        hintLabel.setText("Cannot build road here");
                    } else if (!game.spendMoney(cost)) {
                        hintLabel.setText("Need $" + cost + " to build a road");
                    } else {
                        map.placeRoad(row, col);
                        markMapStaticDirty();
                        hintLabel.setText("Road placed (-$" + cost + ")");
                    }
                }

                case PLACE_STOP -> {
                    int cost = game.getStopCost();
                    if (!map.canPlaceStop(row, col)) {
                        hintLabel.setText("Stops must be on buildable road-adjacent tiles");
                    } else if (!game.spendMoney(cost)) {
                        hintLabel.setText("Need $" + cost + " to place a stop");
                    } else {
                        map.placeStop(row, col);
                        markMapStaticDirty();
                        hintLabel.setText("Stop placed (-$" + cost + ")");
                    }
                }

                case PLACE_TRAFFIC_LIGHT -> handleTrafficLightInteraction(row, col, e.getButton());

                case CREATE_ROUTE -> {
                    handleRouteSelection(row, col);
                }

                case SPAWN_BUS -> {
                    hintLabel.setText("Click Buy Bus again to buy on the active route");
                }

                case SPAWN_TRAM -> {
                    hintLabel.setText("Click Buy Tram again to buy on the active route");
                }

                case SELECT -> {
                    hintLabel.setText("Selection mode");
                }
            }

            updateHud();
            render();
        });

        minimapCanvas.setOnMouseClicked(e -> {
            GridPos minimapTarget = resolveGridPositionFromMinimapEvent(e);
            if (minimapTarget == null) {
                return;
            }

            centerMainViewportOn(minimapTarget);
            hintLabel.setText("Jumped to district: " + minimapTarget.getRow() + ", " + minimapTarget.getCol());
        });
    }

    private GridPos resolveGridPositionFromEvent(MouseEvent event) {
        Point2D localPoint = mapCanvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        int col = (int) Math.floor(localPoint.getX() / mapRenderer.getTileSize());
        int row = (int) Math.floor(localPoint.getY() / mapRenderer.getTileSize());

        if (!game.getGameMap().isWithinBounds(row, col)) {
            return null;
        }

        return new GridPos(row, col);
    }

    private GridPos resolveGridPositionFromMinimapEvent(MouseEvent event) {
        if (minimapCanvas == null) {
            return null;
        }

        double miniWidth = minimapCanvas.getWidth();
        double miniHeight = minimapCanvas.getHeight();
        if (miniWidth <= 0 || miniHeight <= 0) {
            return null;
        }

        double x = clamp(event.getX(), 0, miniWidth - 0.0001);
        double y = clamp(event.getY(), 0, miniHeight - 0.0001);

        double miniTileWidth = minimapSystem.getMiniTileWidth(miniWidth);
        double miniTileHeight = minimapSystem.getMiniTileHeight(miniHeight);

        int col = (int) Math.floor(x / miniTileWidth);
        int row = (int) Math.floor(y / miniTileHeight);

        if (!game.getGameMap().isWithinBounds(row, col)) {
            return null;
        }

        return new GridPos(row, col);
    }

    private void centerMainViewportOn(GridPos target) {
        if (mapScrollPane == null || target == null) {
            return;
        }

        double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
        double viewportHeight = mapScrollPane.getViewportBounds().getHeight();
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        double tileSize = mapRenderer.getTileSize();
        double targetX = target.getCol() * tileSize + tileSize * 0.5;
        double targetY = target.getRow() * tileSize + tileSize * 0.5;

        double contentWidth = mapCanvas.getWidth();
        double contentHeight = mapCanvas.getHeight();

        double hDenominator = Math.max(1.0, contentWidth - viewportWidth);
        double vDenominator = Math.max(1.0, contentHeight - viewportHeight);

        double hValue = (targetX - viewportWidth * 0.5) / hDenominator;
        double vValue = (targetY - viewportHeight * 0.5) / vDenominator;

        mapScrollPane.setHvalue(clamp(hValue, 0, 1));
        mapScrollPane.setVvalue(clamp(vValue, 0, 1));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void handleRouteSelection(int row, int col) {
        if (!isStopTile(row, col)) {
            hintLabel.setText("Select stop tiles only when creating a route");
            return;
        }

        GridPos clickedStop = new GridPos(row, col);

        if (pendingRouteStops.contains(clickedStop)) {
            if (pendingRouteStops.size() >= 2 && clickedStop.equals(pendingRouteStops.get(0))) {
                createPendingRoute();
            } else {
                hintLabel.setText("Stop already selected. Click the first stop again to finish the route");
            }
            return;
        }

        pendingRouteStops.add(clickedStop);

        if (pendingRouteStops.size() == 1) {
            hintLabel.setText("First stop selected. Click another stop, then click the first stop again to finish");
        } else {
            hintLabel.setText("Stop added to route. Click the first selected stop again to create the route");
        }
    }

    private void createPendingRoute() {
        if (pendingRouteStops.size() < 2) {
            hintLabel.setText("A route needs at least two stops");
            return;
        }

        List<GridPos> routePath = buildRoutePath(pendingRouteStops);
        if (routePath.isEmpty()) {
            hintLabel.setText("No road-connected path between selected stops");
            return;
        }

        String routeName = "Route " + (game.getRoutes().size() + 1);
        Route route = new Route(routeName, new ArrayList<>(pendingRouteStops), routePath);

        if (game.addRoute(route)) {
            activeRoute = route;
            hintLabel.setText(routeName + " created and set active");
            pendingRouteStops.clear();
        } else {
            hintLabel.setText("Could not create route");
        }
    }

    private List<GridPos> buildRoutePath(List<GridPos> selectedStops) {
        List<GridPos> forwardPath = buildOneWayRoutePath(selectedStops);
        if (forwardPath.size() < 2) {
            return new ArrayList<>();
        }

        List<GridPos> loopPath = new ArrayList<>(forwardPath);

        for (int i = forwardPath.size() - 2; i > 0; i--) {
            loopPath.add(forwardPath.get(i));
        }

        return loopPath;
    }

    private List<GridPos> buildOneWayRoutePath(List<GridPos> selectedStops) {
        List<GridPos> fullPath = new ArrayList<>();

        for (int i = 0; i < selectedStops.size() - 1; i++) {
            GridPos start = selectedStops.get(i);
            GridPos end = selectedStops.get(i + 1);

            List<GridPos> segment = findRoadPath(start, end);
            if (segment.isEmpty()) {
                return new ArrayList<>();
            }

            if (!fullPath.isEmpty()) {
                segment = new ArrayList<>(segment.subList(1, segment.size()));
            }

            fullPath.addAll(segment);
        }

        return fullPath;
    }

    private List<GridPos> findRoadPath(GridPos start, GridPos end) {
        Queue<GridPos> queue = new ArrayDeque<>();
        Map<GridPos, GridPos> previous = new HashMap<>();
        Set<GridPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        int[][] directions = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        };

        while (!queue.isEmpty()) {
            GridPos current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(previous, end);
            }

            for (int[] dir : directions) {
                int newRow = current.getRow() + dir[0];
                int newCol = current.getCol() + dir[1];

                if (!game.getGameMap().isWithinBounds(newRow, newCol)) {
                    continue;
                }

                GridPos next = new GridPos(newRow, newCol);

                if (!next.equals(end) && !isDriveableForRoute(newRow, newCol)) {
                    continue;
                }

                if (visited.add(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<GridPos> reconstructPath(Map<GridPos, GridPos> previous, GridPos target) {
        List<GridPos> path = new ArrayList<>();
        GridPos current = target;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }

    private boolean isDriveableForRoute(int row, int col) {
        return game.getGameMap().isRoad(row, col) || isStopTile(row, col);
    }

    private boolean isStopTile(int row, int col) {
        return game.getGameMap().getTile(row, col).getType().name().equals("STOP");
    }

    @FXML
    private void handleBuildRoadMode() {
        interactionMode = InteractionMode.BUILD_ROAD;
        pendingRouteStops.clear();
        updateInteractionModeUI();
    }

    @FXML
    private void handlePlaceStopMode() {
        interactionMode = InteractionMode.PLACE_STOP;
        pendingRouteStops.clear();
        updateInteractionModeUI();
    }

    @FXML
    private void handleCreateRouteMode() {
        interactionMode = InteractionMode.CREATE_ROUTE;
        pendingRouteStops.clear();
        updateInteractionModeUI();
    }

    @FXML
    private void handleSpawnBusMode() {
        interactionMode = InteractionMode.SPAWN_BUS;
        updateInteractionModeUI();
        attemptPurchase(VehicleType.BUS, game.getBusCost());
    }

    @FXML
    private void handleSpawnTramMode() {
        interactionMode = InteractionMode.SPAWN_TRAM;
        updateInteractionModeUI();
        attemptPurchase(VehicleType.TRAM, game.getTramCost());
    }

    @FXML
    private void handlePlaceTrafficLightMode() {
        interactionMode = InteractionMode.PLACE_TRAFFIC_LIGHT;
        updateInteractionModeUI();
    }

    @FXML
    private void handleSave() {
        try {
            GameSnapshot snapshot = new GameSnapshot();
            snapshot.setMoney(game.getMoney());
            snapshot.setElapsedSeconds(game.getGameClock().getElapsedSeconds());
            snapshot.setGameSpeed(game.getGameClock().getGameSpeed().name());
            snapshot.setGameState(game.getGameState().name());
            snapshot.setMapData(serializeMap());
            snapshot.setVehicleData(serializeVehicles());
            snapshot.setRouteData(serializeRoutes());
            snapshot.setTrafficLightData(serializeTrafficLights());
            snapshot.setPassengerData(serializePassengerState());

            saveService.save(snapshot, "savegame.json");
            hintLabel.setText("Game saved to savegame.json");
        } catch (Exception e) {
            hintLabel.setText("Save failed");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoad() {
        loadGameFromFile("savegame.json");
    }

    public void loadGameFromFile(String filePath) {
        try {
            GameSnapshot snapshot = saveService.load(filePath);
            game.setMoney(snapshot.getMoney());
            game.getGameClock().setElapsedSeconds(snapshot.getElapsedSeconds());
            restoreMap(snapshot.getMapData());
            restoreRoutes(snapshot.getRouteData());
            restoreVehicles(snapshot.getVehicleData());
            restoreTrafficLights(snapshot.getTrafficLightData());
            restorePassengerState(snapshot.getPassengerData());
            restoreClockAndState(snapshot);
            activeRoute = getRouteForPurchase();
            focusedVehicle = getFocusedVehicle();
            pendingRouteStops.clear();

            hintLabel.setText("Game loaded from " + filePath);
            updateLabels();
            updateHud();
            render();
        } catch (Exception e) {
            hintLabel.setText("Load failed");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStart() {
        game.start();
        updateLabels();
    }

    @FXML
    private void handlePause() {
        game.pause();
        updateLabels();
    }

    @FXML
    private void handleNormalSpeed() {
        game.setSpeed(GameSpeed.NORMAL);
        game.resume();
        updateLabels();
    }

    @FXML
    private void handleFastSpeed() {
        game.setSpeed(GameSpeed.FAST);
        game.resume();
        updateLabels();
    }

    private void attemptPurchase(VehicleType type, int cost) {
        Route routeForVehicle = getRouteForPurchase();

        if (routeForVehicle == null) {
            hintLabel.setText("Create a route before buying a " + displayName(type));
            return;
        }

        boolean success = game.buyVehicle(type, cost, routeForVehicle, mapRenderer.getTileSize());

        if (success) {
            focusedVehicle = game.getVehicles().get(game.getVehicles().size() - 1);
            activeRoute = routeForVehicle;
            hintLabel.setText(displayName(type) + " bought for " + cost + " on " + routeForVehicle.getName());
        } else {
            hintLabel.setText("Not enough money to buy " + displayName(type) + " (" + cost + ")");
        }

        updateHud();
        render();
    }

    private Route getRouteForPurchase() {
        if (activeRoute != null && game.getRoutes().contains(activeRoute)) {
            return activeRoute;
        }

        if (game.getRoutes().isEmpty()) {
            return null;
        }

        return game.getRoutes().get(game.getRoutes().size() - 1);
    }

    private String displayName(VehicleType type) {
        return type == VehicleType.TRAM ? "tram" : "bus";
    }

    private void updateInteractionModeUI() {
        buildModeLabel.setText("Mode: " + switch (interactionMode) {
            case BUILD_ROAD -> "Build Road";
            case PLACE_STOP -> "Place Stop";
            case PLACE_TRAFFIC_LIGHT -> "Place Traffic Light";
            case CREATE_ROUTE -> "Create Route";
            case SPAWN_BUS -> "Buy Bus";
            case SPAWN_TRAM -> "Buy Tram";
            case SELECT -> "Select";
        });

        hintLabel.setText(switch (interactionMode) {
            case BUILD_ROAD -> "Roads can be placed only on buildable land (-$" + game.getRoadCost() + ")";
            case PLACE_STOP -> "Stops can be placed only next to existing roads (-$" + game.getStopCost() + ")";
            case PLACE_TRAFFIC_LIGHT -> "Click intersection to place/cycle light. Right-click to remove";
            case CREATE_ROUTE -> "Click stops to build a route. Click the first selected stop again to finish";
            case SPAWN_BUS -> "Buy a bus for $" + game.getBusCost() + " on the active route";
            case SPAWN_TRAM -> "Buy a tram for $" + game.getTramCost() + " on the active route";
            case SELECT -> "Select objects on the map";
        });

        updateModeButtonState(buildRoadButton, interactionMode == InteractionMode.BUILD_ROAD);
        updateModeButtonState(placeStopButton, interactionMode == InteractionMode.PLACE_STOP);
        updateModeButtonState(placeTrafficLightButton, interactionMode == InteractionMode.PLACE_TRAFFIC_LIGHT);
        updateModeButtonState(createRouteButton, interactionMode == InteractionMode.CREATE_ROUTE);
        updateModeButtonState(spawnBusButton, interactionMode == InteractionMode.SPAWN_BUS);
        updateModeButtonState(spawnTramButton, interactionMode == InteractionMode.SPAWN_TRAM);

        render();
    }

    private void updateModeButtonState(Button button, boolean active) {
        if (button == null) {
            return;
        }

        button.getStyleClass().remove("mode-button-active");
        if (active) {
            button.getStyleClass().add("mode-button-active");
        }
    }

    private void handleTrafficLightInteraction(int row, int col, MouseButton mouseButton) {
        if (!isValidTrafficLightTile(row, col)) {
            hintLabel.setText("Traffic lights can be placed only on road intersections");
            return;
        }

        GridPos position = new GridPos(row, col);
        TrafficLight existing = game.getTrafficLightSystem().getTrafficLightAt(position);

        if (existing != null) {
            if (mouseButton == MouseButton.SECONDARY) {
                game.getTrafficLightSystem().removeTrafficLightAt(position);
                hintLabel.setText("Traffic light removed");
            } else {
                cycleTrafficLightTiming(existing);
                hintLabel.setText("Light timing set to G:"
                        + formatSeconds(existing.getGreenDuration())
                        + "s / R:" + formatSeconds(existing.getRedDuration()) + "s");
            }
            return;
        }

        int cost = game.getTrafficLightCost();
        if (!game.spendMoney(cost)) {
            hintLabel.setText("Need $" + cost + " to place a traffic light");
            return;
        }

        game.getTrafficLightSystem().addTrafficLight(new TrafficLight(position));
        hintLabel.setText("Traffic light placed (-$" + cost + ")");
    }

    private boolean isValidTrafficLightTile(int row, int col) {
        if (!game.getGameMap().isRoad(row, col)) {
            return false;
        }

        RoadShape shape = game.getGameMap().getTile(row, col).getRoadShape();
        return shape == RoadShape.CROSS
                || shape == RoadShape.T_UP
                || shape == RoadShape.T_DOWN
                || shape == RoadShape.T_LEFT
                || shape == RoadShape.T_RIGHT;
    }

    private void cycleTrafficLightTiming(TrafficLight light) {
        double[][] presets = {
                {3.0, 3.0},
                {4.5, 2.5},
                {5.5, 3.5}
        };

        int currentIndex = 0;
        for (int i = 0; i < presets.length; i++) {
            if (Math.abs(light.getGreenDuration() - presets[i][0]) < 0.05
                    && Math.abs(light.getRedDuration() - presets[i][1]) < 0.05) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % presets.length;
        light.setGreenDuration(presets[nextIndex][0]);
        light.setRedDuration(presets[nextIndex][1]);
        light.setStateTimer(0.0);
    }

    private String formatSeconds(double seconds) {
        return String.format(Locale.US, "%.1f", seconds);
    }

    private void updateLabels() {
        stateLabel.setText("State: " + game.getGameState());
        speedLabel.setText("Speed: " + game.getGameClock().getGameSpeed());
        timeLabel.setText(String.format("Time: %.1f", game.getGameClock().getElapsedSeconds()));
    }

    private void updateHud() {
        if (moneyLabel != null) {
            moneyLabel.setText("City Treasury: $" + MONEY_FORMAT.format(game.getMoney()));
        }

        if (vehicleCountLabel != null) {
            vehicleCountLabel.setText("Vehicles: " + game.getVehicles().size());
        }

        if (routeCountLabel != null) {
            routeCountLabel.setText("Routes: " + game.getRoutes().size());
        }

        if (stopCountLabel != null) {
            stopCountLabel.setText("Stops: " + countStopsOnMap());
        }

        if (passengerWaitingLabel != null) {
            passengerWaitingLabel.setText("Waiting: " + game.getPassengerSystem().getTotalWaitingPassengers());
        }

        if (passengerOnboardLabel != null) {
            passengerOnboardLabel.setText("Onboard: " + getTotalOnboardPassengers());
        }
    }

    private int countStopsOnMap() {
        int count = 0;

        for (int row = 0; row < game.getGameMap().getRows(); row++) {
            for (int col = 0; col < game.getGameMap().getCols(); col++) {
                if (game.getGameMap().getTile(row, col).getType().name().equals("STOP")) {
                    count++;
                }
            }
        }

        return count;
    }

    private List<String> serializeMap() {
        List<String> mapData = new ArrayList<>();

        for (int row = 0; row < game.getGameMap().getRows(); row++) {
            for (int col = 0; col < game.getGameMap().getCols(); col++) {
                var tile = game.getGameMap().getTile(row, col);
                mapData.add(row + "|" + col
                        + "|" + tile.getType().name()
                        + "|" + tile.isBuildable()
                        + "|" + tile.getDecorationType().name()
                        + "|" + tile.getVisualVariant());
            }
        }

        return mapData;
    }

    private void restoreMap(List<?> mapData) {
        GameMap map = game.getGameMap();

        if (mapData == null) {
            return;
        }

        for (Object item : mapData) {
            String[] parts = String.valueOf(item).split("\\|", -1);
            if (parts.length < 6) {
                continue;
            }

            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                TileType type = TileType.valueOf(parts[2]);
                boolean buildable = Boolean.parseBoolean(parts[3]);
                DecorationType decoration = DecorationType.valueOf(parts[4]);
                int visualVariant = Integer.parseInt(parts[5]);

                if (!map.isWithinBounds(row, col)) {
                    continue;
                }

                map.setTile(row, col, type, buildable, decoration, visualVariant);
            } catch (Exception ignored) {
                // Skip malformed map entries.
            }
        }

        map.refreshRoadShapes();
        markMapStaticDirty();
    }

    private List<String> serializeVehicles() {
        List<String> vehicleData = new ArrayList<>();
        for (Vehicle currentVehicle : game.getVehicles()) {
            vehicleData.add(currentVehicle.toSaveString());
        }
        return vehicleData;
    }

    private void restoreVehicles(List<?> vehicleData) {
        game.clearVehicles();

        if (vehicleData == null) {
            return;
        }

        for (Object item : vehicleData) {
            Vehicle loadedVehicle = Vehicle.fromSaveString(String.valueOf(item), game.getRoutes());
            if (loadedVehicle != null) {
                game.addVehicle(loadedVehicle);
            }
        }
    }

    private List<String> serializeRoutes() {
        List<String> routeData = new ArrayList<>();
        for (Route route : game.getRoutes()) {
            routeData.add(route.toSaveString());
        }
        return routeData;
    }

    private void restoreRoutes(List<?> routeData) {
        game.clearRoutes();

        if (routeData == null) {
            return;
        }

        for (Object item : routeData) {
            Route route = Route.fromSaveString(String.valueOf(item));
            if (route != null) {
                game.addRoute(route);
            }
        }
    }

    private List<String> serializeTrafficLights() {
        List<String> data = new ArrayList<>();
        for (TrafficLight light : game.getTrafficLightSystem().getTrafficLights()) {
            GridPos pos = light.getPosition();
            data.add(pos.getRow() + "|" + pos.getCol()
                    + "|" + light.getState().name()
                    + "|" + light.getGreenDuration()
                    + "|" + light.getRedDuration()
                    + "|" + light.getStateTimer());
        }
        return data;
    }

    private void restoreTrafficLights(List<?> trafficLightData) {
        game.getTrafficLightSystem().clear();

        if (trafficLightData == null) {
            return;
        }

        for (Object item : trafficLightData) {
            String[] parts = String.valueOf(item).split("\\|", -1);
            if (parts.length < 6) {
                continue;
            }

            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                if (!game.getGameMap().isWithinBounds(row, col) || !game.getGameMap().isRoad(row, col)) {
                    continue;
                }

                TrafficLight light = new TrafficLight(
                        new GridPos(row, col),
                        routeandriches.model.TrafficLightState.valueOf(parts[2]),
                        Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]));
                light.setStateTimer(Double.parseDouble(parts[5]));
                game.getTrafficLightSystem().addTrafficLight(light);
            } catch (Exception ignored) {
                // Skip malformed traffic light entries.
            }
        }
    }

    private List<String> serializePassengerState() {
        List<String> data = new ArrayList<>();

        for (routeandriches.model.Stop stop : game.getPassengerSystem().getStops()) {
            for (Passenger passenger : stop.getWaitingPassengers()) {
                data.add("W|" + stop.getId()
                        + "|" + passenger.getId()
                        + "|" + passenger.getOriginStopId()
                        + "|" + passenger.getDestinationStopId());
            }
        }

        List<Vehicle> vehicles = game.getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);
            for (Passenger passenger : vehicle.getOnboardPassengers()) {
                data.add("V|" + i
                        + "|" + passenger.getId()
                        + "|" + passenger.getOriginStopId()
                        + "|" + passenger.getDestinationStopId());
            }
        }

        return data;
    }

    private void restorePassengerState(List<?> passengerData) {
        game.getPassengerSystem().synchronizeStops(game.getGameMap(), game.getRoutes());
        game.getPassengerSystem().clearWaitingPassengers();
        for (Vehicle vehicle : game.getVehicles()) {
            vehicle.clearOnboardPassengers();
        }

        if (passengerData == null) {
            return;
        }

        int maxPassengerNumber = 0;

        for (Object item : passengerData) {
            String[] parts = String.valueOf(item).split("\\|", -1);
            if (parts.length < 5) {
                continue;
            }

            String kind = parts[0];
            String passengerId = parts[2];
            String origin = parts[3];
            String destination = parts[4];

            try {
                Passenger passenger = new Passenger(passengerId, origin, destination);
                maxPassengerNumber = Math.max(maxPassengerNumber, parsePassengerIndex(passengerId));

                if ("W".equals(kind)) {
                    String stopId = parts[1];
                    game.getPassengerSystem().addWaitingPassengerToStop(stopId, passenger);
                } else if ("V".equals(kind)) {
                    int vehicleIndex = Integer.parseInt(parts[1]);
                    if (vehicleIndex >= 0 && vehicleIndex < game.getVehicles().size()) {
                        game.getVehicles().get(vehicleIndex).restoreOnboardPassenger(passenger);
                    }
                }
            } catch (Exception ignored) {
                // Skip malformed passenger entries.
            }
        }

        game.getPassengerSystem().setPassengerSequence(maxPassengerNumber + 1);
    }

    private int parsePassengerIndex(String passengerId) {
        if (passengerId == null || passengerId.length() < 2 || passengerId.charAt(0) != 'P') {
            return 0;
        }

        try {
            return Integer.parseInt(passengerId.substring(1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void restoreClockAndState(GameSnapshot snapshot) {
        try {
            GameSpeed speed = GameSpeed.valueOf(snapshot.getGameSpeed());
            game.setSpeed(speed);
        } catch (Exception ignored) {
            game.setSpeed(GameSpeed.PAUSED);
        }

        try {
            GameState state = GameState.valueOf(snapshot.getGameState());
            switch (state) {
                case RUNNING -> game.resume();
                case PAUSED -> game.pause();
                case STOPPED -> game.stop();
                default -> game.pause();
            }
        } catch (Exception ignored) {
            game.pause();
        }
    }

    private Vehicle getFocusedVehicle() {
        if (focusedVehicle != null && game.getVehicles().contains(focusedVehicle)) {
            return focusedVehicle;
        }

        if (game.getVehicles().isEmpty()) {
            return null;
        }

        return game.getVehicles().get(0);
    }

    private void drawVehicle(GraphicsContext gc, Vehicle currentVehicle) {
        double heading = calculateVehicleHeading(currentVehicle);

        gc.save();
        gc.translate(currentVehicle.getX(), currentVehicle.getY());
        gc.rotate(heading);

        if (currentVehicle.getType() == VehicleType.TRAM) {
            drawTramSprite(gc);
        } else {
            drawBusSprite(gc);
        }

        gc.restore();
    }

    private double calculateVehicleHeading(Vehicle vehicle) {
        GridPos next = vehicle.getNextPathPosition();
        if (next == null) {
            return 0.0;
        }

        double tx = centerX(next);
        double ty = centerY(next);
        double dx = tx - vehicle.getX();
        double dy = ty - vehicle.getY();

        if (Math.abs(dx) < 0.001 && Math.abs(dy) < 0.001) {
            return 0.0;
        }

        return Math.toDegrees(Math.atan2(dy, dx));
    }

    private void drawBusSprite(GraphicsContext gc) {
        double w = 18;
        double h = 10;

        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillRoundRect(-w / 2.0 + 1.0, -h / 2.0 + 1.0, w, h, 4, 4);

        gc.setFill(Color.web("#f6c94f"));
        gc.fillRoundRect(-w / 2.0, -h / 2.0, w, h, 4, 4);
        gc.setStroke(Color.web("#8c6a1e"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(-w / 2.0, -h / 2.0, w, h, 4, 4);

        gc.setFill(Color.web("#2d3b4a"));
        gc.fillRoundRect(-w / 2.0 + 3, -h / 2.0 + 1.8, w - 6, h - 3.6, 2.5, 2.5);
        gc.setFill(Color.rgb(196, 227, 255, 0.85));
        gc.fillRoundRect(-w / 2.0 + 4, -h / 2.0 + 2.4, w - 8, h - 4.8, 2, 2);

        gc.setFill(Color.web("#3f3f3f"));
        gc.fillOval(-w / 2.0 + 2.2, -h / 2.0 + 0.6, 2.5, 2.5);
        gc.fillOval(-w / 2.0 + 2.2, h / 2.0 - 3.1, 2.5, 2.5);
        gc.fillOval(w / 2.0 - 4.7, -h / 2.0 + 0.6, 2.5, 2.5);
        gc.fillOval(w / 2.0 - 4.7, h / 2.0 - 3.1, 2.5, 2.5);
    }

    private void drawTramSprite(GraphicsContext gc) {
        double w = 22;
        double h = 10;

        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillRoundRect(-w / 2.0 + 1.0, -h / 2.0 + 1.0, w, h, 5, 5);

        gc.setFill(Color.web("#51b4ff"));
        gc.fillRoundRect(-w / 2.0, -h / 2.0, w, h, 5, 5);
        gc.setStroke(Color.web("#1f6799"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(-w / 2.0, -h / 2.0, w, h, 5, 5);

        gc.setFill(Color.web("#f2f8ff"));
        gc.fillRoundRect(-w / 2.0 + 3, -h / 2.0 + 2, w - 6, h - 4, 3, 3);

        gc.setFill(Color.web("#d93444"));
        gc.fillRect(-w / 2.0 + 2.5, -0.8, w - 5, 1.6);

        gc.setStroke(Color.web("#a8cde8"));
        gc.setLineWidth(0.8);
        for (int i = -7; i <= 7; i += 4) {
            gc.strokeLine(i, -h / 2.0 + 2, i, h / 2.0 - 2);
        }
    }

    private void drawPath(GraphicsContext gc, List<GridPos> path, Color color, boolean dashed) {
        if (path.size() < 2) {
            return;
        }

        gc.setStroke(color);
        gc.setLineWidth(4);

        if (dashed) {
            gc.setLineDashes(10);
        } else {
            gc.setLineDashes(0);
        }

        for (int i = 0; i < path.size() - 1; i++) {
            GridPos a = path.get(i);
            GridPos b = path.get(i + 1);
            gc.strokeLine(centerX(a), centerY(a), centerX(b), centerY(b));
        }

        gc.setLineDashes(0);
    }

    private void drawRoutes(GraphicsContext gc) {
        for (Route route : game.getRoutes()) {
            Color routeColor = route.equals(activeRoute) ? Color.GOLD : Color.ORANGE;
            drawPath(gc, route.getPath(), routeColor, false);
        }

        if (pendingRouteStops.size() >= 2) {
            List<GridPos> previewPath = buildOneWayRoutePath(pendingRouteStops);
            drawPath(gc, previewPath, Color.YELLOW, true);
        }
    }

    private double centerX(GridPos pos) {
        return pos.getCol() * mapRenderer.getTileSize() + mapRenderer.getTileSize() / 2.0;
    }

    private double centerY(GridPos pos) {
        return pos.getRow() * mapRenderer.getTileSize() + mapRenderer.getTileSize() / 2.0;
    }

    private void render() {
        render(System.nanoTime());
    }

    private void render(long nowNanos) {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        rebuildMapStaticLayerIfNeeded();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        if (mapStaticImage != null) {
            gc.drawImage(mapStaticImage, 0, 0);
        } else {
            mapRenderer.drawMap(gc, game.getGameMap());
        }

        if (hoveredRow >= 0 && hoveredCol >= 0 && game.getGameMap().isWithinBounds(hoveredRow, hoveredCol)) {
            boolean valid = switch (interactionMode) {
                case BUILD_ROAD -> game.getGameMap().canPlaceRoad(hoveredRow, hoveredCol);
                case PLACE_STOP -> game.getGameMap().canPlaceStop(hoveredRow, hoveredCol);
                case PLACE_TRAFFIC_LIGHT -> isValidTrafficLightTile(hoveredRow, hoveredCol);
                case CREATE_ROUTE -> isStopTile(hoveredRow, hoveredCol);
                default -> false;
            };

            boolean isStopPreview = interactionMode == InteractionMode.PLACE_STOP;

            if (interactionMode == InteractionMode.BUILD_ROAD || interactionMode == InteractionMode.PLACE_STOP) {
                mapRenderer.drawPlacementPreview(gc, hoveredRow, hoveredCol, valid, isStopPreview);
            } else if (interactionMode == InteractionMode.PLACE_TRAFFIC_LIGHT) {
                mapRenderer.drawPlacementPreview(gc, hoveredRow, hoveredCol, valid, false);
            }
        }

        drawTrafficLights(gc);
        drawRoutes(gc);
        drawPassengerIndicators(gc);

        if (lastMinimapRenderNanos == 0L
                || nowNanos - lastMinimapRenderNanos >= MINIMAP_INTERVAL_NANOS
                || minimapDirty) {
            Vehicle minimapVehicle = getFocusedVehicle();
            GraphicsContext minimapGc = minimapCanvas.getGraphicsContext2D();
            minimapRenderer.drawMinimap(minimapGc, game.getGameMap(), minimapSystem, game.getVehicles(), minimapVehicle);
            lastMinimapRenderNanos = nowNanos;
            minimapDirty = false;
        }

        for (Vehicle currentVehicle : game.getVehicles()) {
            drawVehicle(gc, currentVehicle);
        }
    }

    private void markMapStaticDirty() {
        mapStaticDirty = true;
        minimapDirty = true;
    }

    private void rebuildMapStaticLayerIfNeeded() {
        if (!mapStaticDirty) {
            return;
        }

        if (mapStaticCanvas == null) {
            mapStaticCanvas = new Canvas(mapCanvas.getWidth(), mapCanvas.getHeight());
        }

        if (mapStaticCanvas.getWidth() != mapCanvas.getWidth()
                || mapStaticCanvas.getHeight() != mapCanvas.getHeight()) {
            mapStaticCanvas.setWidth(mapCanvas.getWidth());
            mapStaticCanvas.setHeight(mapCanvas.getHeight());
        }

        GraphicsContext staticGc = mapStaticCanvas.getGraphicsContext2D();
        mapRenderer.drawMap(staticGc, game.getGameMap());
        mapStaticImage = mapStaticCanvas.snapshot(new SnapshotParameters(), mapStaticImage);
        mapStaticDirty = false;
    }

    private int getTotalOnboardPassengers() {
        int total = 0;
        for (Vehicle vehicle : game.getVehicles()) {
            total += vehicle.getOnboardCount();
        }
        return total;
    }

    private void drawPassengerIndicators(GraphicsContext gc) {
        for (Stop stop : game.getPassengerSystem().getStops()) {
            int waitingCount = stop.getWaitingCount();
            if (waitingCount <= 0) {
                continue;
            }

            GridPos position = stop.getPosition();
            double tileX = position.getCol() * mapRenderer.getTileSize();
            double tileY = position.getRow() * mapRenderer.getTileSize();
            double badgeX = tileX + mapRenderer.getTileSize() * 0.56;
            double badgeY = tileY + mapRenderer.getTileSize() * 0.04;
            double badgeWidth = waitingCount >= 10 ? 15 : 12;
            double badgeHeight = 10;

            gc.setFill(Color.rgb(0, 0, 0, 0.35));
            gc.fillRoundRect(badgeX + 0.8, badgeY + 0.8, badgeWidth, badgeHeight, 8, 8);
            gc.setFill(Color.web("#f28b45"));
            gc.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);
            gc.setStroke(Color.web("#6d3512"));
            gc.setLineWidth(0.8);
            gc.strokeRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 8));
            String text = waitingCount > 99 ? "99+" : Integer.toString(waitingCount);
            gc.fillText(text, badgeX + 2.2, badgeY + 7.8);
        }
    }

    private void drawTrafficLights(GraphicsContext gc) {
        for (TrafficLight light : game.getTrafficLightSystem().getTrafficLights()) {
            GridPos pos = light.getPosition();
            double x = pos.getCol() * mapRenderer.getTileSize();
            double y = pos.getRow() * mapRenderer.getTileSize();

            gc.setFill(Color.rgb(0, 0, 0, 0.25));
            gc.fillRoundRect(x + 7, y + 5, 10, 14, 4, 4);
            gc.setFill(Color.web("#1f1f22"));
            gc.fillRoundRect(x + 6, y + 4, 10, 14, 4, 4);

            gc.setFill(light.isRed() ? Color.web("#ff4f5f") : Color.web("#48d47e"));
            gc.fillOval(x + 8.7, y + (light.isRed() ? 6.2 : 11.2), 4.8, 4.8);
        }
    }
}
