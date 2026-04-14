# Teammate 2 Progress Tracker (Backend Simulation)

Date: 2026-04-14

## Scope

Backend work for Milestone 3:

- passenger system
- economy/simulation flow
- traffic light simulation effects
- win/lose conditions

## Issue Status

1. `Add Passenger Domain Model`  
Status: Done

2. `Implement Stop Waiting Queue`  
Status: Done

3. `Add Vehicle Capacity + Onboard Passengers`  
Status: Done

4. `Create Passenger Spawn System`  
Status: Done

5. `Implement Boarding and Dropoff Flow`  
Status: Done

6. `Add Revenue on Passenger Delivery`  
Status: Done

7. `Integrate Traffic Lights into Vehicle Movement`  
Status: Done

8. `Implement Win/Lose Conditions in Game Loop`  
Status: Done

## API Surface Added

- `Passenger` model and `PassengerState` enum
- Stop queue operations in `Stop`
- Vehicle boarding/dropoff primitives in `Vehicle`
- Spawn/update stop-sync logic in `PassengerSystem`
- `Game.getPassengerSystem()`

## Integration Notes for Teammates

- UI teammate can read waiting counts through `game.getPassengerSystem()`.
- Persistence teammate should include passenger system state in save/load once boarding/dropoff is added.
- Keep route/stop IDs stable (`S_row_col`) to maintain passenger consistency.

## Immediate Next Backend Task

Backend issue set for Teammate 2 is now functionally implemented.

Recommended next:

- add focused unit tests for `Game.update(...)` integration paths
- wire traffic light placement/timing actions from UI to backend system
- include passenger and traffic light runtime data in persistence
