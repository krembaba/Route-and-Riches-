package routeandriches.system;

import java.util.List;
import java.util.Random;
import routeandriches.model.DecorationType;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Route;
import routeandriches.model.Tile;
import routeandriches.model.TileType;
import routeandriches.model.Vehicle;

/**
 * Expands city development over time based on transport activity and provides
 * demand multipliers used by the passenger system.
 */
public class CityGrowthSystem {

    private static final double ACTIVITY_TO_GROWTH_FACTOR = 1.0;
    private static final double PASSIVE_ROUTE_GROWTH_PER_SECOND = 0.18;
    private static final double PASSIVE_VEHICLE_GROWTH_PER_SECOND = 0.12;
    private static final double BASE_EXPANSION_INTERVAL_SECONDS = 9.0;
    private static final double HEAT_DECAY_PER_SECOND = 0.965;
    private static final double MAX_DEMAND_BOOST = 2.6;

    private final Random random;

    private int growthLevel;
    private double growthProgress;
    private double queuedTransportActivity;
    private double expansionTimerSeconds;

    private double[][] serviceHeat;
    private double[][] demandBoost;
    private int gridRows;
    private int gridCols;

    /**
     * Creates a new CityGrowthSystem instance.
     */
    public CityGrowthSystem() {
        this.random = new Random(84L);
        this.growthLevel = 1;
        this.growthProgress = 0.0;
        this.queuedTransportActivity = 0.0;
        this.expansionTimerSeconds = 0.0;
        this.gridRows = 0;
        this.gridCols = 0;
    }

    /**
     * Executes update.
     */
    public void update(double deltaSeconds, GameMap map, List<Route> routes, List<Vehicle> vehicles) {
        if (deltaSeconds <= 0.0 || map == null) {
            return;
        }

        ensureBuffers(map);
        queuePassiveTransportActivity(deltaSeconds, routes, vehicles);

        growthProgress += queuedTransportActivity * ACTIVITY_TO_GROWTH_FACTOR;
        queuedTransportActivity = 0.0;

        while (growthProgress >= growthThresholdForLevel(growthLevel)) {
            growthProgress -= growthThresholdForLevel(growthLevel);
            growthLevel++;
        }

        decayHeat(deltaSeconds);

        expansionTimerSeconds += deltaSeconds;
        double expansionInterval = Math.max(3.6, BASE_EXPANSION_INTERVAL_SECONDS - (growthLevel - 1) * 0.28);
        while (expansionTimerSeconds >= expansionInterval) {
            expansionTimerSeconds -= expansionInterval;
            expandAroundActiveCorridor(map);
        }
    }

    /**
     * Executes registerTransportActivity.
     */
    public void registerTransportActivity(GridPos stopPosition, int boardedCount, int deliveredCount) {
        int boarded = Math.max(0, boardedCount);
        int delivered = Math.max(0, deliveredCount);
        double points = boarded + (delivered * 2.0);

        if (points <= 0.0) {
            return;
        }

        queuedTransportActivity += points;

        if (stopPosition != null && isValidCell(stopPosition.getRow(), stopPosition.getCol())) {
            addInfluence(stopPosition.getRow(), stopPosition.getCol(), points * 0.24 + 1.2, 4);
        }
    }

    /**
     * Executes getGrowthLevel.
     */
    public int getGrowthLevel() {
        return growthLevel;
    }

    /**
     * Executes getGlobalDemandMultiplier.
     */
    public double getGlobalDemandMultiplier() {
        return 1.0 + Math.min(1.6, (growthLevel - 1) * 0.11);
    }

    /**
     * Executes getDemandMultiplierAt.
     */
    public double getDemandMultiplierAt(GridPos position) {
        double global = getGlobalDemandMultiplier();

        if (position == null || !isValidCell(position.getRow(), position.getCol()) || demandBoost == null) {
            return global;
        }

        int row = position.getRow();
        int col = position.getCol();
        double local = demandBoost[row][col];
        local += averageNeighborBoost(row, col) * 0.35;

        return clamp(1.0, 4.8, global + local);
    }

    /**
     * Executes getProgressToNextLevel.
     */
    public double getProgressToNextLevel() {
        double threshold = growthThresholdForLevel(growthLevel);
        if (threshold <= 0.0) {
            return 1.0;
        }
        return clamp(0.0, 1.0, growthProgress / threshold);
    }

