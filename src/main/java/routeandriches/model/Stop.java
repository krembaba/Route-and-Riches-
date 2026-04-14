package routeandriches.model;

import java.util.Objects;

public class Stop {

    private final String id;
    private final String name;
    private final GridPos position;

    public Stop(String id, String name, GridPos position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GridPos getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return name + " (" + position.getRow() + ", " + position.getCol() + ")";
    }
}