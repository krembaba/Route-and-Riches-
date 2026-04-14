# Route-and-Riches: Work Done So Far

Date: 2026-04-14

## Summary

The following backend and launch groundwork has been implemented:

- Passenger domain model added
- Stop waiting queue support added
- Vehicle capacity and onboard-passenger handling added
- Passenger spawn system added and connected to the game loop
- Missing JavaFX `main-view.fxml` added so `Main.java` can load a view
- PowerShell run script added and improved with safe-render options

## Files Added

- `routeandriches/model/enums/PassengerState.java`
- `routeandriches/model/Passenger.java`
- `routeandriches/system/PassengerSystem.java`
- `routeandriches/main-view.fxml`
- `run-game.ps1`

## Files Updated

- `routeandriches/model/Stop.java`
- `routeandriches/model/enums/VehicleType.java`
- `routeandriches/model/Vehicle.java`
- `routeandriches/model/Game.java`
- `run-game.ps1` (extended with safe render and native lib path handling)

## Implemented Features

### 1) Passenger model

- New `Passenger` entity with validation:
  - `id`, `originStopId`, `destinationStopId`
  - prevents blank IDs
  - prevents `origin == destination`
- Passenger lifecycle:
  - `WAITING`, `ONBOARD`, `DELIVERED`

### 2) Stop waiting queue

`Stop` now supports waiting passengers via:

- `addWaitingPassenger(...)`
- `pollWaitingPassenger()`
- `peekWaitingPassenger()`
- `getWaitingCount()`
- `getWaitingPassengers()` (read-only snapshot)

### 3) Vehicle capacity + onboard list

`VehicleType` now defines:

- capacity
- default speed

`Vehicle` now supports:

- `getCapacity()`
- `getOnboardCount()`
- `getOnboardPassengers()`
- `hasFreeSeat()`
- `boardPassenger(...)`
- `dropOffPassengersAt(stopId)`

### 4) Passenger spawning system

New `PassengerSystem`:

- discovers/syncs stops from map/routes
- spawns passengers periodically while game runs
- avoids same origin/destination
- tracks total waiting passengers

Integrated in `Game.update(...)`:

- `passengerSystem.update(scaledDelta, gameMap, routes)`

### 5) Boarding, dropoff, revenue, and end conditions

`Game.update(...)` now also:

- updates `TrafficLightSystem`
- blocks vehicles from entering red-light tiles
- processes stop-arrival events
- drops off passengers at matching destination stop IDs
- boards waiting passengers while capacity allows
- awards money per delivered passenger
- evaluates win/lose conditions and stops simulation in terminal states

New game-level state exposure:

- `getTrafficLightSystem()`
- `getDeliveryReward()`
- `getTargetMoney()`
- `getEndReason()`

## Build/Run Status

- Model/system/persistence Java compile checks pass.
- Full JavaFX app launch currently depends on correct JavaFX SDK package with native DLLs.
- Current machine error indicates missing JavaFX native renderer DLL (`prism_sw.dll`) in SDK path.

## Next Technical Steps

- Implement boarding + dropoff flow integration in simulation
- Add revenue on delivery
- Integrate traffic-light effects into vehicle movement
- Add win/lose conditions to update loop
- Add tests + CI