    private void queuePassiveTransportActivity(double deltaSeconds, List<Route> routes, List<Vehicle> vehicles) {
        int routeCount = routes == null ? 0 : routes.size();
        int vehicleCount = vehicles == null ? 0 : vehicles.size();

        queuedTransportActivity += routeCount * PASSIVE_ROUTE_GROWTH_PER_SECOND * deltaSeconds;
        queuedTransportActivity += vehicleCount * PASSIVE_VEHICLE_GROWTH_PER_SECOND * deltaSeconds;

        if (vehicles == null) {
            return;
        }

        for (Vehicle vehicle : vehicles) {
            if (vehicle == null) {
                continue;
            }
            GridPos current = vehicle.getCurrentPathPosition();
            if (current != null && isValidCell(current.getRow(), current.getCol())) {
                addInfluence(current.getRow(), current.getCol(), 0.14 * deltaSeconds, 2);
            }
        }
    }

    private void expandAroundActiveCorridor(GameMap map) {
        if (serviceHeat == null || demandBoost == null) {
            return;
        }

        GridPos hotspot = findHotspot();
        int centerRow = hotspot.getRow();
        int centerCol = hotspot.getCol();
        int radius = Math.min(8, 3 + growthLevel / 2);

        for (int row = centerRow - radius; row <= centerRow + radius; row++) {
            for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                if (!map.isWithinBounds(row, col)) {
                    continue;
                }

                int manhattan = Math.abs(row - centerRow) + Math.abs(col - centerCol);
                if (manhattan > radius) {
                    continue;
                }

                double heat = serviceHeat[row][col];
                double distancePenalty = manhattan * 0.018;
                double growthFactor = (growthLevel - 1) * 0.03;
                double developChance = 0.06 + (heat * 0.09) + growthFactor - distancePenalty;

                Tile tile = map.getTile(row, col);
                if (!tile.isRoadLike() && tile.getType() == TileType.BUILDING && random.nextDouble() < developChance) {
                    DecorationType decoration = random.nextDouble() < 0.25
                            ? DecorationType.BUSH : DecorationType.NONE;
                    map.setTile(row, col, TileType.EMPTY, true, decoration, random.nextInt(4));
                }

                double boostGain = Math.max(0.02, 0.17 - manhattan * 0.015 + heat * 0.03);
                demandBoost[row][col] = clamp(0.0, MAX_DEMAND_BOOST, demandBoost[row][col] + boostGain);
            }
        }
    }

    private GridPos findHotspot() {
        int fallbackRow = Math.max(0, gridRows / 2);
        int fallbackCol = Math.max(0, gridCols / 2);
        int bestRow = fallbackRow;
        int bestCol = fallbackCol;
        double bestScore = -1.0;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                double jitter = 0.9 + random.nextDouble() * 0.2;
                double score = serviceHeat[row][col] * jitter;
                if (score > bestScore) {
                    bestScore = score;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        return new GridPos(bestRow, bestCol);
    }

    private void addInfluence(int centerRow, int centerCol, double strength, int radius) {
        if (strength <= 0.0 || serviceHeat == null || demandBoost == null) {
            return;
        }

        for (int row = centerRow - radius; row <= centerRow + radius; row++) {
            for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                if (!isValidCell(row, col)) {
                    continue;
                }

                int distance = Math.abs(row - centerRow) + Math.abs(col - centerCol);
                if (distance > radius) {
                    continue;
                }

                double attenuation = 1.0 / (1.0 + distance);
                serviceHeat[row][col] += strength * attenuation;
                demandBoost[row][col] = clamp(
                        0.0,
                        MAX_DEMAND_BOOST,
                        demandBoost[row][col] + strength * attenuation * 0.016);
            }
        }
    }

    private double averageNeighborBoost(int row, int col) {
        double total = 0.0;
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int nr = row + dr;
                int nc = col + dc;
                if (!isValidCell(nr, nc)) {
                    continue;
                }
                total += demandBoost[nr][nc];
                count++;
            }
        }

        return count == 0 ? 0.0 : total / count;
    }

    private void decayHeat(double deltaSeconds) {
        if (serviceHeat == null || demandBoost == null) {
            return;
        }

        double heatFactor = Math.pow(HEAT_DECAY_PER_SECOND, deltaSeconds);
        double demandFactor = Math.pow(0.995, deltaSeconds);

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                serviceHeat[row][col] *= heatFactor;
                demandBoost[row][col] *= demandFactor;
            }
        }
    }

    private void ensureBuffers(GameMap map) {
        int rows = map.getRows();
        int cols = map.getCols();

        if (rows == gridRows && cols == gridCols && serviceHeat != null && demandBoost != null) {
            return;
        }

        gridRows = rows;
        gridCols = cols;
        serviceHeat = new double[rows][cols];
        demandBoost = new double[rows][cols];
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < gridRows && col >= 0 && col < gridCols;
    }

    private double growthThresholdForLevel(int level) {
        return 36.0 + (level - 1) * 16.0;
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }
}
