# Part B Completion Update (2026-04-14)

## Completed in this pass

1. Construction economy rules enforced
- Road placement now costs money.
- Stop placement now costs money.
- Insufficient funds blocks placement with clear player hint.

2. Traffic light gameplay wiring
- Added new interaction mode: `PLACE_TRAFFIC_LIGHT`.
- Added toolbar button to enter traffic-light mode.
- Click intersection to place a light (cost applied).
- Click existing light to cycle timing presets.
- Right-click existing light to remove it.
- Traffic lights are rendered on the map for visibility.

3. Persistence expanded for simulation state
- Save now stores:
  - game state
  - game speed
  - elapsed time
  - traffic light runtime data
  - passenger runtime data (waiting + onboard)
- Load now restores the same data.

4. Financial lose condition improved
- Lose now triggers when the player cannot afford any core action and has no vehicles.

## Files changed
- `routeandriches/model/Game.java`
- `routeandriches/system/GameClock.java`
- `routeandriches/system/TrafficLightSystem.java`
- `routeandriches/model/TrafficLight.java`
- `routeandriches/system/PassengerSystem.java`
- `routeandriches/model/Vehicle.java`
- `routeandriches/persistence/GameSnapshot.java`
- `routeandriches/persistence/SaveService.java`
- `routeandriches/controller/InteractionMode.java`
- `routeandriches/controller/GameController.java`
- `routeandriches/main-view.fxml`

## Validation
- Backend compile check passed for non-JavaFX packages:
  - `model`
  - `model/enums`
  - `system`
  - `persistence`
