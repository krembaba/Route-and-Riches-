/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.system;

import routeandriches.model.enums.GameSpeed;

public class GameClock {
    private double elapsedSeconds;
    private GameSpeed gameSpeed;

    public GameClock() {
        this.elapsedSeconds = 0.0;
        this.gameSpeed = GameSpeed.NORMAL;
    }

    public void update(double deltaSeconds) {
        elapsedSeconds += deltaSeconds * gameSpeed.getMultiplier();
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = Math.max(0.0, elapsedSeconds);
    }

    public GameSpeed getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(GameSpeed gameSpeed) {
        if (gameSpeed == null) {
            throw new IllegalArgumentException("Game speed cannot be null.");
        }
        this.gameSpeed = gameSpeed;
    }

    public void reset() {
        elapsedSeconds = 0.0;
        gameSpeed = GameSpeed.NORMAL;
    }
}
