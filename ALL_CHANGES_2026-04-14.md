# Route-and-Riches: All Changes Summary (2026-04-14)

This file summarizes the full set of changes completed in this session.

## 1) Backend Simulation (Teammate 2 scope)

### Passenger and stop flow
- Added `Passenger` domain object and lifecycle enum.
- Added waiting queue behavior to `Stop`.
- Added vehicle passenger APIs:
  - seat capacity
  - board/drop-off behavior
  - onboard passenger inspection

Files:
- `routeandriches/model/Passenger.java`
- `routeandriches/model/enums/PassengerState.java`
- `routeandriches/model/Stop.java`
- `routeandriches/model/Vehicle.java`
- `routeandriches/model/enums/VehicleType.java`

### Passenger spawning
- Added `PassengerSystem` and integrated it into `Game.update(...)`.

Files:
- `routeandriches/system/PassengerSystem.java`
- `routeandriches/model/Game.java`

### Traffic lights, rewards, and end conditions
- Vehicles now check red lights before moving to the next tile.
- Stop-arrival processing now performs:
  - drop-off
  - boarding
  - delivery reward payout
- Added terminal conditions (win/lose) and end reason tracking.

Files:
- `routeandriches/model/Game.java`
- `routeandriches/model/Vehicle.java`

## 2) UI Layout Reframe

- Reworked `main-view.fxml` to match wireframe structure:
  - top control bar
  - left minimap zone
  - center map zone
  - right info panel
  - bottom action toolbar
- Removed placeholder labels from wireframe markers.
- Improved right panel readability (width and text wrapping).
- Restored separate buy actions:
  - Buy Bus
  - Buy Tram
- Hidden map scroll bars while keeping `pannable="true"` drag behavior.

File:
- `routeandriches/main-view.fxml`

## 3) Graphics Upgrade

- Upgraded map tile visuals:
  - richer ground texture/shading
  - enhanced road texture and lane detail
  - improved building roof detail
  - refined stop marker
- Replaced dot vehicles with directional top-down sprites for:
  - bus
  - tram
- Added UI polish styles for buttons/cards while preserving theme.

Files:
- `routeandriches/ui/MapRenderer.java`
- `routeandriches/controller/GameController.java`
- `routeandriches/main-view.fxml`

## 4) Runtime and Docs

- Added/improved launch script with JavaFX module-path handling:
  - `run-game.ps1`
- Added/updated documentation files:
  - `WORK_DONE_SO_FAR.md`
  - `TEAMMATE2_PROGRESS.md`
  - `RUN_GAME_WINDOWS.md`
  - `UI_LAYOUT_CHANGES_2026-04-14.md`
  - `GRAPHICS_UPGRADE_2026-04-14.md`
  - `ALL_CHANGES_2026-04-14.md` (this file)
