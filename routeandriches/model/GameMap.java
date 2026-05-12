package routeandriches.model;

/**

 * Represents the GameMap component.

 */

public class GameMap {

    private final int rows;
    private final int cols;
    private final Tile[][] tiles;

    /**
     * Creates a new GameMap instance.
     */
    public GameMap(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Map size must be positive.");
        }

        this.rows = rows;
        this.cols = cols;
        this.tiles = new Tile[rows][cols];

        initializeEmptyMap();
        new MapGenerator().generateStarterRoadNetwork(this);
        refreshRoadShapes();
    }

    private void initializeEmptyMap() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tiles[row][col] = new Tile(TileType.EMPTY);
            }
        }
    }

    /**
     * Executes getTile.
     */
    public Tile getTile(int row, int col) {
        validatePosition(row, col);
        return tiles[row][col];
    }

    /**
     * Executes isWithinBounds.
     */
    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Executes placeRoad.
     */
    public boolean placeRoad(int row, int col) {
        validatePosition(row, col);
        Tile tile = tiles[row][col];
        if (!tile.isBuildable() || tile.isRoadLike()) {
            return false;
        }

        tile.setType(TileType.ROAD);
        tile.setBuildable(false);
        tile.setDecorationType(DecorationType.NONE);
        refreshRoadShapesAround(row, col);
        return true;
    }

    /**
     * Executes placeStop.
     */
    public boolean placeStop(int row, int col) {
        validatePosition(row, col);
        Tile tile = tiles[row][col];

        if (!tile.isBuildable() || tile.isRoadLike() || !isRoadAdjacent(row, col)) {
            return false;
        }

        tile.setType(TileType.STOP);
        tile.setBuildable(false);
        tile.setDecorationType(DecorationType.NONE);
        refreshRoadShapesAround(row, col);
        return true;
    }

    /**
     * Executes canPlaceRoad.
     */
    public boolean canPlaceRoad(int row, int col) {
        validatePosition(row, col);
        Tile tile = tiles[row][col];
        return tile.isBuildable() && !tile.isRoadLike();
    }

    /**
     * Executes canPlaceStop.
     */
    public boolean canPlaceStop(int row, int col) {
        validatePosition(row, col);
        Tile tile = tiles[row][col];
        return tile.isBuildable() && !tile.isRoadLike() && isRoadAdjacent(row, col);
    }

    /**
     * Executes isRoad.
     */
    public boolean isRoad(int row, int col) {
        validatePosition(row, col);
        return tiles[row][col].isRoadLike();
    }

    /**
     * Executes isBuildable.
     */
    public boolean isBuildable(int row, int col) {
        validatePosition(row, col);
        return tiles[row][col].isBuildable();
    }

    /**
     * Executes isRoadAdjacent.
     */
    public boolean isRoadAdjacent(int row, int col) {
        return isRoadLikeAt(row - 1, col)
                || isRoadLikeAt(row + 1, col)
                || isRoadLikeAt(row, col - 1)
                || isRoadLikeAt(row, col + 1);
    }

    /**
     * Executes setTile.
     */
    public void setTile(int row, int col, TileType type, boolean buildable,
                        DecorationType decorationType, int visualVariant) {
        validatePosition(row, col);
        Tile tile = tiles[row][col];
        tile.setType(type);
        tile.setBuildable(buildable);
        tile.setDecorationType(decorationType);
        tile.setVisualVariant(visualVariant);
    }

    /**
     * Executes refreshRoadShapes.
     */
    public void refreshRoadShapes() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                updateRoadShapeAt(row, col);
            }
        }
    }

    /**
     * Executes refreshRoadShapesAround.
     */
    public void refreshRoadShapesAround(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (isWithinBounds(r, c)) {
                    updateRoadShapeAt(r, c);
                }
            }
        }
    }

    private void updateRoadShapeAt(int row, int col) {
        Tile tile = tiles[row][col];
        if (!tile.isRoadLike()) {
            tile.setRoadShape(RoadShape.NONE);
            return;
        }

        boolean north = isRoadLikeAt(row - 1, col);
        boolean south = isRoadLikeAt(row + 1, col);
        boolean west = isRoadLikeAt(row, col - 1);
        boolean east = isRoadLikeAt(row, col + 1);

        int count = (north ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0) + (east ? 1 : 0);

        if (count == 4) {
            tile.setRoadShape(RoadShape.CROSS);
        } else if (count == 3) {
            if (!north) {
                tile.setRoadShape(RoadShape.T_UP);
            } else if (!south) {
                tile.setRoadShape(RoadShape.T_DOWN);
            } else if (!west) {
                tile.setRoadShape(RoadShape.T_LEFT);
            } else {
                tile.setRoadShape(RoadShape.T_RIGHT);
            }
        } else if (count == 2) {
            if (north && south) {
                tile.setRoadShape(RoadShape.STRAIGHT_VERTICAL);
            } else if (west && east) {
                tile.setRoadShape(RoadShape.STRAIGHT_HORIZONTAL);
            } else if (north && east) {
                tile.setRoadShape(RoadShape.CORNER_NE);
            } else if (north && west) {
                tile.setRoadShape(RoadShape.CORNER_NW);
            } else if (south && east) {
                tile.setRoadShape(RoadShape.CORNER_SE);
            } else {
                tile.setRoadShape(RoadShape.CORNER_SW);
            }
        } else if (count == 1) {
            if (north) {
                tile.setRoadShape(RoadShape.DEAD_END_N);
            } else if (south) {
                tile.setRoadShape(RoadShape.DEAD_END_S);
            } else if (east) {
                tile.setRoadShape(RoadShape.DEAD_END_E);
            } else {
                tile.setRoadShape(RoadShape.DEAD_END_W);
            }
        } else {
            tile.setRoadShape(RoadShape.CROSS);
        }
    }

    private boolean isRoadLikeAt(int row, int col) {
        return isWithinBounds(row, col) && tiles[row][col].isRoadLike();
    }

    /**
     * Executes getRows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Executes getCols.
     */
    public int getCols() {
        return cols;
    }

    private void validatePosition(int row, int col) {
        if (!isWithinBounds(row, col)) {
            throw new IllegalArgumentException("Invalid map position: (" + row + ", " + col + ")");
        }
    }
}

