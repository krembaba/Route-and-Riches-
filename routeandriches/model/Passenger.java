package routeandriches.model;

import java.util.Objects;
import routeandriches.model.enums.PassengerState;

public class Passenger {

    private final String id;
    private final String originStopId;
    private final String destinationStopId;
    private PassengerState state;

    public Passenger(String id, String originStopId, String destinationStopId) {
        this.id = requireNonBlank(id, "Passenger id cannot be blank.");
        this.originStopId = requireNonBlank(originStopId, "Origin stop id cannot be blank.");
        this.destinationStopId = requireNonBlank(destinationStopId, "Destination stop id cannot be blank.");

        if (this.originStopId.equals(this.destinationStopId)) {
            throw new IllegalArgumentException("Origin and destination must differ.");
        }

        this.state = PassengerState.WAITING;
    }

    public String getId() {
        return id;
    }

    public String getOriginStopId() {
        return originStopId;
    }

    public String getDestinationStopId() {
        return destinationStopId;
    }

    public PassengerState getState() {
        return state;
    }

    public void setState(PassengerState state) {
        this.state = Objects.requireNonNull(state, "Passenger state cannot be null.");
    }

    public void markOnboard() {
        setState(PassengerState.ONBOARD);
    }

    public void markDelivered() {
        setState(PassengerState.DELIVERED);
    }

    public boolean isWaiting() {
        return state == PassengerState.WAITING;
    }

    public boolean isOnboard() {
        return state == PassengerState.ONBOARD;
    }

    public boolean isDelivered() {
        return state == PassengerState.DELIVERED;
    }

    @Override
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
