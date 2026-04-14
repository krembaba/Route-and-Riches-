/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.system;

import routeandriches.model.GridPos;


public class MinimapSystem {
    private final int mapRows;
    private final int mapCols;

    public MinimapSystem(int mapRows, int mapCols) {
        if (mapRows <= 0 || mapCols <= 0) {
            throw new IllegalArgumentException("Map size must be positive.");
        }
        this.mapRows = mapRows;
        this.mapCols = mapCols;
    }

    public int getMapRows() {
        return mapRows;
    }

    public int getMapCols() {
        return mapCols;
    }

    public double getMiniTileWidth(double minimapWidth) {
        return minimapWidth / mapCols;
    }

    public double getMiniTileHeight(double minimapHeight) {
        return minimapHeight / mapRows;
    }

    public double toMiniMapX(GridPos pos, double minimapWidth) {
        return pos.getCol() * getMiniTileWidth(minimapWidth);
    }

    public double toMiniMapY(GridPos pos, double minimapHeight) {
        return pos.getRow() * getMiniTileHeight(minimapHeight);
    }
}
