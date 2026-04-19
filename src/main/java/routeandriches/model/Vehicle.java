package routeandriches.model;

import java.util.List;
import routeandriches.model.enums.VehicleType;

public class Vehicle {

    private final VehicleType type;
    private double x;
    private double y;
    private double speed;
    private Route assignedRoute;
    private int pathIndex;

    public Vehicle(VehicleType type, double x, double y) {
        this(type, x, y, type == VehicleType.TRAM ? 40 : 50);
    }

    public Vehicle(VehicleType type, double x, double y, double speed) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.pathIndex = 0;
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

    public void clearRoute() {
        this.assignedRoute = null;
        this.pathIndex = 0;
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
