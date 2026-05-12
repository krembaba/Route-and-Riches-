package routeandriches.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import routeandriches.model.enums.GameSpeed;
import routeandriches.model.enums.GameState;

public class GameTest {

    @Test
    void constructorShouldInitializeGameWithDefaultValues() {
        Game game = new Game();

        assertEquals(GameState.PAUSED, game.getGameState());
        assertNotNull(game.getGameClock());
        assertNotNull(game.getGameMap());
        assertEquals(0.0, game.getGameClock().getElapsedSeconds(), 0.0001);
        assertEquals(GameSpeed.NORMAL, game.getGameClock().getGameSpeed());
    }

    @Test
    void startShouldSetRunningStateAndNormalSpeed() {
        Game game = new Game();
        game.setSpeed(GameSpeed.FAST);

        game.start();

        assertEquals(GameState.RUNNING, game.getGameState());
        assertEquals(GameSpeed.NORMAL, game.getGameClock().getGameSpeed());
    }

    @Test
    void pauseShouldSetPausedStateAndPausedSpeed() {
        Game game = new Game();
        game.start();

        game.pause();

        assertEquals(GameState.PAUSED, game.getGameState());
        assertEquals(GameSpeed.PAUSED, game.getGameClock().getGameSpeed());
    }

    @Test
    void resumeShouldSetRunningStateAndRestoreNormalSpeedWhenClockIsPaused() {
        Game game = new Game();
        game.start();
        game.pause();

        game.resume();

        assertEquals(GameState.RUNNING, game.getGameState());
        assertEquals(GameSpeed.NORMAL, game.getGameClock().getGameSpeed());
    }

    @Test
    void resumeShouldKeepCurrentSpeedWhenClockIsNotPaused() {
        Game game = new Game();
        game.setSpeed(GameSpeed.FAST);

        game.resume();

        assertEquals(GameState.RUNNING, game.getGameState());
        assertEquals(GameSpeed.FAST, game.getGameClock().getGameSpeed());
    }

    @Test
    void stopShouldSetStoppedStateAndPausedSpeed() {
        Game game = new Game();
        game.start();
        game.setSpeed(GameSpeed.FAST);

        game.stop();

        assertEquals(GameState.STOPPED, game.getGameState());
        assertEquals(GameSpeed.PAUSED, game.getGameClock().getGameSpeed());
    }

    @Test
    void setSpeedShouldUpdateClockSpeed() {
        Game game = new Game();

        game.setSpeed(GameSpeed.FAST);

        assertEquals(GameSpeed.FAST, game.getGameClock().getGameSpeed());
    }

    @Test
    void updateShouldNotAdvanceClockWhenGameIsPaused() {
        Game game = new Game();

        game.update(5.0);

        assertEquals(0.0, game.getGameClock().getElapsedSeconds(), 0.0001);
    }

    @Test
    void updateShouldAdvanceClockWhenGameIsRunningAtNormalSpeed() {
        Game game = new Game();
        game.start();

        game.update(5.0);

        assertEquals(5.0, game.getGameClock().getElapsedSeconds(), 0.0001);
    }

    @Test
    void updateShouldAdvanceClockUsingCurrentSpeedMultiplier() {
        Game game = new Game();
        game.start();
        game.setSpeed(GameSpeed.FAST);

        game.update(3.0);

        assertEquals(6.0, game.getGameClock().getElapsedSeconds(), 0.0001);
    }

    @Test
    void updateShouldNotAdvanceClockWhenGameIsStopped() {
        Game game = new Game();
        game.start();
        game.stop();

        game.update(5.0);

        assertEquals(0.0, game.getGameClock().getElapsedSeconds(), 0.0001);
    }
}