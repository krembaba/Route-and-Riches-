# UI Overhaul and Sparse Map Update (2026-04-14)

## Scope

Major visual redesign while keeping the same structural layout:
- top control bar
- left minimap panel
- center map area
- right information panel
- bottom action toolbar

Also includes starter map density reduction.

## Files Updated

- `routeandriches/main-view.fxml`
- `routeandriches/controller/GameController.java`
- `routeandriches/model/MapGenerator.java`

## UI Overhaul Highlights

### New visual direction
- Shifted from flat light theme to a deeper command-center style palette:
  - navy/slate gradients
  - card-based surfaces
  - stronger contrast for readability
- Added hierarchy with:
  - title + subtitle in top bar
  - dashboard card groups on right side
  - styled money badge and primary Start action

### Money and controls
- Money label remains dynamic through `fx:id="moneyLabel"`.
- Bottom toolbar buttons and top control buttons have polished rounded styles.
- Interaction mode button styling is also enhanced in controller for selected/normal states.

### Navigation/map presentation
- Map remains pannable.
- Scroll bars are hidden:
  - `hbarPolicy="NEVER"`
  - `vbarPolicy="NEVER"`
- Minimap panel refined to match the new visual language.

## Sparse Starter Map Update

Starter city road network now generates with fewer roads:
- main avenue backbone reduced
- secondary roads are less frequent and shorter
- random connector count lowered
- more buildable `EMPTY` plots added in base district generation
- parks slightly increased for variety

Result: fewer stop-adjacent tiles at start and more meaningful expansion decisions.

## Compatibility Notes

- Existing `fx:id` bindings used by `GameController` are preserved.
- Existing action handlers (`onAction`) are preserved.
- No architecture or package layout changes were introduced.
