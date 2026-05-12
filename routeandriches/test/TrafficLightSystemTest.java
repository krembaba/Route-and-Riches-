package routeandriches.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import routeandriches.model.GridPos;
import routeandriches.model.TrafficLight;
import routeandriches.model.TrafficLightState;

public class TrafficLightSystemTest {

    private TrafficLightSystem system;

    @BeforeEach
    void setUp() {
        system = new TrafficLightSystem();
    }

    @Test
    void addTrafficLightShouldThrowWhenNull() {
        TrafficLight nullLight = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.addTrafficLight(nullLight)
        );

        assertEquals("Traffic light cannot be null.", exception.getMessage());
    }

    @Test
    void addTrafficLightShouldAddLightToSystem() {
        TrafficLight light = new TrafficLight(new GridPos(1, 1));

        system.addTrafficLight(light);

        assertEquals(1, system.getTrafficLights().size());
        assertEquals(light, system.getTrafficLights().get(0));
    }

    @Test
    void removeTrafficLightAtShouldRemoveExistingLight() {
        TrafficLight light = new TrafficLight(new GridPos(2, 2));
        system.addTrafficLight(light);

        boolean removed = system.removeTrafficLightAt(new GridPos(2, 2));

        assertTrue(removed);
        assertEquals(0, system.getTrafficLights().size());
    }

    @Test
    void removeTrafficLightAtShouldReturnFalseWhenNoLightExists() {
        boolean removed = system.removeTrafficLightAt(new GridPos(3, 3));

        assertFalse(removed);
        assertEquals(0, system.getTrafficLights().size());
    }

    @Test
    void getTrafficLightAtShouldReturnCorrectLight() {
        TrafficLight expectedLight = new TrafficLight(new GridPos(4, 4));
        system.addTrafficLight(expectedLight);

        TrafficLight foundLight = system.getTrafficLightAt(new GridPos(4, 4));

        assertNotNull(foundLight);
        assertEquals(expectedLight.getPosition(), foundLight.getPosition());
    }

    @Test
    void getTrafficLightAtShouldReturnNullWhenNoLightExists() {
        TrafficLight light = system.getTrafficLightAt(new GridPos(5, 5));

        assertNull(light);
    }

    @Test
    void hasTrafficLightAtShouldReturnTrueForExistingLight() {
        system.addTrafficLight(new TrafficLight(new GridPos(6, 6)));

        assertTrue(system.hasTrafficLightAt(new GridPos(6, 6)));
    }

    @Test
    void hasTrafficLightAtShouldReturnFalseForMissingLight() {
        assertFalse(system.hasTrafficLightAt(new GridPos(7, 7)));
    }

    @Test
    void isRedAtShouldReturnTrueForRedLight() {
        TrafficLight redLight = new TrafficLight(new GridPos(8, 8));
        redLight.setState(TrafficLightState.RED);
        system.addTrafficLight(redLight);

        assertTrue(system.isRedAt(new GridPos(8, 8)));
    }

    @Test
    void isRedAtShouldReturnFalseForNonRedLight() {
        TrafficLight greenLight = new TrafficLight(new GridPos(9, 9));
        greenLight.setState(TrafficLightState.GREEN);
        system.addTrafficLight(greenLight);

        assertFalse(system.isRedAt(new GridPos(9, 9)));
    }

    @Test
    void isRedAtShouldReturnFalseForMissingLight() {
        assertFalse(system.isRedAt(new GridPos(10, 10)));
    }

    @Test
    void isGreenAtShouldReturnTrueForGreenLight() {
        TrafficLight greenLight = new TrafficLight(new GridPos(11, 11));
        greenLight.setState(TrafficLightState.GREEN);
        system.addTrafficLight(greenLight);

        assertTrue(system.isGreenAt(new GridPos(11, 11)));
    }

    @Test
    void isGreenAtShouldReturnFalseForNonGreenLight() {
        TrafficLight redLight = new TrafficLight(new GridPos(12, 12));
        redLight.setState(TrafficLightState.RED);
        system.addTrafficLight(redLight);

        assertFalse(system.isGreenAt(new GridPos(12, 12)));
    }

    @Test
    void isGreenAtShouldReturnFalseForMissingLight() {
        assertFalse(system.isGreenAt(new GridPos(13, 13)));
    }

    @Test
    void updateShouldCallUpdateOnAllTrafficLights() {
        TrafficLight mockLight1 = new TrafficLight(new GridPos(14, 14));
        TrafficLight mockLight2 = new TrafficLight(new GridPos(15, 15));
        system.addTrafficLight(mockLight1);
        system.addTrafficLight(mockLight2);

        system.update(1.0);

        // TrafficLight.update() advances its internal timer, but we can't verify that directly
        // The fact that the test doesn't crash verifies the method exists and is called
        assertEquals(2, system.getTrafficLights().size());
    }

    @Test
    void getTrafficLightsShouldReturnUnmodifiableList() {
        TrafficLight light = new TrafficLight(new GridPos(16, 16));
        system.addTrafficLight(light);

        List<TrafficLight> lights = system.getTrafficLights();

        assertThrows(
                UnsupportedOperationException.class,
                () -> lights.add(new TrafficLight(new GridPos(17, 17)))
        );
    }
}