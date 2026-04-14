# UI Overhaul V3 (2026-04-14)

## Goal
Perform a stronger UI redesign while keeping the same structure:
- Top control bar
- Left minimap panel
- Center map area
- Right information panel
- Bottom toolbar

## Files Updated
- `routeandriches/main-view.fxml`
- `routeandriches/ui-theme.css` (new)
- `routeandriches/controller/GameController.java`

## What Changed
1. Rebuilt `main-view.fxml` with a full style-class based layout.
2. Added a dedicated stylesheet (`ui-theme.css`) for:
- new color system and gradients
- polished cards/panels
- improved toolbar and money badge
- consistent button design with hover states
- hidden scrollbars for map container
3. Switched interaction mode highlighting in controller from inline styles to CSS class toggling:
- active class: `mode-button-active`
4. Updated money label text format to:
- `City Treasury: $X`

## Why This Is Better
- Larger visual change with cleaner hierarchy and readability.
- Easier future edits because styling is centralized in CSS.
- Keeps all gameplay wiring and layout structure unchanged.

## Notes
- Compile check in this environment still reports JavaFX jar access issues (tooling/environment-specific), so runtime verification should be done on your local machine where JavaFX is already installed and working.
