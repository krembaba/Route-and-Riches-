package routeandriches.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Route {

    private final String id;
    private final String name;
    private final List<Stop> stops;

    public Route(String id, String name) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.stops = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addStop(Stop stop) {
        stops.add(Objects.requireNonNull(stop, "stop cannot be null"));
    }

    public List<Stop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    public boolean isValid() {
        return stops.size() >= 2;
    }

    public int size() {
        return stops.size();
    }

    public Stop getStop(int index) {
        return stops.get(index);
    }
}