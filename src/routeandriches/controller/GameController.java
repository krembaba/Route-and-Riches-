package routeandriches.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import routeandriches.model.Game;
import routeandriches.model.GameMap;
import routeandriches.model.Vehicle;
import routeandriches.model.enums.GameSpeed;
import routeandriches.persistence.GameSnapshot;
import routeandriches.persistence.SaveService;
import routeandriches.system.MinimapSystem;
import routeandriches.ui.MapRenderer;
import routeandriches.ui.MinimapRenderer;

public class GameController {

    private InteractionMode interactionMode = InteractionMode.BUILD_ROAD;

    private final SaveService saveService = new SaveService();
    private final MapRenderer mapRenderer = new MapRenderer(24);
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();
    private final Game game = new Game();
    private final MinimapSystem minimapSystem = new MinimapSystem(60, 90);

    private Vehicle vehicle;
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
        vehicle = new Vehicle(100, 100);
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

                game.update(deltaSeconds);
                vehicle.update(deltaSeconds);

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
                   hintLabel.setText(success
                   ? "Road placed"
                   : "Cannot build road here");
                }

                case PLACE_STOP -> {
                boolean success = map.placeStop(row, col);
                hintLabel.setText(success
                    ? "Stop placed"
                    : "Stops must be on buildable road-adjacent tiles");
                }

                case CREATE_ROUTE -> {
                   hintLabel.setText("Route creation not implemented yet");
                }

                case SPAWN_BUS -> {
                    hintLabel.setText("Bus spawning not implemented yet");
                }

                case SPAWN_TRAM -> {
                    hintLabel.setText("Tram spawning not implemented yet");
                }

                case SELECT -> {
                    hintLabel.setText("Selection mode");
                }
            }
            updateHud();
            render();
        });
    }

    @FXML
    private void handleBuildRoadMode() {
        interactionMode = InteractionMode.BUILD_ROAD;
        updateInteractionModeUI();
    }

    @FXML
    private void handlePlaceStopMode() {
        interactionMode = InteractionMode.PLACE_STOP;
        updateInteractionModeUI();
    }
    
    @FXML
    private void handleCreateRouteMode() {
        interactionMode = InteractionMode.CREATE_ROUTE;
        updateInteractionModeUI();
    }

    @FXML
    private void handleSpawnBusMode() {
        interactionMode = InteractionMode.SPAWN_BUS;
        updateInteractionModeUI();
    }

    @FXML
    private void handleSpawnTramMode() {
        interactionMode = InteractionMode.SPAWN_TRAM;
        updateInteractionModeUI();
    }

    @FXML
    private void handleSave() {
        try {
            GameSnapshot snapshot = new GameSnapshot();
            snapshot.setMoney(1000);
            snapshot.setElapsedSeconds(game.getGameClock().getElapsedSeconds());
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
            hintLabel.setText("Loaded money: " + snapshot.getMoney());
        } catch (Exception e) {
            hintLabel.setText("Load failed");
            e.printStackTrace();
        }
    }

    @FXML private void handleStart() { game.start(); updateLabels(); }
    @FXML private void handlePause() { game.pause(); updateLabels(); }

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

    private void updateInteractionModeUI() {
        
        buildModeLabel.setText("Mode: " + switch (interactionMode) {
            case BUILD_ROAD -> "Build Road";
            case PLACE_STOP -> "Place Stop";
            case CREATE_ROUTE -> "Create Route";
            case SPAWN_BUS -> "Spawn Bus";
            case SPAWN_TRAM -> "Spawn Tram";
            case SELECT -> "Select";
        });
        
        hintLabel.setText(switch (interactionMode) {
            case BUILD_ROAD -> "Roads can be placed only on buildable land";
            case PLACE_STOP -> "Stops can be placed only next to existing roads";
            case CREATE_ROUTE -> "Select stops to build a route";
            case SPAWN_BUS -> "Spawn a bus on an existing route";
            case SPAWN_TRAM -> "Spawn a tram on an existing route";
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
            moneyLabel.setText("Money: 1000");
        }

        if (vehicleCountLabel != null) {
            vehicleCountLabel.setText("Vehicles: " + (vehicle != null ? 1 : 0));
        }

        if (routeCountLabel != null) {
            routeCountLabel.setText("Routes: 0");
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

    private void render() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        mapRenderer.drawMap(gc, game.getGameMap());

        if (hoveredRow >= 0 && hoveredCol >= 0 && game.getGameMap().isWithinBounds(hoveredRow, hoveredCol)) {
            boolean valid = switch (interactionMode) {
               case BUILD_ROAD -> game.getGameMap().canPlaceRoad(hoveredRow, hoveredCol);
               case PLACE_STOP -> game.getGameMap().canPlaceStop(hoveredRow, hoveredCol);
               default -> false;
            };

            boolean isStopPreview = interactionMode == InteractionMode.PLACE_STOP;

            if (interactionMode == InteractionMode.BUILD_ROAD || interactionMode == InteractionMode.PLACE_STOP) {
                mapRenderer.drawPlacementPreview(gc, hoveredRow, hoveredCol, valid, isStopPreview);
            }  
        }

        GraphicsContext minimapGc = minimapCanvas.getGraphicsContext2D();
        minimapRenderer.drawMinimap(minimapGc, game.getGameMap(), minimapSystem, vehicle);

        if (vehicle != null) {
            gc.setFill(Color.rgb(253, 221, 92, 0.95));
            gc.fillOval(vehicle.getX(), vehicle.getY(), 10, 10);
            gc.setStroke(Color.rgb(90, 70, 20, 0.6));
            gc.strokeOval(vehicle.getX(), vehicle.getY(), 10, 10);
        }
    }
}
