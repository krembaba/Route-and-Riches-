package routeandriches.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Stop {

    private final String id;
    private final String name;
    private final GridPos position;
    private final ArrayDeque<Passenger> waitingPassengers;

    public Stop(String id, String name, GridPos position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.position = Objects.requireNonNull(position, "position cannot be null");
        this.waitingPassengers = new ArrayDeque<>();
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

    public void addWaitingPassenger(Passenger passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        waitingPassengers.addLast(passenger);
    }

    public Passenger pollWaitingPassenger() {
        return waitingPassengers.pollFirst();
    }

    public Passenger peekWaitingPassenger() {
        return waitingPassengers.peekFirst();
    }

    public int getWaitingCount() {
        return waitingPassengers.size();
    }

    public List<Passenger> getWaitingPassengers() {
        return Collections.unmodifiableList(new ArrayList<>(waitingPassengers));
    }

    @Override
    public String toString() {
        return name + " (" + position.getRow() + ", " + position.getCol() + ")";
    }
}
