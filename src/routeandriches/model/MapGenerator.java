package routeandriches.model;

import java.util.Random;

public class MapGenerator {
    private final Random random;

    public MapGenerator() {
        this.random = new Random(42L);
    }

    public void generateStarterRoadNetwork(GameMap map) {
        if (map == null) {
            throw new IllegalArgumentException("GameMap cannot be null.");
        }

        int rows = map.getRows();
        int cols = map.getCols();

        fillBaseDistricts(map, rows, cols);
        carveMainAvenues(map, rows, cols);
        carveSecondaryRoads(map, rows, cols);
        addParks(map, rows, cols);
        addStarterStops(map, rows, cols);
        decorateBlocks(map, rows, cols);
        map.refreshRoadShapes();
    }

    private void fillBaseDistricts(GameMap map, int rows, int cols) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                boolean border = row < 2 || row >= rows - 2 || col < 2 || col >= cols - 2;
                DecorationType decoration = border && random.nextDouble() < 0.35
                        ? DecorationType.TREE : DecorationType.NONE;
                int variant = (row * 17 + col * 11) % 4;
                map.setTile(row, col, TileType.BUILDING, false, decoration, variant);
            }
        }
    }

    private void carveMainAvenues(GameMap map, int rows, int cols) {
        int[] avenueRows = {rows / 4, rows / 2, (rows * 3) / 4};
        int[] avenueCols = {cols / 4, cols / 2, (cols * 3) / 4};

        for (int row : avenueRows) {
            carveHorizontal(map, row, 2, cols - 3, 2);
        }
        for (int col : avenueCols) {
            carveVertical(map, col, 2, rows - 3, 2);
        }
    }

    private void carveSecondaryRoads(GameMap map, int rows, int cols) {
        for (int row = 5; row < rows - 5; row += 6) {
            int start = 2 + random.nextInt(4);
            carveHorizontal(map, row, start, cols - 3, 1);
        }
        for (int col = 6; col < cols - 6; col += 8) {
            int start = 2 + random.nextInt(3);
            carveVertical(map, col, start, rows - 3, 1);
        }

        // Short connectors to avoid a perfect grid.
        for (int i = 0; i < 18; i++) {
            int row = 3 + random.nextInt(rows - 6);
            int col = 3 + random.nextInt(cols - 6);
            if (random.nextBoolean()) {
                carveHorizontal(map, row, Math.max(2, col - 2), Math.min(cols - 3, col + 2), 1);
            } else {
                carveVertical(map, col, Math.max(2, row - 2), Math.min(rows - 3, row + 2), 1);
            }
        }
    }

    private void addParks(GameMap map, int rows, int cols) {
        for (int i = 0; i < 6; i++) {
            int parkHeight = 2 + random.nextInt(3);
            int parkWidth = 3 + random.nextInt(4);
            int startRow = 3 + random.nextInt(Math.max(1, rows - parkHeight - 6));
            int startCol = 3 + random.nextInt(Math.max(1, cols - parkWidth - 6));

            for (int row = startRow; row < startRow + parkHeight; row++) {
                for (int col = startCol; col < startCol + parkWidth; col++) {
                    if (!map.isRoad(row, col)) {
                        DecorationType decoration = random.nextDouble() < 0.25
                                ? DecorationType.FLOWER_BED : DecorationType.TREE;
                        map.setTile(row, col, TileType.PARK, true, decoration, random.nextInt(4));
                    }
                }
            }
        }
    }

    private void addStarterStops(GameMap map, int rows, int cols) {
        int[][] stopCandidates = {
                {rows / 4 - 1, cols / 4 + 2},
                {rows / 2 + 1, cols / 2 - 2},
                {(rows * 3) / 4 - 1, (cols * 3) / 4 + 1},
                {rows / 2 - 2, cols / 4 - 1}
        };

        for (int[] pos : stopCandidates) {
            int row = Math.max(2, Math.min(rows - 3, pos[0]));
            int col = Math.max(2, Math.min(cols - 3, pos[1]));
            if (!map.isRoad(row, col) && map.canPlaceStop(row, col)) {
                map.placeStop(row, col);
            }
        }
    }

    private void decorateBlocks(GameMap map, int rows, int cols) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = map.getTile(row, col);
                if (tile.getType() == TileType.BUILDING) {
                    boolean nearRoad = map.isRoadAdjacent(row, col);
                    if (nearRoad && random.nextDouble() < 0.12) {
                        tile.setDecorationType(DecorationType.LAMP);
                    } else if (!nearRoad && random.nextDouble() < 0.08) {
                        tile.setDecorationType(DecorationType.PLAZA);
                    }
                } else if (tile.getType() == TileType.EMPTY && random.nextDouble() < 0.15) {
                    tile.setDecorationType(DecorationType.BUSH);
                }
            }
        }
    }

    private void carveHorizontal(GameMap map, int row, int startCol, int endCol, int width) {
        for (int r = row; r < row + width; r++) {
            if (r < 0 || r >= map.getRows()) {
                continue;
            }
            for (int col = startCol; col <= endCol; col++) {
                if (!map.isWithinBounds(r, col)) {
                    continue;
                }
                map.setTile(r, col, TileType.ROAD, false, DecorationType.NONE, 0);
            }
        }
    }

    private void carveVertical(GameMap map, int col, int startRow, int endRow, int width) {
        for (int c = col; c < col + width; c++) {
            if (c < 0 || c >= map.getCols()) {
                continue;
            }
            for (int row = startRow; row <= endRow; row++) {
                if (!map.isWithinBounds(row, c)) {
                    continue;
                }
                map.setTile(row, c, TileType.ROAD, false, DecorationType.NONE, 0);
            }
        }
    }
}
