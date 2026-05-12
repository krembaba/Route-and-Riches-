package routeandriches.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**

 * Represents the Stop component.

 */

public class Stop {

    private final String id;
    private final String name;
    private final GridPos position;
    private final ArrayDeque<Passenger> waitingPassengers;

    /**
     * Creates a new Stop instance.
     */
    public Stop(String id, String name, GridPos position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.position = Objects.requireNonNull(position, "position cannot be null");
        this.waitingPassengers = new ArrayDeque<>();
    }

    /**
     * Executes getId.
     */
    public String getId() {
        return id;
    }

    /**
     * Executes getName.
     */
    public String getName() {
        return name;
    }

    /**
     * Executes getPosition.
     */
    public GridPos getPosition() {
        return position;
    }

    /**
     * Executes addWaitingPassenger.
     */
    public void addWaitingPassenger(Passenger passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        waitingPassengers.addLast(passenger);
    }

    /**
     * Executes pollWaitingPassenger.
     */
    public Passenger pollWaitingPassenger() {
        return waitingPassengers.pollFirst();
    }

    /**
     * Executes peekWaitingPassenger.
     */
    public Passenger peekWaitingPassenger() {
        return waitingPassengers.peekFirst();
    }

    /**
     * Executes getWaitingCount.
     */
    public int getWaitingCount() {
        return waitingPassengers.size();
    }

    /**
     * Executes getWaitingPassengers.
     */
    public List<Passenger> getWaitingPassengers() {
        return Collections.unmodifiableList(new ArrayList<>(waitingPassengers));
    }

    @Override
    /**
     * Executes toString.
     */
    public String toString() {
        return name + " (" + position.getRow() + ", " + position.getCol() + ")";
    }
}

