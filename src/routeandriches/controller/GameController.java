/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.controller;


import routeandriches.model.Game;
import routeandriches.model.enums.GameSpeed;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;

public class GameController {

    @FXML
    private Canvas mapCanvas;

    @FXML
    private Canvas minimapCanvas;

    @FXML
    private Label stateLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label timeLabel;

    private final Game game = new Game();

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;

    @FXML
    public void initialize() {
        setupGameLoop();
        render();
        updateLabels();
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
                render();
                updateLabels();
            }
        };
        gameLoop.start();
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

    private void updateLabels() {
        stateLabel.setText("State: " + game.getGameState());
        speedLabel.setText("Speed: " + game.getGameClock().getGameSpeed());
        timeLabel.setText(String.format("Time: %.1f", game.getGameClock().getElapsedSeconds()));
    }

    private void render() {
        drawMapPlaceholder();
        drawMinimapPlaceholder();
    }

    private void drawMapPlaceholder() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        double tileSize = 40;
        int rows = (int) (mapCanvas.getHeight() / tileSize);
        int cols = (int) (mapCanvas.getWidth() / tileSize);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = col * tileSize;
                double y = row * tileSize;
                gc.strokeRect(x, y, tileSize, tileSize);
            }
        }
    }

    private void drawMinimapPlaceholder() {
        GraphicsContext gc = minimapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, minimapCanvas.getWidth(), minimapCanvas.getHeight());
        gc.strokeRect(10, 10, 180, 180);
    }
}
