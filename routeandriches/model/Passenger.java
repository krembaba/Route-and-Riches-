package routeandriches.model;

import java.util.Objects;
import routeandriches.model.enums.PassengerState;

/**

 * Represents the Passenger component.

 */

public class Passenger {

    private final String id;
    private final String originStopId;
    private final String destinationStopId;
    private PassengerState state;

    /**
     * Creates a new Passenger instance.
     */
    public Passenger(String id, String originStopId, String destinationStopId) {
        this.id = requireNonBlank(id, "Passenger id cannot be blank.");
        this.originStopId = requireNonBlank(originStopId, "Origin stop id cannot be blank.");
        this.destinationStopId = requireNonBlank(destinationStopId, "Destination stop id cannot be blank.");

        if (this.originStopId.equals(this.destinationStopId)) {
            throw new IllegalArgumentException("Origin and destination must differ.");
        }

        this.state = PassengerState.WAITING;
    }

    /**
     * Executes getId.
     */
    public String getId() {
        return id;
    }

    /**
     * Executes getOriginStopId.
     */
    public String getOriginStopId() {
        return originStopId;
    }

    /**
     * Executes getDestinationStopId.
     */
    public String getDestinationStopId() {
        return destinationStopId;
    }

    /**
     * Executes getState.
     */
    public PassengerState getState() {
        return state;
    }

    /**
     * Executes setState.
     */
    public void setState(PassengerState state) {
        this.state = Objects.requireNonNull(state, "Passenger state cannot be null.");
    }

    /**
     * Executes markOnboard.
     */
    public void markOnboard() {
        setState(PassengerState.ONBOARD);
    }

    /**
     * Executes markDelivered.
     */
    public void markDelivered() {
        setState(PassengerState.DELIVERED);
    }

    /**
     * Executes isWaiting.
     */
    public boolean isWaiting() {
        return state == PassengerState.WAITING;
    }

    /**
     * Executes isOnboard.
     */
    public boolean isOnboard() {
        return state == PassengerState.ONBOARD;
    }

    /**
     * Executes isDelivered.
     */
    public boolean isDelivered() {
        return state == PassengerState.DELIVERED;
    }

    @Override
    /**
     * Executes toString.
     */
    public String toString() {
        return "Passenger{"
                + "id='" + id + '\''
                + ", originStopId='" + originStopId + '\''
                + ", destinationStopId='" + destinationStopId + '\''
                + ", state=" + state
                + '}';
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

