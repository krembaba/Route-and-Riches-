package routeandriches.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 * Represents the Route component.

 */

public class Route {

    private final String name;
    private final List<GridPos> stops;
    private final List<GridPos> path;

    /**
     * Creates a new Route instance.
     */
    public Route(String name, List<GridPos> stops, List<GridPos> path) {
        this.name = name;
        this.stops = new ArrayList<>(stops);
        this.path = new ArrayList<>(path);
    }

    /**
     * Executes getName.
     */
    public String getName() {
        return name;
    }

    /**
     * Executes getStops.
     */
    public List<GridPos> getStops() {
        return Collections.unmodifiableList(stops);
    }

    /**
     * Executes getPath.
     */
    public List<GridPos> getPath() {
        return Collections.unmodifiableList(path);
    }

    /**
     * Executes isValid.
     */
    public boolean isValid() {
        return stops.size() >= 2 && path.size() >= 2;
    }

    /**
     * Executes toSaveString.
     */
    public String toSaveString() {
        return name + "#" + encode(stops) + "#" + encode(path);
    }

    private String encode(List<GridPos> positions) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < positions.size(); i++) {
            GridPos pos = positions.get(i);
            sb.append(pos.getRow()).append(",").append(pos.getCol());
            if (i < positions.size() - 1) {
                sb.append(";");
            }
        }

        return sb.toString();
    }

    /**
     * Executes fromSaveString.
     */
    public static Route fromSaveString(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        String[] parts = data.split("#", -1);
        if (parts.length != 3) {
            return null;
        }

        try {
            List<GridPos> loadedStops = decode(parts[1]);
            List<GridPos> loadedPath = decode(parts[2]);

            Route route = new Route(parts[0], loadedStops, loadedPath);
            return route.isValid() ? route : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<GridPos> decode(String encoded) {
        List<GridPos> result = new ArrayList<>();

        if (encoded == null || encoded.isBlank()) {
            return result;
        }

        String[] entries = encoded.split(";");
        for (String entry : entries) {
            String[] coords = entry.split(",");
            if (coords.length != 2) {
                continue;
            }

            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            result.add(new GridPos(row, col));
        }

        return result;
    }
}
