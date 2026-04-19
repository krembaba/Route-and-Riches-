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
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import routeandriches.model.Game;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Route;
import routeandriches.model.Tile;
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
    private static final int BUS_REFUND = 50;
    private static final int TRAM_REFUND = 75;

    private InteractionMode interactionMode = InteractionMode.BUILD_ROAD;

    private final SaveService saveService = new SaveService();
    private final MapRenderer mapRenderer = new MapRenderer(24);
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();
    private final Game game = new Game();
    private final MinimapSystem minimapSystem = new MinimapSystem(60, 90);

    private Vehicle focusedVehicle;
    private Route activeRoute;
    private GridPos inspectedTile;
    private GridPos selectedRouteStop;
    private final List<GridPos> pendingRouteStops = new ArrayList<>();
    private int hoveredRow = -1;
    private int hoveredCol = -1;

    @FXML private Canvas mapCanvas;
    @FXML private Canvas minimapCanvas;
    @FXML private ScrollPane mapScrollPane;
    @FXML private Label stateLabel;
    @FXML private Label speedLabel;
    @FXML private Label timeLabel;
    @FXML private Label buildModeLabel;
    @FXML private Label hintLabel;
    @FXML private Button buildRoadButton;
    @FXML private Button removeRoadButton;
    @FXML private Button placeStopButton;
    @FXML private Button removeStopButton;
    @FXML private Button createRouteButton;
    @FXML private Button editRouteButton;
    @FXML private Button buyVehicleButton;
    @FXML private Button sellVehicleButton;
    @FXML private Button inspectButton;
    @FXML private ComboBox<String> vehicleTypeComboBox;

    @FXML private Label moneyLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label routeCountLabel;
    @FXML private Label stopCountLabel;
    @FXML private Label activeRouteLabel;
    @FXML private Label selectedStopLabel;
    @FXML private Label inspectTypeLabel;
    @FXML private Label inspectPositionLabel;
    @FXML private Label inspectDetailsLabel;
    @FXML private Button moveStopUpButton;
    @FXML private Button moveStopDownButton;
    @FXML private Button removeRouteStopButton;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;

    @FXML
    public void initialize() {
        if (vehicleTypeComboBox != null) {
            vehicleTypeComboBox.setItems(FXCollections.observableArrayList("Bus", "Tram", "Taxi (Coming Soon)"));
            vehicleTypeComboBox.getSelectionModel().selectFirst();
        }
        setupGameLoop();
        setupMouseInput();
        setupMinimapInput();
        updateInteractionModeUI();
        render();
        updateLabels();
        updateHud();
        updateInspector();
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
                updateInspector();
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

            Vehicle clickedVehicle = findVehicleNear(e.getX(), e.getY());
            GridPos clickedPos = new GridPos(row, col);

            switch (interactionMode) {
                case BUILD_ROAD -> {
                    boolean success = map.placeRoad(row, col);
                    hintLabel.setText(success ? "Road placed" : "Cannot build road here");
                    if (success) {
                        handleNetworkChange();
                    }
                }
                case REMOVE_ROAD -> {
                    boolean success = map.removeRoad(row, col);
                    hintLabel.setText(success ? "Road removed" : "Select a road tile to remove");
                    if (success) {
                        inspectedTile = clickedPos;
                        handleNetworkChange();
                    }
                }
                case PLACE_STOP -> {
                    boolean success = map.placeStop(row, col);
                    hintLabel.setText(success
                            ? "Stop placed"
                            : "Stops must be on buildable road-adjacent tiles");
                    if (success) {
                        inspectedTile = clickedPos;
                        handleNetworkChange();
                    }
                }
                case REMOVE_STOP -> {
                    boolean success = map.removeStop(row, col);
                    hintLabel.setText(success ? "Stop removed" : "Select a stop tile to remove");
                    if (success) {
                        inspectedTile = clickedPos;
                        handleNetworkChange();
                    }
                }
                case CREATE_ROUTE -> handleRouteSelection(row, col);
                case EDIT_ROUTE -> handleRouteEditClick(clickedPos);
                case BUY_VEHICLE -> attemptSelectedVehiclePurchase();
                case SELL_VEHICLE -> handleVehicleSell(clickedVehicle);
                case INSPECT, SELECT -> handleInspectClick(clickedVehicle, clickedPos);
            }

            updateHud();
            updateInspector();
            render();
        });
    }

    private void setupMinimapInput() {
        if (minimapCanvas == null || mapScrollPane == null) {
            return;
        }

        minimapCanvas.setOnMouseClicked(e -> {
            double canvasWidth = minimapCanvas.getWidth();
            double canvasHeight = minimapCanvas.getHeight();
            double mapWidth = mapCanvas.getWidth();
            double mapHeight = mapCanvas.getHeight();
            double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
            double viewportHeight = mapScrollPane.getViewportBounds().getHeight();

            if (mapWidth <= viewportWidth || mapHeight <= viewportHeight) {
                hintLabel.setText("Map already fits in the current viewport");
                return;
            }

            double centerX = (e.getX() / canvasWidth) * mapWidth;
            double centerY = (e.getY() / canvasHeight) * mapHeight;
            double left = clamp(centerX - viewportWidth / 2.0, 0, mapWidth - viewportWidth);
            double top = clamp(centerY - viewportHeight / 2.0, 0, mapHeight - viewportHeight);

            mapScrollPane.setHvalue(left / (mapWidth - viewportWidth));
            mapScrollPane.setVvalue(top / (mapHeight - viewportHeight));
            hintLabel.setText("Camera jumped to minimap location");
            render();
        });
    }

    private void handleInspectClick(Vehicle clickedVehicle, GridPos clickedPos) {
        if (clickedVehicle != null) {
            focusedVehicle = clickedVehicle;
            activeRoute = clickedVehicle.getAssignedRoute();
            selectedRouteStop = null;
            hintLabel.setText("Vehicle inspected");
            return;
        }

        inspectedTile = clickedPos;
        selectedRouteStop = isStopTile(clickedPos.getRow(), clickedPos.getCol()) ? clickedPos : null;
        Route routeAtStop = findRouteContainingStop(clickedPos);
        if (routeAtStop != null) {
            activeRoute = routeAtStop;
        }
        hintLabel.setText("Tile inspected");
    }

    private void handleVehicleSell(Vehicle clickedVehicle) {
        if (clickedVehicle == null) {
            hintLabel.setText("Click a vehicle to sell it");
            return;
        }

        int refund = clickedVehicle.getType() == VehicleType.TRAM ? TRAM_REFUND : BUS_REFUND;
        Route vehicleRoute = clickedVehicle.getAssignedRoute();
        if (game.removeVehicle(clickedVehicle)) {
            game.earnMoney(refund);
            focusedVehicle = null;
            if (vehicleRoute != null) {
                activeRoute = vehicleRoute;
            }
            hintLabel.setText(displayName(clickedVehicle.getType()) + " sold for refund " + refund);
        } else {
            hintLabel.setText("Could not sell selected vehicle");
        }
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
        selectedRouteStop = clickedStop;
        inspectedTile = clickedStop;

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
            selectedRouteStop = pendingRouteStops.get(0);
            inspectedTile = selectedRouteStop;
            hintLabel.setText(routeName + " created and set active");
            pendingRouteStops.clear();
        } else {
            hintLabel.setText("Could not create route");
        }
    }

    private void handleRouteEditClick(GridPos clickedPos) {
        if (!isStopTile(clickedPos.getRow(), clickedPos.getCol())) {
            hintLabel.setText("Click a stop to select or add it to the active route");
            return;
        }

        if (activeRoute == null) {
            Route route = findRouteContainingStop(clickedPos);
            if (route == null) {
                hintLabel.setText("Select a stop that belongs to a route first");
                return;
            }
            activeRoute = route;
            selectedRouteStop = clickedPos;
            inspectedTile = clickedPos;
            hintLabel.setText(activeRoute.getName() + " selected for editing");
            return;
        }

        if (activeRoute.containsStop(clickedPos)) {
            selectedRouteStop = clickedPos;
            inspectedTile = clickedPos;
            hintLabel.setText("Stop selected. Use move or remove controls to edit the route");
            return;
        }

        List<GridPos> newStops = new ArrayList<>(activeRoute.getStops());
        int insertIndex = selectedRouteStop != null ? newStops.indexOf(selectedRouteStop) + 1 : newStops.size();
        if (insertIndex < 0) {
            insertIndex = newStops.size();
        }
        newStops.add(insertIndex, clickedPos);

        if (applyRouteEdit(newStops, "Stop added to " + activeRoute.getName())) {
            selectedRouteStop = clickedPos;
            inspectedTile = clickedPos;
        }
    }

    @FXML
    private void handleMoveStopUp() {
        if (!ensureEditableRouteStop()) {
            return;
        }

        List<GridPos> newStops = new ArrayList<>(activeRoute.getStops());
        int index = newStops.indexOf(selectedRouteStop);
        if (index <= 0) {
            hintLabel.setText("Selected stop is already first in the route");
            return;
        }

        GridPos current = newStops.remove(index);
        newStops.add(index - 1, current);
        applyRouteEdit(newStops, "Stop moved earlier in " + activeRoute.getName());
    }

    @FXML
    private void handleMoveStopDown() {
        if (!ensureEditableRouteStop()) {
            return;
        }

        List<GridPos> newStops = new ArrayList<>(activeRoute.getStops());
        int index = newStops.indexOf(selectedRouteStop);
        if (index < 0 || index >= newStops.size() - 1) {
            hintLabel.setText("Selected stop is already last in the route");
            return;
        }

        GridPos current = newStops.remove(index);
        newStops.add(index + 1, current);
        applyRouteEdit(newStops, "Stop moved later in " + activeRoute.getName());
    }

    @FXML
    private void handleRemoveRouteStop() {
        if (!ensureEditableRouteStop()) {
            return;
        }

        List<GridPos> newStops = new ArrayList<>(activeRoute.getStops());
        if (newStops.size() <= 2) {
            hintLabel.setText("A route must keep at least two stops");
            return;
        }

        newStops.remove(selectedRouteStop);
        GridPos fallback = newStops.get(Math.max(0, newStops.size() - 1));
        if (applyRouteEdit(newStops, "Stop removed from " + activeRoute.getName())) {
            selectedRouteStop = fallback;
            inspectedTile = fallback;
        }
    }

    private boolean applyRouteEdit(List<GridPos> newStops, String successMessage) {
        List<GridPos> newPath = buildRoutePath(newStops);
        if (newPath.isEmpty()) {
            hintLabel.setText("Route update failed: selected stops are not road-connected");
            return false;
        }

        activeRoute.updateStopsAndPath(newStops, newPath);
        for (Vehicle vehicle : game.getVehicles()) {
            if (vehicle.getAssignedRoute() == activeRoute) {
                vehicle.assignRoute(activeRoute, mapRenderer.getTileSize());
            }
        }
        hintLabel.setText(successMessage);
        return true;
    }

    private boolean ensureEditableRouteStop() {
        if (activeRoute == null) {
            hintLabel.setText("Select a route first in Edit Route mode");
            return false;
        }
        if (selectedRouteStop == null || !activeRoute.containsStop(selectedRouteStop)) {
            hintLabel.setText("Select a stop that belongs to the active route");
            return false;
        }
        return true;
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

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

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
        return game.getGameMap().getTile(row, col).isStop();
    }

    @FXML
    private void handleBuildRoadMode() {
        interactionMode = InteractionMode.BUILD_ROAD;
        pendingRouteStops.clear();
        updateInteractionModeUI();
    }

    @FXML
    private void handleRemoveRoadMode() {
        interactionMode = InteractionMode.REMOVE_ROAD;
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
    private void handleRemoveStopMode() {
        interactionMode = InteractionMode.REMOVE_STOP;
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
    private void handleEditRouteMode() {
        interactionMode = InteractionMode.EDIT_ROUTE;
        pendingRouteStops.clear();
        updateInteractionModeUI();
    }

    @FXML
    private void handleBuyVehicleMode() {
        interactionMode = InteractionMode.BUY_VEHICLE;
        updateInteractionModeUI();
        attemptSelectedVehiclePurchase();
    }

    @FXML
    private void handleSellVehicleMode() {
        interactionMode = InteractionMode.SELL_VEHICLE;
        updateInteractionModeUI();
    }

    @FXML
    private void handleInspectMode() {
        interactionMode = InteractionMode.INSPECT;
        updateInteractionModeUI();
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
            inspectedTile = null;
            selectedRouteStop = activeRoute != null && !activeRoute.getStops().isEmpty() ? activeRoute.getStops().get(0) : null;
            pendingRouteStops.clear();
            hintLabel.setText("Game loaded");
            updateHud();
            updateInspector();
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

    private void attemptSelectedVehiclePurchase() {
        if (vehicleTypeComboBox == null) {
            return;
        }

        String selected = vehicleTypeComboBox.getValue();
        if (selected == null) {
            hintLabel.setText("Select a vehicle type first");
            return;
        }

        if (selected.startsWith("Taxi")) {
            hintLabel.setText("Taxi is a UI placeholder and is not implemented yet");
            return;
        }

        VehicleType type = "Tram".equals(selected) ? VehicleType.TRAM : VehicleType.BUS;
        int cost = type == VehicleType.TRAM ? TRAM_COST : BUS_COST;
        Route routeForVehicle = getRouteForPurchase();

        if (routeForVehicle == null) {
            hintLabel.setText("Create or select a route before buying a " + displayName(type));
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
        updateInspector();
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
        focusedVehicle = null;

        buildModeLabel.setText("Mode: " + switch (interactionMode) {
            case BUILD_ROAD -> "Build Road";
            case REMOVE_ROAD -> "Remove Road";
            case PLACE_STOP -> "Place Stop";
            case REMOVE_STOP -> "Remove Stop";
            case CREATE_ROUTE -> "Create Route";
            case EDIT_ROUTE -> "Edit Route";
            case BUY_VEHICLE -> "Buy Vehicle";
            case SELL_VEHICLE -> "Sell Vehicle";
            case INSPECT -> "Inspect";
            case SELECT -> "Select";
        });

        hintLabel.setText(switch (interactionMode) {
            case BUILD_ROAD -> "Roads can be placed only on buildable land";
            case REMOVE_ROAD -> "Click a road tile to remove it";
            case PLACE_STOP -> "Stops can be placed only next to existing roads";
            case REMOVE_STOP -> "Click a stop tile to remove it";
            case CREATE_ROUTE -> "Click stops to build a route. Click the first selected stop again to finish";
            case EDIT_ROUTE -> "Select a route stop, then add, remove or reorder stops";
            case BUY_VEHICLE -> "Choose a type and buy it on the active route";
            case SELL_VEHICLE -> "Click a vehicle on the map to sell it";
            case INSPECT -> "Click a tile, stop or vehicle to inspect it";
            case SELECT -> "Select objects on the map";
        });

        styleButton(buildRoadButton, interactionMode == InteractionMode.BUILD_ROAD);
        styleButton(removeRoadButton, interactionMode == InteractionMode.REMOVE_ROAD);
        styleButton(placeStopButton, interactionMode == InteractionMode.PLACE_STOP);
        styleButton(removeStopButton, interactionMode == InteractionMode.REMOVE_STOP);
        styleButton(createRouteButton, interactionMode == InteractionMode.CREATE_ROUTE);
        styleButton(editRouteButton, interactionMode == InteractionMode.EDIT_ROUTE);
        styleButton(buyVehicleButton, interactionMode == InteractionMode.BUY_VEHICLE);
        styleButton(sellVehicleButton, interactionMode == InteractionMode.SELL_VEHICLE);
        styleButton(inspectButton, interactionMode == InteractionMode.INSPECT);

        if (moveStopUpButton != null) {
            moveStopUpButton.setDisable(activeRoute == null || selectedRouteStop == null);
        }
        if (moveStopDownButton != null) {
            moveStopDownButton.setDisable(activeRoute == null || selectedRouteStop == null);
        }
        if (removeRouteStopButton != null) {
            removeRouteStopButton.setDisable(activeRoute == null || selectedRouteStop == null);
        }

        render();
    }

    private void styleButton(Button button, boolean selected) {
        if (button == null) {
            return;
        }
        button.setStyle(selected ? selectedButtonStyle() : normalButtonStyle());
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
        if (activeRouteLabel != null) {
            activeRouteLabel.setText("Active Route: " + (activeRoute != null ? activeRoute.getName() : "None"));
        }
        if (selectedStopLabel != null) {
            selectedStopLabel.setText("Selected Stop: " + (selectedRouteStop != null ? formatPos(selectedRouteStop) : "None"));
        }
    }

    private void updateInspector() {
        if (inspectTypeLabel == null || inspectPositionLabel == null || inspectDetailsLabel == null) {
            return;
        }

        if (focusedVehicle != null) {
            inspectTypeLabel.setText("Object: Vehicle (" + focusedVehicle.getType() + ")");
            inspectPositionLabel.setText(String.format("Position: %.0f, %.0f", focusedVehicle.getX(), focusedVehicle.getY()));
            inspectDetailsLabel.setText("Route: "
                    + (focusedVehicle.getAssignedRoute() != null ? focusedVehicle.getAssignedRoute().getName() : "None")
                    + "\nSpeed: " + String.format("%.1f", focusedVehicle.getSpeed())
                    + "\nRefund: " + (focusedVehicle.getType() == VehicleType.TRAM ? TRAM_REFUND : BUS_REFUND));
            return;
        }

        if (inspectedTile != null && game.getGameMap().isWithinBounds(inspectedTile.getRow(), inspectedTile.getCol())) {
            Tile tile = game.getGameMap().getTile(inspectedTile.getRow(), inspectedTile.getCol());
            inspectTypeLabel.setText("Object: " + tile.getType());
            inspectPositionLabel.setText("Position: " + formatPos(inspectedTile));
            Route routeAtStop = findRouteContainingStop(inspectedTile);
            inspectDetailsLabel.setText("Buildable: " + tile.isBuildable()
                    + "\nRoad Adjacent: " + game.getGameMap().isRoadAdjacent(inspectedTile.getRow(), inspectedTile.getCol())
                    + (tile.isStop() ? "\nRoute: " + (routeAtStop != null ? routeAtStop.getName() : "None") : ""));
            return;
        }

        inspectTypeLabel.setText("Object: None");
        inspectPositionLabel.setText("Position: -");
        inspectDetailsLabel.setText("Click Inspect, Sell Vehicle, or Edit Route actions to view details here.");
    }

    private int countStopsOnMap() {
        int count = 0;
        for (int row = 0; row < game.getGameMap().getRows(); row++) {
            for (int col = 0; col < game.getGameMap().getCols(); col++) {
                if (game.getGameMap().getTile(row, col).isStop()) {
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

    private Vehicle findVehicleNear(double mouseX, double mouseY) {
        double threshold = 9;
        for (Vehicle vehicle : game.getVehicles()) {
            double dx = vehicle.getX() - mouseX;
            double dy = vehicle.getY() - mouseY;
            if (Math.sqrt(dx * dx + dy * dy) <= threshold) {
                return vehicle;
            }
        }
        return null;
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
        gc.setLineDashes(dashed ? 10 : 0);
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
            for (GridPos stop : route.getStops()) {
                mapRenderer.drawSelectionMarker(gc, stop.getRow(), stop.getCol(),
                        route.equals(activeRoute) ? Color.rgb(255, 226, 115, 0.85) : Color.rgb(255, 165, 0, 0.45));
            }
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
                case REMOVE_ROAD -> game.getGameMap().getTile(hoveredRow, hoveredCol).isRoad();
                case REMOVE_STOP, CREATE_ROUTE, EDIT_ROUTE -> isStopTile(hoveredRow, hoveredCol);
                default -> false;
            };
            boolean isStopPreview = interactionMode == InteractionMode.PLACE_STOP;
            if (interactionMode == InteractionMode.BUILD_ROAD
                    || interactionMode == InteractionMode.PLACE_STOP
                    || interactionMode == InteractionMode.REMOVE_ROAD
                    || interactionMode == InteractionMode.REMOVE_STOP) {
                mapRenderer.drawPlacementPreview(gc, hoveredRow, hoveredCol, valid, isStopPreview);
            }
        }

        drawRoutes(gc);

        if (inspectedTile != null) {
            mapRenderer.drawSelectionMarker(gc, inspectedTile.getRow(), inspectedTile.getCol(), Color.CYAN);
        }
        if (selectedRouteStop != null) {
            mapRenderer.drawSelectionMarker(gc, selectedRouteStop.getRow(), selectedRouteStop.getCol(), Color.YELLOW);
        }

        for (Vehicle currentVehicle : game.getVehicles()) {
            drawVehicle(gc, currentVehicle);
        }
        if (focusedVehicle != null) {
            mapRenderer.drawVehicleSelection(gc, focusedVehicle.getX(), focusedVehicle.getY(), Color.CYAN);
        }

        Vehicle minimapVehicle = getFocusedVehicle();
        GraphicsContext minimapGc = minimapCanvas.getGraphicsContext2D();
        minimapRenderer.drawMinimap(minimapGc, game.getGameMap(), minimapSystem, minimapVehicle, mapRenderer.getTileSize());
        if (mapScrollPane != null) {
            double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
            double viewportHeight = mapScrollPane.getViewportBounds().getHeight();
            double contentWidth = mapCanvas.getWidth();
            double contentHeight = mapCanvas.getHeight();
            double left = mapScrollPane.getHvalue() * Math.max(0, contentWidth - viewportWidth);
            double top = mapScrollPane.getVvalue() * Math.max(0, contentHeight - viewportHeight);
            minimapRenderer.drawViewport(minimapGc, game.getGameMap(), left, top, viewportWidth, viewportHeight, mapRenderer.getTileSize());
        }
    }

    private void handleNetworkChange() {
        List<Route> invalidRoutes = new ArrayList<>();
        for (Route route : new ArrayList<>(game.getRoutes())) {
            boolean allStopsExist = route.getStops().stream().allMatch(stop -> isStopTile(stop.getRow(), stop.getCol()));
            if (!allStopsExist) {
                invalidRoutes.add(route);
                continue;
            }
            List<GridPos> newPath = buildRoutePath(route.getStops());
            if (newPath.isEmpty()) {
                invalidRoutes.add(route);
                continue;
            }
            route.updateStopsAndPath(route.getStops(), newPath);
            for (Vehicle vehicle : game.getVehicles()) {
                if (vehicle.getAssignedRoute() == route) {
                    vehicle.assignRoute(route, mapRenderer.getTileSize());
                }
            }
        }

        int removedVehicles = 0;
        for (Route invalidRoute : invalidRoutes) {
            for (Vehicle vehicle : new ArrayList<>(game.getVehicles())) {
                if (vehicle.getAssignedRoute() == invalidRoute) {
                    game.removeVehicle(vehicle);
                    removedVehicles++;
                    if (vehicle == focusedVehicle) {
                        focusedVehicle = null;
                    }
                }
            }
            game.removeRoute(invalidRoute);
            if (invalidRoute == activeRoute) {
                activeRoute = null;
                selectedRouteStop = null;
            }
        }

        if (!invalidRoutes.isEmpty()) {
            hintLabel.setText(hintLabel.getText() + ". Removed " + invalidRoutes.size() + " broken route(s) and " + removedVehicles + " vehicle(s)");
        }
    }

    private Route findRouteContainingStop(GridPos position) {
        if (position == null) {
            return null;
        }
        for (Route route : game.getRoutes()) {
            if (route.containsStop(position)) {
                return route;
            }
        }
        return null;
    }

    private String formatPos(GridPos pos) {
        return pos.getRow() + ", " + pos.getCol();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
