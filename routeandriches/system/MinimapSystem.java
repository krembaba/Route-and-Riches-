/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.system;

import routeandriches.model.GridPos;


/**


 * Represents the MinimapSystem component.


 */


public class MinimapSystem {
    private final int mapRows;
    private final int mapCols;

    /**
     * Creates a new MinimapSystem instance.
     */
    public MinimapSystem(int mapRows, int mapCols) {
        if (mapRows <= 0 || mapCols <= 0) {
            throw new IllegalArgumentException("Map size must be positive.");
        }
        this.mapRows = mapRows;
        this.mapCols = mapCols;
    }

    /**
     * Executes getMapRows.
     */
    public int getMapRows() {
        return mapRows;
    }

    /**
     * Executes getMapCols.
     */
    public int getMapCols() {
        return mapCols;
    }

    /**
     * Executes getMiniTileWidth.
     */
    public double getMiniTileWidth(double minimapWidth) {
        return minimapWidth / mapCols;
    }

    /**
     * Executes getMiniTileHeight.
     */
    public double getMiniTileHeight(double minimapHeight) {
        return minimapHeight / mapRows;
    }

    /**
     * Executes toMiniMapX.
     */
    public double toMiniMapX(GridPos pos, double minimapWidth) {
        return pos.getCol() * getMiniTileWidth(minimapWidth);
    }

    /**
     * Executes toMiniMapY.
     */
    public double toMiniMapY(GridPos pos, double minimapHeight) {
        return pos.getRow() * getMiniTileHeight(minimapHeight);
    }
}

