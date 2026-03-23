/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.model;

import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.GameState;
import routeandriches.system.GameClock;

public class Game {
    private GameState gameState;
    private final GameClock gameClock;

    public Game() {
        this.gameState = GameState.PAUSED;
        this.gameClock = new GameClock();
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameClock getGameClock() {
        return gameClock;
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
        if (gameState == GameState.RUNNING) {
            gameClock.update(deltaSeconds);
        }
    }
}
