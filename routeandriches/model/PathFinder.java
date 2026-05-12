package routeandriches.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**

 * Represents the PathFinder component.

 */

public class PathFinder {

    private static final int[][] DIRECTIONS = {
        {-1, 0},
        {1, 0},
        {0, -1},
        {0, 1}
    };

    /**
     * Executes findPath.
     */
    public List<GridPos> findPath(GameMap gameMap, GridPos start, GridPos goal) {
        if (gameMap == null || start == null || goal == null) {
            return Collections.emptyList();
        }

        if (!gameMap.isWithinBounds(start.getRow(), start.getCol())
                || !gameMap.isWithinBounds(goal.getRow(), goal.getCol())) {
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

                if (!gameMap.isRoad(nextRow, nextCol)) {
                    continue;
                }

                GridPos next = new GridPos(nextRow, nextCol);

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
        return gameMap.isRoad(pos.getRow(), pos.getCol());
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
