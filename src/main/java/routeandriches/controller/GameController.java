package routeandriches.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import routeandriches.model.Game;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Route;
import routeandriches.model.Vehicle;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.VehicleType;
import routeandriches.persistence.GameSnapshot;
import routeandriches.persistence.SaveService;
import routeandriches.system.MinimapSystem;
import routeandriches.ui.MapRenderer;
import routeandriches.ui.MinimapRenderer;

public class GameController {

    private static final int BUS_COST = 100;
    private static final int TRAM_COST = 150;

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

    @FXML private Canvas mapCanvas;
    @FXML private Canvas minimapCanvas;
    @FXML private Label stateLabel;
    @FXML private Label speedLabel;
    @FXML private Label timeLabel;
    @FXML private Label buildModeLabel;
    @FXML private Label hintLabel;
    @FXML private Button buildRoadButton;
    @FXML private Button placeStopButton;
    @FXML private Button createRouteButton;
    @FXML private Button spawnBusButton;
    @FXML private Button spawnTramButton;

    @FXML private Label moneyLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label routeCountLabel;
    @FXML private Label stopCountLabel;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;

    @FXML
    public void initialize() {
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

                double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                game.update(deltaSeconds, mapRenderer.getTileSize());

                render();
                updateLabels();
                updateHud();
            }
        };
        gameLoop.start();
    }

    private void setupMouseInput() {
        mapCanvas.setOnMouseMoved(e -> {
            hoveredCol = (int) (e.getX() / mapRenderer.getTileSize());
            hoveredRow = (int) (e.getY() / mapRenderer.getTileSize());
            render();
        });

        mapCanvas.setOnMouseExited(e -> {
            hoveredRow = -1;
            hoveredCol = -1;
            render();
        });

        mapCanvas.setOnMouseClicked(e -> {
            int col = (int) (e.getX() / mapRenderer.getTileSize());
            int row = (int) (e.getY() / mapRenderer.getTileSize());

            GameMap map = game.getGameMap();
            if (!map.isWithinBounds(row, col)) {
                return;
            }

            switch (interactionMode) {
                case BUILD_ROAD -> {
                    boolean success = map.placeRoad(row, col);
                    hintLabel.setText(success ? "Road placed" : "Cannot build road here");
                }

                case PLACE_STOP -> {
                    boolean success = map.placeStop(row, col);
                    hintLabel.setText(success
                            ? "Stop placed"
                            : "Stops must be on buildable road-adjacent tiles");
                }

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
        attemptPurchase(VehicleType.BUS, BUS_COST);
    }

    @FXML
    private void handleSpawnTramMode() {
        interactionMode = InteractionMode.SPAWN_TRAM;
        updateInteractionModeUI();
        attemptPurchase(VehicleType.TRAM, TRAM_COST);
    }

    @FXML
    private void handleSave() {
        try {
            GameSnapshot snapshot = new GameSnapshot();
            snapshot.setMoney(game.getMoney());
            snapshot.setElapsedSeconds(game.getGameClock().getElapsedSeconds());
            snapshot.setVehicleData(serializeVehicles());
            snapshot.setRouteData(serializeRoutes());

            saveService.save(snapshot, "savegame.json");
            hintLabel.setText("Game saved to savegame.json");
        } catch (Exception e) {
            hintLabel.setText("Save failed");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoad() {
        try {
            GameSnapshot snapshot = saveService.load("savegame.json");
            game.setMoney(snapshot.getMoney());
            restoreRoutes(snapshot.getRouteData());
            restoreVehicles(snapshot.getVehicleData());
            activeRoute = getRouteForPurchase();
            focusedVehicle = getFocusedVehicle();
            pendingRouteStops.clear();

            hintLabel.setText("Game loaded");
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
            case CREATE_ROUTE -> "Create Route";
            case SPAWN_BUS -> "Buy Bus";
            case SPAWN_TRAM -> "Buy Tram";
            case SELECT -> "Select";
        });

        hintLabel.setText(switch (interactionMode) {
            case BUILD_ROAD -> "Roads can be placed only on buildable land";
            case PLACE_STOP -> "Stops can be placed only next to existing roads";
            case CREATE_ROUTE -> "Click stops to build a route. Click the first selected stop again to finish";
            case SPAWN_BUS -> "Buy a bus for " + BUS_COST + " on the active route";
            case SPAWN_TRAM -> "Buy a tram for " + TRAM_COST + " on the active route";
            case SELECT -> "Select objects on the map";
        });

        if (buildRoadButton != null) {
            buildRoadButton.setStyle(interactionMode == InteractionMode.BUILD_ROAD
                    ? selectedButtonStyle() : normalButtonStyle());
        }

        if (placeStopButton != null) {
            placeStopButton.setStyle(interactionMode == InteractionMode.PLACE_STOP
                    ? selectedButtonStyle() : normalButtonStyle());
        }

        if (createRouteButton != null) {
            createRouteButton.setStyle(interactionMode == InteractionMode.CREATE_ROUTE
                    ? selectedButtonStyle() : normalButtonStyle());
        }

        if (spawnBusButton != null) {
            spawnBusButton.setStyle(interactionMode == InteractionMode.SPAWN_BUS
                    ? selectedButtonStyle() : normalButtonStyle());
        }

        if (spawnTramButton != null) {
            spawnTramButton.setStyle(interactionMode == InteractionMode.SPAWN_TRAM
                    ? selectedButtonStyle() : normalButtonStyle());
        }

        render();
    }

    private String selectedButtonStyle() {
        return "-fx-background-color: linear-gradient(#3fa9f5, #287dc9);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;";
    }

    private String normalButtonStyle() {
        return "-fx-background-color: linear-gradient(#ffffff, #dde7f2);"
                + "-fx-text-fill: #22313f; -fx-font-weight: bold; -fx-background-radius: 8;";
    }

    private void updateLabels() {
        stateLabel.setText("State: " + game.getGameState());
        speedLabel.setText("Speed: " + game.getGameClock().getGameSpeed());
        timeLabel.setText(String.format("Time: %.1f", game.getGameClock().getElapsedSeconds()));
    }

    private void updateHud() {
        if (moneyLabel != null) {
            moneyLabel.setText("Money: " + game.getMoney());
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
        Color fill = currentVehicle.getType() == VehicleType.TRAM
                ? Color.rgb(102, 204, 255, 0.95)
                : Color.rgb(253, 221, 92, 0.95);

        Color stroke = currentVehicle.getType() == VehicleType.TRAM
                ? Color.rgb(30, 90, 130, 0.8)
                : Color.rgb(90, 70, 20, 0.6);

        gc.setFill(fill);
        gc.fillOval(currentVehicle.getX() - 5, currentVehicle.getY() - 5, 10, 10);
        gc.setStroke(stroke);
        gc.strokeOval(currentVehicle.getX() - 5, currentVehicle.getY() - 5, 10, 10);
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
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        mapRenderer.drawMap(gc, game.getGameMap());

        if (hoveredRow >= 0 && hoveredCol >= 0 && game.getGameMap().isWithinBounds(hoveredRow, hoveredCol)) {
            boolean valid = switch (interactionMode) {
                case BUILD_ROAD -> game.getGameMap().canPlaceRoad(hoveredRow, hoveredCol);
                case PLACE_STOP -> game.getGameMap().canPlaceStop(hoveredRow, hoveredCol);
                case CREATE_ROUTE -> isStopTile(hoveredRow, hoveredCol);
                default -> false;
            };

            boolean isStopPreview = interactionMode == InteractionMode.PLACE_STOP;

            if (interactionMode == InteractionMode.BUILD_ROAD || interactionMode == InteractionMode.PLACE_STOP) {
                mapRenderer.drawPlacementPreview(gc, hoveredRow, hoveredCol, valid, isStopPreview);
            }
        }

        drawRoutes(gc);

        Vehicle minimapVehicle = getFocusedVehicle();
        GraphicsContext minimapGc = minimapCanvas.getGraphicsContext2D();
        minimapRenderer.drawMinimap(minimapGc, game.getGameMap(), minimapSystem, minimapVehicle);

        for (Vehicle currentVehicle : game.getVehicles()) {
            drawVehicle(gc, currentVehicle);
        }
    }
}