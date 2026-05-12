/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.system;

import routeandriches.model.enums.GameSpeed;

/**
 * Maintains in-game elapsed time and speed multiplier state.
 */
public class GameClock {
    private double elapsedSeconds;
    private GameSpeed gameSpeed;

    /**
     * Creates a new clock starting at {@code 0.0} seconds in normal speed mode.
     */
    public GameClock() {
        this.elapsedSeconds = 0.0;
        this.gameSpeed = GameSpeed.NORMAL;
    }

    /**
     * Advances the clock by scaled delta time.
     *
     * @param deltaSeconds elapsed real time in seconds; negative values are ignored
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0.0) {
            return;
        }
        elapsedSeconds += deltaSeconds * gameSpeed.getMultiplier();
    }

    /**
     * @return total elapsed game time in seconds
     */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Sets elapsed game time.
     *
     * @param elapsedSeconds new elapsed value; values below zero are clamped to zero
     */
    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = Math.max(0.0, elapsedSeconds);
    }

    /**
     * @return current game speed mode
     */
    public GameSpeed getGameSpeed() {
        return gameSpeed;
    }

    /**
     * Sets the current game speed mode.
     *
     * @param gameSpeed non-null speed value
     */
    public void setGameSpeed(GameSpeed gameSpeed) {
        if (gameSpeed == null) {
            throw new IllegalArgumentException("Game speed cannot be null.");
        }
        this.gameSpeed = gameSpeed;
    }

    /**
     * Resets the clock to its initial state.
     */
    public void reset() {
        elapsedSeconds = 0.0;
        gameSpeed = GameSpeed.NORMAL;
    }
}
