package routeandriches.model.enums;

/**

 * Enumerates supported VehicleType values.

 */

public enum VehicleType {
    BUS(40, 50),
    TRAM(70, 40);

    private final int capacity;
    private final double defaultSpeed;

    VehicleType(int capacity, double defaultSpeed) {
        this.capacity = capacity;
        this.defaultSpeed = defaultSpeed;
    }

    /**
     * Executes getCapacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Executes getDefaultSpeed.
     */
    public double getDefaultSpeed() {
        return defaultSpeed;
    }
}

