package routeandriches.model.enums;

public enum VehicleType {
    BUS(40, 50),
    TRAM(70, 40);

    private final int capacity;
    private final double defaultSpeed;

    VehicleType(int capacity, double defaultSpeed) {
        this.capacity = capacity;
        this.defaultSpeed = defaultSpeed;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getDefaultSpeed() {
        return defaultSpeed;
    }
}
