package routeandriches.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import routeandriches.model.enums.PassengerState;
import routeandriches.model.enums.VehicleType;

public class Vehicle {

    private final VehicleType type;
    private final int capacity;
    private double x;
    private double y;
    private double speed;
    private Route assignedRoute;
    private int pathIndex;
    private final List<Passenger> onboardPassengers;

    public Vehicle(VehicleType type, double x, double y) {
        this(type, x, y, type == null ? 0 : type.getDefaultSpeed());
    }

    public Vehicle(VehicleType type, double x, double y, double speed) {
        this.type = Objects.requireNonNull(type, "Vehicle type cannot be null.");
        this.capacity = type.getCapacity();
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.pathIndex = 0;
        this.onboardPassengers = new ArrayList<>();
    }

    public void assignRoute(Route route, double tileSize) {
        this.assignedRoute = route;
        this.pathIndex = 0;

        if (route != null && route.isValid()) {
            GridPos start = route.getPath().get(0);
            this.x = centerX(start, tileSize);
            this.y = centerY(start, tileSize);
        }
    }

    public void update(double deltaSeconds, double tileSize) {
        if (assignedRoute == null || !assignedRoute.isValid()) {
            return;
        }

        List<GridPos> path = assignedRoute.getPath();
        if (path.size() < 2) {
            return;
        }

        int nextIndex = (pathIndex + 1) % path.size();
        GridPos target = path.get(nextIndex);

        double targetX = centerX(target, tileSize);
        double targetY = centerY(target, tileSize);

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double moveDistance = speed * deltaSeconds;

        if (distance <= moveDistance) {
            x = targetX;
            y = targetY;
            pathIndex = nextIndex;
        } else if (distance > 0) {
            x += (dx / distance) * moveDistance;
            y += (dy / distance) * moveDistance;
        }
    }

    public VehicleType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeed() {
        return speed;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public GridPos getCurrentPathPosition() {
        if (assignedRoute == null || !assignedRoute.isValid()) {
            return null;
        }

        List<GridPos> path = assignedRoute.getPath();
        if (path.isEmpty() || pathIndex < 0 || pathIndex >= path.size()) {
            return null;
        }

        return path.get(pathIndex);
    }

    public GridPos getNextPathPosition() {
        if (assignedRoute == null || !assignedRoute.isValid()) {
            return null;
        }

        List<GridPos> path = assignedRoute.getPath();
        if (path.size() < 2) {
            return null;
        }

        int nextIndex = (pathIndex + 1) % path.size();
        return path.get(nextIndex);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOnboardCount() {
        return onboardPassengers.size();
    }

    public List<Passenger> getOnboardPassengers() {
        return Collections.unmodifiableList(onboardPassengers);
    }

    public void clearOnboardPassengers() {
        onboardPassengers.clear();
    }

    public boolean hasFreeSeat() {
        return onboardPassengers.size() < capacity;
    }

    public boolean boardPassenger(Passenger passenger) {
        if (passenger == null || !hasFreeSeat()) {
            return false;
        }

        if (passenger.getState() != PassengerState.WAITING) {
            return false;
        }

        passenger.markOnboard();
        onboardPassengers.add(passenger);
        return true;
    }

    public boolean restoreOnboardPassenger(Passenger passenger) {
        if (passenger == null || !hasFreeSeat()) {
            return false;
        }
        passenger.markOnboard();
        onboardPassengers.add(passenger);
        return true;
    }

    public int dropOffPassengersAt(String stopId) {
        if (stopId == null || stopId.isBlank()) {
            return 0;
        }

        int dropped = 0;
        for (int i = onboardPassengers.size() - 1; i >= 0; i--) {
            Passenger passenger = onboardPassengers.get(i);
            if (stopId.equals(passenger.getDestinationStopId())) {
                passenger.markDelivered();
                onboardPassengers.remove(i);
                dropped++;
            }
        }

        return dropped;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String toSaveString() {
        String routeName = assignedRoute != null ? assignedRoute.getName() : "NONE";
        return type + "|" + x + "|" + y + "|" + speed + "|" + routeName + "|" + pathIndex;
    }

    public static Vehicle fromSaveString(String data, List<Route> routes) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 6) {
                return null;
            }

            VehicleType type = VehicleType.valueOf(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double speed = Double.parseDouble(parts[3]);
            String routeName = parts[4];
            int savedPathIndex = Integer.parseInt(parts[5]);

            Vehicle vehicle = new Vehicle(type, x, y, speed);

            if (!"NONE".equals(routeName)) {
                Route route = findRouteByName(routes, routeName);
                if (route != null) {
                    vehicle.assignedRoute = route;
                    int maxIndex = Math.max(0, route.getPath().size() - 1);
                    vehicle.pathIndex = Math.min(savedPathIndex, maxIndex);
                }
            }

            return vehicle;
        } catch (Exception e) {
            return null;
        }
    }

    private static Route findRouteByName(List<Route> routes, String routeName) {
        if (routes == null) {
            return null;
        }

        for (Route route : routes) {
            if (route.getName().equals(routeName)) {
                return route;
            }
        }

        return null;
    }

    private double centerX(GridPos pos, double tileSize) {
        return pos.getCol() * tileSize + tileSize / 2.0;
    }

    private double centerY(GridPos pos, double tileSize) {
        return pos.getRow() * tileSize + tileSize / 2.0;
    }
}
