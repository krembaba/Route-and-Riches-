package routeandriches.model;

import java.util.Random;

/**

 * Represents the MapGenerator component.

 */

public class MapGenerator {
    private final Random random;

    /**
     * Creates a new MapGenerator instance.
     */
    public MapGenerator() {
        this.random = new Random(42L);
    }

    /**
     * Executes generateStarterRoadNetwork.
     */
    public void generateStarterRoadNetwork(GameMap map) {
        if (map == null) {
            throw new IllegalArgumentException("GameMap cannot be null.");
        }

        int rows = map.getRows();
        int cols = map.getCols();

        fillBaseDistricts(map, rows, cols);
        carveMainAvenues(map, rows, cols);
        carveSecondaryRoads(map, rows, cols);
        createStarterDevelopmentZones(map, rows, cols);
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

                // Keep a few open plots globally, then create bigger buildable clusters later.
                double openChance = border ? 0.26 : 0.18;
                if (random.nextDouble() < openChance) {
                    DecorationType openDecoration = random.nextDouble() < 0.40
                            ? DecorationType.BUSH : DecorationType.NONE;
                    map.setTile(row, col, TileType.EMPTY, true, openDecoration, variant);
                } else {
                    map.setTile(row, col, TileType.BUILDING, false, decoration, variant);
                }
            }
        }
    }

    private void carveMainAvenues(GameMap map, int rows, int cols) {
        int[] avenueRows = {rows / 3, (rows * 2) / 3};
        int[] avenueCols = {cols / 3, (cols * 2) / 3};

        for (int row : avenueRows) {
            carveHorizontal(map, row, 2, cols - 3, 2);
        }
        for (int col : avenueCols) {
            carveVertical(map, col, 2, rows - 3, 2);
        }
    }

    private void carveSecondaryRoads(GameMap map, int rows, int cols) {
        // Connected collector roads keep the city easy to expand while still sparser than a full grid.
        for (int row = 8; row < rows - 8; row += 14) {
            carveHorizontal(map, row, 2, cols - 3, 1);
        }
        for (int col = 10; col < cols - 10; col += 18) {
            carveVertical(map, col, 2, rows - 3, 1);
        }

        // Small connectors around the middle reduce dead-zones for the first few routes.
        int centerRow = rows / 2;
        int centerCol = cols / 2;
        carveHorizontal(map, centerRow - 4, Math.max(2, centerCol - 10), Math.min(cols - 3, centerCol + 10), 1);
        carveHorizontal(map, centerRow + 4, Math.max(2, centerCol - 10), Math.min(cols - 3, centerCol + 10), 1);
        carveVertical(map, centerCol - 6, Math.max(2, centerRow - 8), Math.min(rows - 3, centerRow + 8), 1);
        carveVertical(map, centerCol + 6, Math.max(2, centerRow - 8), Math.min(rows - 3, centerRow + 8), 1);
    }

    private void createStarterDevelopmentZones(GameMap map, int rows, int cols) {
        // 1) Open continuous buildable shoulders around roads so players can extend routes easily.
        for (int row = 2; row < rows - 2; row++) {
            for (int col = 2; col < cols - 2; col++) {
                if (!map.isRoad(row, col)) {
                    continue;
                }

                for (int dr = -2; dr <= 2; dr++) {
                    for (int dc = -2; dc <= 2; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }

                        int targetRow = row + dr;
                        int targetCol = col + dc;
                        if (!map.isWithinBounds(targetRow, targetCol) || map.isRoad(targetRow, targetCol)) {
                            continue;
                        }

                        int distance = Math.abs(dr) + Math.abs(dc);
                        double chance = switch (distance) {
                            case 1 -> 0.92;
                            case 2 -> 0.70;
                            case 3 -> 0.42;
                            default -> 0.22;
                        };

                        maybeConvertToBuildableLot(map, targetRow, targetCol, chance);
                    }
                }
            }
        }

        // 2) Add a few larger starter hubs to reduce scattered feeling for early gameplay.
        int[][] hubs = {
                {rows / 2, cols / 2},
                {rows / 3, cols / 3},
                {(rows * 2) / 3, (cols * 2) / 3}
        };

        for (int[] hub : hubs) {
            openBuildableHub(map, hub[0], hub[1], 6, 8);
        }
    }

    private void addParks(GameMap map, int rows, int cols) {
        for (int i = 0; i < 8; i++) {
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

    private void openBuildableHub(GameMap map, int centerRow, int centerCol, int halfHeight, int halfWidth) {
        int startRow = Math.max(2, centerRow - halfHeight);
        int endRow = Math.min(map.getRows() - 3, centerRow + halfHeight);
        int startCol = Math.max(2, centerCol - halfWidth);
        int endCol = Math.min(map.getCols() - 3, centerCol + halfWidth);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (map.isRoad(row, col)) {
                    continue;
                }

                int distance = Math.abs(row - centerRow) + Math.abs(col - centerCol);
                double chance = distance <= 4 ? 0.88 : 0.64;
                maybeConvertToBuildableLot(map, row, col, chance);
            }
        }
    }

    private void maybeConvertToBuildableLot(GameMap map, int row, int col, double chance) {
        if (!map.isWithinBounds(row, col) || random.nextDouble() > chance) {
            return;
        }

        Tile tile = map.getTile(row, col);
        if (tile.isRoadLike()) {
            return;
        }

        if (tile.getType() == TileType.BUILDING || !tile.isBuildable()) {
            DecorationType decoration = random.nextDouble() < 0.25 ? DecorationType.BUSH : DecorationType.NONE;
            map.setTile(row, col, TileType.EMPTY, true, decoration, random.nextInt(4));
        }
    }

    private void addStarterStops(GameMap map, int rows, int cols) {
        int[][] stopCandidates = {
                {rows / 3 - 1, cols / 3 + 2},
                {rows / 2, cols / 2},
                {(rows * 2) / 3, (cols * 2) / 3 - 2},
                {rows / 2 - 2, cols / 3 - 1}
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

