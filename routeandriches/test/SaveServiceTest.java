package routeandriches.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SaveServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadShouldPreserveSnapshotData() throws IOException {
        SaveService saveService = new SaveService();
        GameSnapshot snapshot = new GameSnapshot();

        snapshot.setMoney(1250);
        snapshot.setElapsedSeconds(42.5);
        snapshot.setGameState("RUNNING");
        snapshot.setGameSpeed("FAST");
        snapshot.setMapData(List.of(
                "10|15|ROAD|false|NONE|0",
                "10|16|STOP|false|NONE|0"
        ));
        snapshot.setRouteData(List.of(
                "Route 1;10,16|11,16;10,16|10,15|11,15|11,16"
        ));
        snapshot.setVehicleData(List.of(
                "BUS|240.0|360.0|1.5|Route 1|2"
        ));
        snapshot.setTrafficLightData(List.of(
                "12|18|GREEN|3.0|3.0|1.5"
        ));
        snapshot.setPassengerData(List.of(
                "W|S10_16|P1|S10_16|S11_16",
                "V|0|P2|S10_16|S11_16"
        ));

        Path saveFile = tempDir.resolve("savegame.json");

        saveService.save(snapshot, saveFile.toString());
        GameSnapshot loaded = saveService.load(saveFile.toString());

        assertEquals(1250, loaded.getMoney());
        assertEquals(42.5, loaded.getElapsedSeconds(), 0.0001);
        assertEquals("RUNNING", loaded.getGameState());
        assertEquals("FAST", loaded.getGameSpeed());
        assertIterableEquals(snapshot.getMapData(), loaded.getMapData());
        assertIterableEquals(snapshot.getRouteData(), loaded.getRouteData());
        assertIterableEquals(snapshot.getVehicleData(), loaded.getVehicleData());
        assertIterableEquals(snapshot.getTrafficLightData(), loaded.getTrafficLightData());
        assertIterableEquals(snapshot.getPassengerData(), loaded.getPassengerData());
    }

    @Test
    void saveShouldThrowWhenSnapshotIsNull() {
        SaveService saveService = new SaveService();
        Path saveFile = tempDir.resolve("savegame.json");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> saveService.save(null, saveFile.toString())
        );

        assertEquals("Snapshot cannot be null.", exception.getMessage());
    }

    @Test
    void saveShouldThrowWhenPathIsBlank() {
        SaveService saveService = new SaveService();
        GameSnapshot snapshot = new GameSnapshot();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> saveService.save(snapshot, "   ")
        );

        assertEquals("File path cannot be empty.", exception.getMessage());
    }

    @Test
    void loadShouldThrowWhenPathIsBlank() {
        SaveService saveService = new SaveService();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> saveService.load(" ")
        );

        assertEquals("File path cannot be empty.", exception.getMessage());
    }

    @Test
    void loadShouldThrowWhenFileDoesNotExist() {
        SaveService saveService = new SaveService();
        Path missingFile = tempDir.resolve("missing-save.json");

        assertThrows(IOException.class, () -> saveService.load(missingFile.toString()));
    }

    @Test
    void saveShouldCreateAReadableJsonFile() throws IOException {
        SaveService saveService = new SaveService();
        GameSnapshot snapshot = new GameSnapshot();
        Path saveFile = tempDir.resolve("savegame.json");

        snapshot.setMoney(900);
        snapshot.setElapsedSeconds(12.0);
        snapshot.setGameState("PAUSED");
        snapshot.setGameSpeed("NORMAL");
        snapshot.setMapData(List.of("1|1|ROAD|false|NONE|0"));

        saveService.save(snapshot, saveFile.toString());

        assertTrue(Files.exists(saveFile));
        String content = Files.readString(saveFile);
        assertTrue(content.contains("\"money\": 900"));
        assertTrue(content.contains("\"gameState\": \"PAUSED\""));
        assertTrue(content.contains("\"mapData\""));
    }

    @Test
    void loadShouldUseDefaultsWhenJsonIsMissingFields() throws IOException {
        SaveService saveService = new SaveService();
        Path saveFile = tempDir.resolve("partial-save.json");

        Files.writeString(saveFile, "{\n  \"money\": 300\n}");

        GameSnapshot loaded = saveService.load(saveFile.toString());

        assertEquals(300, loaded.getMoney());
        assertEquals(0.0, loaded.getElapsedSeconds(), 0.0001);
        assertEquals("PAUSED", loaded.getGameState());
        assertEquals("PAUSED", loaded.getGameSpeed());
        assertTrue(loaded.getMapData().isEmpty());
        assertTrue(loaded.getRouteData().isEmpty());
        assertTrue(loaded.getVehicleData().isEmpty());
        assertTrue(loaded.getTrafficLightData().isEmpty());
        assertTrue(loaded.getPassengerData().isEmpty());
    }

    @Test
    void saveAndLoadShouldHandleEscapedCharacters() throws IOException {
        SaveService saveService = new SaveService();
        GameSnapshot snapshot = new GameSnapshot();
        Path saveFile = tempDir.resolve("escaped-save.json");

        snapshot.setGameState("RUNNING");
        snapshot.setGameSpeed("FAST");
        snapshot.setRouteData(List.of("Route \"A\";10,10|10,11;10,10|10,11"));
        snapshot.setPassengerData(List.of("W|STOP\\\\1|P7|STOP\\\\1|STOP2"));

        saveService.save(snapshot, saveFile.toString());
        GameSnapshot loaded = saveService.load(saveFile.toString());

        assertEquals("RUNNING", loaded.getGameState());
        assertEquals("FAST", loaded.getGameSpeed());
        assertIterableEquals(snapshot.getRouteData(), loaded.getRouteData());
        assertIterableEquals(snapshot.getPassengerData(), loaded.getPassengerData());
    }
}