/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.ui;

import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import routeandriches.model.GameMap;
import routeandriches.model.TileType;
import routeandriches.model.Vehicle;
import routeandriches.model.enums.VehicleType;
import routeandriches.system.MinimapSystem;

/**
 *
 * @author dell
 */
public class MinimapRenderer {
    /**
     * Executes drawMinimap.
     */
    public void drawMinimap(GraphicsContext gc,
                            GameMap map,
                            MinimapSystem minimapSystem,
                            List<Vehicle> vehicles,
                            Vehicle focusedVehicle) {

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        double miniTileWidth = minimapSystem.getMiniTileWidth(canvasWidth);
        double miniTileHeight = minimapSystem.getMiniTileHeight(canvasHeight);

        
        gc.setFill(Color.web("#18222d"));
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

        if (vehicles != null) {
            double tileSize = 24.0; 
            for (Vehicle vehicle : vehicles) {
                if (vehicle == null) {
                    continue;
                }

                double vehicleCol = vehicle.getX() / tileSize;
                double vehicleRow = vehicle.getY() / tileSize;

                double miniX = vehicleCol * miniTileWidth;
                double miniY = vehicleRow * miniTileHeight;

                Color fillColor = vehicle.getType() == VehicleType.TRAM
                        ? Color.web("#5bd7ff")
                        : Color.web("#ffdd5c");
                Color strokeColor = vehicle.getType() == VehicleType.TRAM
                        ? Color.rgb(20, 70, 95, 0.8)
                        : Color.rgb(60, 50, 20, 0.8);

                gc.setFill(fillColor);
                gc.fillOval(miniX, miniY, 5, 5);

                gc.setStroke(strokeColor);
                gc.strokeOval(miniX, miniY, 5, 5);
            }
        }

        if (focusedVehicle != null) {
            double tileSize = 24.0;
            double focusedCol = focusedVehicle.getX() / tileSize;
            double focusedRow = focusedVehicle.getY() / tileSize;
            double focusedX = focusedCol * miniTileWidth;
            double focusedY = focusedRow * miniTileHeight;

            gc.setStroke(Color.web("#ffffff"));
            gc.setLineWidth(1.2);
            gc.strokeOval(focusedX - 2, focusedY - 2, 9, 9);
        }

        
        gc.setStroke(Color.rgb(255, 255, 255, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(1, 1, canvasWidth - 2, canvasHeight - 2, 12, 12);
    }
}
