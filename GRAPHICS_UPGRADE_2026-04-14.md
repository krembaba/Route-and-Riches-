# Graphics Upgrade (2026-04-14)

## Scope

Visual improvement pass for:
- map tile detail
- vehicle rendering (bus/tram)
- UI polish

No game rules or persistence behavior were changed in this pass.

## Files Updated

- `routeandriches/controller/GameController.java`
- `routeandriches/ui/MapRenderer.java`
- `routeandriches/main-view.fxml`

## Changes

### 1) Top-down vehicle sprites

Replaced circle vehicle markers with directional top-down sprites:

- Bus sprite:
  - yellow body
  - windshield/windows
  - wheels + outline/shadow
- Tram sprite:
  - blue elongated body
  - roof stripe + window separators
  - outline/shadow

The sprite rotates toward route heading using next path position.

### 2) More detailed map tiles

Enhanced map rendering detail in `MapRenderer`:

- Ground:
  - subtle gradient bands
  - micro speckle texture
- Buildings:
  - roof stripe + rooftop detail block
  - stronger depth/shadow feeling
- Roads:
  - top and bottom lighting/shadow layers
  - asphalt speckle texture
  - extra dashed side lane accents
- Stops:
  - marker outline refinement

### 3) UI visual polish

Kept the existing layout but improved visual quality:

- top bar buttons styled with rounded gradients
- primary `Start` button highlighted
- minimap card gets subtle drop shadow
- right panel cards get gradient backgrounds
- toolbar/action buttons have consistent polished style
- save/load buttons styled to match theme

## Notes

- This pass keeps all existing `fx:id` and action handler bindings required by `GameController`.
- If needed, a second pass can add true image assets (`.png`) for bus/tram sprites, with current vector sprites as fallback.
