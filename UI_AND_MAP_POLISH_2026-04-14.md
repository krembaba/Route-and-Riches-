# UI and Map Polish (2026-04-14)

## Scope

Refinement pass for:
- HUD polish (money counter and button states)
- starter map density reduction (fewer roads, more open land)

## Files Updated

- `routeandriches/controller/GameController.java`
- `routeandriches/model/MapGenerator.java`
- `routeandriches/main-view.fxml`

## UI Improvements

### Money counter
- Updated HUD text from:
  - `Money: <value>`
- to:
  - `Treasury: $<formatted_value>`
- Uses grouped number formatting (e.g., `1,000`).

### Toolbar/interactions
- Improved selected/normal action button styles in controller:
  - stronger gradients
  - clearer border states
  - glow/shadow on selected mode

### Map area scrollbars
- Hidden map scroll bars while keeping drag-panning:
  - `hbarPolicy="NEVER"`
  - `vbarPolicy="NEVER"`
  - `pannable="true"`

## Map Generation Changes (Sparser Starter Network)

### Lower road density
- Main avenues reduced from 3x3 structure to 2x2 backbone.
- Secondary roads are now less frequent and shorter.
- Random connector count reduced (from many connectors to a small set).

### More open buildable land
- Base district generation now leaves a portion of tiles as `EMPTY` (buildable) instead of all `BUILDING`.
- This creates larger open areas and avoids the map feeling overcrowded.

### Parks and stops
- Slightly increased park placement count for visual variation.
- Starter stop candidates updated to align with the new road backbone.

## Result

- Cleaner, more polished HUD feel.
- Better map readability and less grid congestion.
- Fewer initial roads, more space for player-driven expansion.
