package routeandriches.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import routeandriches.model.GameMap;
import routeandriches.model.TileType;
import routeandriches.model.Vehicle;
import routeandriches.system.MinimapSystem;

public class MinimapRenderer {

    public void drawMinimap(GraphicsContext gc, GameMap map, MinimapSystem minimapSystem, Vehicle vehicle, double tileSize) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        double miniTileWidth = canvasWidth / map.getCols();
        double miniTileHeight = canvasHeight / map.getRows();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        gc.setFill(Color.rgb(36, 38, 42, 0.92));
        gc.fillRoundRect(0, 0, canvasWidth, canvasHeight, 12, 12);

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                TileType tileType = map.getTile(row, col).getType();

                switch (tileType) {
                    case ROAD -> gc.setFill(Color.web("#616971"));
                    case STOP -> gc.setFill(Color.web("#4db3ff"));
                    case BUILDING -> gc.setFill(Color.web("#7f7267"));
                    case PARK -> gc.setFill(Color.web("#6eae63"));
                    default -> gc.setFill(Color.web("#8fb97f"));
                }

                gc.fillRect(col * miniTileWidth,
                            row * miniTileHeight,
                            miniTileWidth,
                            miniTileHeight);
            }
        }

        if (vehicle != null) {
            double vehicleCol = vehicle.getX() / tileSize;
            double vehicleRow = vehicle.getY() / tileSize;

            double miniX = vehicleCol * miniTileWidth;
            double miniY = vehicleRow * miniTileHeight;

            gc.setFill(Color.web("#ffdd5c"));
            gc.fillOval(miniX, miniY, 5, 5);

            gc.setStroke(Color.rgb(60, 50, 20, 0.7));
            gc.strokeOval(miniX, miniY, 5, 5);
        }

        gc.setStroke(Color.rgb(255, 255, 255, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(1, 1, canvasWidth - 2, canvasHeight - 2, 12, 12);
    }

    public void drawViewport(GraphicsContext gc, GameMap map,
                             double viewportLeft, double viewportTop,
                             double viewportWidth, double viewportHeight,
                             double tileSize) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        double totalMapWidth = map.getCols() * tileSize;
        double totalMapHeight = map.getRows() * tileSize;

        if (totalMapWidth <= 0 || totalMapHeight <= 0) {
            return;
        }

        double scaleX = canvasWidth / totalMapWidth;
        double scaleY = canvasHeight / totalMapHeight;

        gc.setStroke(Color.rgb(255, 230, 140, 0.95));
        gc.setLineWidth(2);
        gc.strokeRoundRect(viewportLeft * scaleX,
                viewportTop * scaleY,
                Math.max(8, viewportWidth * scaleX),
                Math.max(8, viewportHeight * scaleY),
                6,
                6);
    }
}
