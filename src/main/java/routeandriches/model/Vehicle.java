package routeandriches.model;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    private double x;
    private double y;
    private double speed = 70.0;
    private final double tileSize = 24.0;

    private final List<GridPos> routePath = new ArrayList<>();
    private int targetIndex = 0;

    public Vehicle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vehicle(List<GridPos> path) {
        setRoute(path);
    }

    public void setRoute(List<GridPos> path) {
        routePath.clear();

        if (path == null || path.isEmpty()) {
            targetIndex = 0;
            return;
        }

        routePath.addAll(path);

        GridPos first = routePath.get(0);
        this.x = tileCenterX(first.getCol()) - 5;
        this.y = tileCenterY(first.getRow()) - 5;

        targetIndex = routePath.size() > 1 ? 1 : 0;
    }

    public void update(double delta) {
        if (routePath.isEmpty() || routePath.size() == 1) {
            return;
        }

        GridPos target = routePath.get(targetIndex);
        double targetX = tileCenterX(target.getCol()) - 5;
        double targetY = tileCenterY(target.getRow()) - 5;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1.0) {
            x = targetX;
            y = targetY;
            targetIndex++;

            if (targetIndex >= routePath.size()) {
                targetIndex = 0;
            }
            return;
        }

        double move = speed * delta;
        if (move >= distance) {
            x = targetX;
            y = targetY;
            targetIndex++;

            if (targetIndex >= routePath.size()) {
                targetIndex = 0;
            }
        } else {
            x += (dx / distance) * move;
            y += (dy / distance) * move;
        }
    }

    private double tileCenterX(int col) {
        return col * tileSize + tileSize / 2.0;
    }

    private double tileCenterY(int row) {
        return row * tileSize + tileSize / 2.0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}