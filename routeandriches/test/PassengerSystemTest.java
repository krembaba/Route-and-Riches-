package routeandriches.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import routeandriches.model.DecorationType;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Passenger;
import routeandriches.model.Route;
import routeandriches.model.Stop;
import routeandriches.model.TileType;

public class PassengerSystemTest {

    @Test
    void updateShouldThrowWhenMapIsNull() {
        PassengerSystem system = new PassengerSystem();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.update(1.0, null, List.of())
        );

        assertEquals("Map cannot be null.", exception.getMessage());
    }

    @Test
    void setSpawnIntervalShouldThrowWhenNonPositive() {
        PassengerSystem system = new PassengerSystem();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.setSpawnIntervalSeconds(0.0)
        );

        assertEquals("Spawn interval must be positive.", exception.getMessage());
    }

    @Test
    void setPassengerSequenceShouldClampToAtLeastOne() {
        PassengerSystem system = new PassengerSystem();

        system.setPassengerSequence(0);

        assertEquals(1, system.getPassengerSequence());
    }

    @Test
    void synchronizeStopsShouldFindStopsPlacedOnMap() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 3, 3);

        system.synchronizeStops(map, List.of());

        assertEquals(2, system.getStopCount());
        assertNotNull(system.findStopByPosition(new GridPos(1, 1)));
        assertNotNull(system.findStopByPosition(new GridPos(3, 3)));
        assertEquals("S_1_1", system.findStopByPosition(new GridPos(1, 1)).getId());
        assertEquals("S_3_3", system.findStopByPosition(new GridPos(3, 3)).getId());
    }

    @Test
    void synchronizeStopsShouldAlsoIncludeRouteStopsInsideBounds() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);

        Route route = new Route(
                "Route 1",
                List.of(new GridPos(0, 0), new GridPos(4, 4)),
                List.of(new GridPos(0, 0), new GridPos(4, 4))
        );

        system.synchronizeStops(map, List.of(route));

        assertEquals(2, system.getStopCount());
        assertNotNull(system.findStopById("S_0_0"));
        assertNotNull(system.findStopById("S_4_4"));
    }

    @Test
    void findStopMethodsShouldReturnNullForMissingOrInvalidInput() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 2, 2);
        system.synchronizeStops(map, List.of());

        assertNull(system.findStopByPosition(null));
        assertNull(system.findStopById(null));
        assertNull(system.findStopById(" "));
        assertNull(system.findStopByPosition(new GridPos(0, 0)));
        assertNull(system.findStopById("S_9_9"));
    }

    @Test
    void updateShouldSpawnPassengerWhenEnoughTimePasses() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 3, 3);

        system.setSpawnIntervalSeconds(1.0);
        system.synchronizeStops(map, List.of());

        assertEquals(0, system.getTotalWaitingPassengers());
        assertEquals(1, system.getPassengerSequence());

        system.update(1.0, map, List.of());

        assertEquals(1, system.getTotalWaitingPassengers());
        assertEquals(2, system.getPassengerSequence());

        Stop firstStop = system.findStopById("S_1_1");
        Stop secondStop = system.findStopById("S_3_3");

        Passenger spawnedPassenger = firstStop.peekWaitingPassenger();
        if (spawnedPassenger == null) {
            spawnedPassenger = secondStop.peekWaitingPassenger();
        }

        assertNotNull(spawnedPassenger);
        assertTrue(spawnedPassenger.isWaiting());
        assertFalse(spawnedPassenger.getOriginStopId().equals(spawnedPassenger.getDestinationStopId()));
    }

    @Test
    void updateShouldSpawnMultiplePassengersForLargeDelta() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 0, 0);
        placeStopTile(map, 4, 4);

        system.setSpawnIntervalSeconds(1.0);
        system.synchronizeStops(map, List.of());

        system.update(3.2, map, List.of());

        assertEquals(3, system.getTotalWaitingPassengers());
        assertEquals(4, system.getPassengerSequence());
    }

    @Test
    void updateShouldPreferDestinationsCompatibleWithOriginRoute() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 1, 5);
        placeStopTile(map, 9, 9); // unrelated stop

        Route route = new Route(
                "Route AB",
                List.of(new GridPos(1, 1), new GridPos(1, 5)),
                List.of(new GridPos(1, 1), new GridPos(1, 2), new GridPos(1, 3), new GridPos(1, 4), new GridPos(1, 5))
        );

        system.setSpawnIntervalSeconds(0.2);
        system.update(4.0, map, List.of(route));

        Stop stopA = system.findStopById("S_1_1");
        Stop stopB = system.findStopById("S_1_5");
        Stop stopC = system.findStopById("S_9_9");

        int compatiblePassengers = countPassengersWithDestination(stopA, "S_1_5")
                + countPassengersWithDestination(stopB, "S_1_1");
        int incompatiblePassengers = countPassengersWithDestination(stopA, "S_9_9")
                + countPassengersWithDestination(stopB, "S_9_9");

        assertTrue(compatiblePassengers > 0);
        assertEquals(0, incompatiblePassengers);
        assertNotNull(stopC);
    }

    @Test
    void updateShouldSpawnFasterWhenGrowthDemandIncreases() {
        PassengerSystem system = new PassengerSystem();
        CityGrowthSystem growthSystem = new CityGrowthSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 8, 8);

        system.setSpawnIntervalSeconds(1.0);
        system.synchronizeStops(map, List.of());

        growthSystem.update(0.5, map, List.of(), List.of());
        growthSystem.registerTransportActivity(new GridPos(1, 1), 80, 80);
        growthSystem.update(1.0, map, List.of(), List.of());

        system.update(4.0, map, List.of(), growthSystem);

        assertTrue(system.getTotalWaitingPassengers() > 4);
    }

    @Test
    void updateShouldNotSpawnWhenFewerThanTwoStopsExist() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 2, 2);

        system.setSpawnIntervalSeconds(1.0);
        system.update(5.0, map, List.of());

        assertEquals(1, system.getStopCount());
        assertEquals(0, system.getTotalWaitingPassengers());
        assertEquals(1, system.getPassengerSequence());
    }

    @Test
    void addWaitingPassengerToStopShouldReturnTrueForExistingStop() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 2, 2);
        system.synchronizeStops(map, List.of());

        Passenger passenger = new Passenger("P100", "S_1_1", "S_2_2");

        boolean added = system.addWaitingPassengerToStop("S_1_1", passenger);

        assertTrue(added);
        assertEquals(1, system.getTotalWaitingPassengers());
        assertEquals(1, system.findStopById("S_1_1").getWaitingCount());
    }

    @Test
    void addWaitingPassengerToStopShouldReturnFalseForMissingStopOrNullPassenger() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 2, 2);
        system.synchronizeStops(map, List.of());

        Passenger passenger = new Passenger("P100", "S_1_1", "S_2_2");

        assertFalse(system.addWaitingPassengerToStop("S_9_9", passenger));
        assertFalse(system.addWaitingPassengerToStop("S_1_1", null));
        assertEquals(0, system.getTotalWaitingPassengers());
    }

    @Test
    void clearWaitingPassengersShouldDrainAllQueues() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 2, 2);
        system.synchronizeStops(map, List.of());

        system.addWaitingPassengerToStop("S_1_1", new Passenger("P1", "S_1_1", "S_2_2"));
        system.addWaitingPassengerToStop("S_1_1", new Passenger("P2", "S_1_1", "S_2_2"));
        system.addWaitingPassengerToStop("S_2_2", new Passenger("P3", "S_2_2", "S_1_1"));

        assertEquals(3, system.getTotalWaitingPassengers());

        system.clearWaitingPassengers();

        assertEquals(0, system.getTotalWaitingPassengers());
        assertEquals(0, system.findStopById("S_1_1").getWaitingCount());
        assertEquals(0, system.findStopById("S_2_2").getWaitingCount());
    }

    @Test
    void getStopsShouldBeUnmodifiable() {
        PassengerSystem system = new PassengerSystem();
        GameMap map = new GameMap(12, 12);

        clearMap(map);
        placeStopTile(map, 1, 1);
        placeStopTile(map, 2, 2);
        system.synchronizeStops(map, List.of());

        assertThrows(
                UnsupportedOperationException.class,
                () -> system.getStops().add(new Stop("S_9_9", "Stop 9_9", new GridPos(9, 9)))
        );
    }

    private void clearMap(GameMap map) {
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                map.setTile(row, col, TileType.EMPTY, true, DecorationType.NONE, 0);
            }
        }
        map.refreshRoadShapes();
    }

    private void placeStopTile(GameMap map, int row, int col) {
        map.setTile(row, col, TileType.STOP, false, DecorationType.NONE, 0);
    }

    private int countPassengersWithDestination(Stop stop, String destinationId) {
        int count = 0;
        if (stop == null) {
            return 0;
        }
        for (Passenger passenger : stop.getWaitingPassengers()) {
            if (destinationId.equals(passenger.getDestinationStopId())) {
                count++;
            }
        }
        return count;
    }
}
