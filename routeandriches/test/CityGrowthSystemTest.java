package routeandriches.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import routeandriches.model.DecorationType;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.TileType;

public class CityGrowthSystemTest {

    @Test
    void growthLevelShouldIncreaseFromTransportActivity() {
        CityGrowthSystem growthSystem = new CityGrowthSystem();
        GameMap map = new GameMap(20, 20);

        growthSystem.update(0.5, map, List.of(), List.of());
        growthSystem.registerTransportActivity(new GridPos(10, 10), 35, 35);
        growthSystem.update(1.0, map, List.of(), List.of());

        assertTrue(growthSystem.getGrowthLevel() > 1);
        assertTrue(growthSystem.getGlobalDemandMultiplier() > 1.0);
    }

    @Test
    void demandMultiplierShouldBeHigherNearActiveTransportZones() {
        CityGrowthSystem growthSystem = new CityGrowthSystem();
        GameMap map = new GameMap(20, 20);

        clearMapAsBuildableLots(map);
        growthSystem.update(0.5, map, List.of(), List.of());

        for (int i = 0; i < 8; i++) {
            growthSystem.registerTransportActivity(new GridPos(8, 8), 8, 8);
        }
        growthSystem.update(1.0, map, List.of(), List.of());

        double hotZoneDemand = growthSystem.getDemandMultiplierAt(new GridPos(8, 8));
        double farZoneDemand = growthSystem.getDemandMultiplierAt(new GridPos(1, 1));

        assertTrue(hotZoneDemand > farZoneDemand);
    }

    @Test
    void expansionShouldConvertSomeBuildingTilesToBuildableLots() {
        CityGrowthSystem growthSystem = new CityGrowthSystem();
        GameMap map = new GameMap(30, 30);
        fillWithBuildings(map);

        growthSystem.update(0.5, map, List.of(), List.of());
        for (int i = 0; i < 20; i++) {
            growthSystem.registerTransportActivity(new GridPos(15, 15), 18, 18);
            growthSystem.update(1.0, map, List.of(), List.of());
        }

        int buildableCount = countBuildableLots(map);
        assertTrue(buildableCount > 0);
    }

    private void fillWithBuildings(GameMap map) {
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                map.setTile(row, col, TileType.BUILDING, false, DecorationType.NONE, 0);
            }
        }
        map.refreshRoadShapes();
    }

    private void clearMapAsBuildableLots(GameMap map) {
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                map.setTile(row, col, TileType.EMPTY, true, DecorationType.NONE, 0);
            }
        }
        map.refreshRoadShapes();
    }

    private int countBuildableLots(GameMap map) {
        int count = 0;
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col).isBuildable()) {
                    count++;
                }
            }
        }
        return count;
    }
}
