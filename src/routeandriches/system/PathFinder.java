package routeandriches.system;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import routeandriches.model.GameMap;
import routeandriches.model.GridPos;
import routeandriches.model.Tile;

public class PathFinder {

    private static final int[][] DIRECTIONS = {
        {-1, 0},
        {1, 0},
        {0, -1},
        {0, 1}
    };

    public List<GridPos> findPath(GameMap gameMap, GridPos start, GridPos goal) {
        if (gameMap == null || start == null || goal == null) {
            return Collections.emptyList();
        }

        if (!isRoadLike(gameMap, start) || !isRoadLike(gameMap, goal)) {
            return Collections.emptyList();
        }

        Queue<GridPos> queue = new ArrayDeque<>();
        Map<GridPos, GridPos> previous = new HashMap<>();

        queue.add(start);
        previous.put(start, null);

        while (!queue.isEmpty()) {
            GridPos current = queue.poll();

            if (current.equals(goal)) {
                return reconstructPath(previous, goal);
            }

            for (int[] dir : DIRECTIONS) {
                int nextRow = current.getRow() + dir[0];
                int nextCol = current.getCol() + dir[1];

                if (!gameMap.isWithinBounds(nextRow, nextCol)) {
                    continue;
                }

                GridPos next = new GridPos(nextRow, nextCol);

                if (!isRoadLike(gameMap, next)) {
                    continue;
                }

                if (previous.containsKey(next)) {
                    continue;
                }

                queue.add(next);
                previous.put(next, current);
            }
        }

        return Collections.emptyList();
    }

    private boolean isRoadLike(GameMap gameMap, GridPos pos) {
        Tile tile = gameMap.getTile(pos.getRow(), pos.getCol());
        return tile != null && tile.isRoadLike();
    }

    private List<GridPos> reconstructPath(Map<GridPos, GridPos> previous, GridPos goal) {
        List<GridPos> path = new ArrayList<>();
        GridPos current = goal;

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}