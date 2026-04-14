# Map Generator Early-Game Tuning (2026-04-14)

## Goal
Make the starting city easier to build in by reducing scattered buildable tiles and creating more contiguous areas around the starter road network.

## File Updated
- `routeandriches/model/MapGenerator.java`

## What Changed
1. Added `createStarterDevelopmentZones(...)` into generation flow (after roads, before parks).
2. Reworked secondary road carving to produce connected collector roads:
- horizontal collectors every ~14 rows
- vertical collectors every ~18 columns
- extra short connectors around map center
3. Added road-adjacent buildable shoulders:
- tiles near roads are now converted to buildable lots with distance-based probability
- high chance at distance 1, lower chance farther away
4. Added clustered starter hubs:
- central and two diagonal hubs become mostly buildable
- this prevents scattered "single-tile" build zones
5. Slightly increased base open-land chance before road carving, while still keeping city density.

## New Helper Methods
- `createStarterDevelopmentZones(...)`
- `openBuildableHub(...)`
- `maybeConvertToBuildableLot(...)`

## Expected Gameplay Impact
- Easier early placement of roads and stops.
- Better continuity when extending routes.
- Fewer isolated buildable pockets.
- Still not an over-dense full-road grid.

## Validation
- Compiled successfully for non-JavaFX classes:
  - `model`
  - `model/enums`
  - `system`
  - `persistence`
