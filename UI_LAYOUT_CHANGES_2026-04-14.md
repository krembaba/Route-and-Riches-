# UI Layout Changes (2026-04-14)

File updated:
- `routeandriches/main-view.fxml`

## Goal

Reframe the UI to match the wireframe structure while preserving the existing color theme, and fix readability issues in the right information panel.

## What Was Changed

### 1) Placeholder labels removed

Removed non-functional wireframe marker text so they no longer appear in the running app:
- `Top Control Bar`
- `Minimap`
- `Main Map View`
- `(Tile-Based Grid)`
- `Toolbar`

### 2) Right panel readability fixed

The right information panel was too narrow and text was getting clipped.

Fixes applied:
- Right panel width is now fixed to a readable size:
  - `minWidth="360"`, `prefWidth="360"`, `maxWidth="360"`
- Key content labels are set to wrap and larger font sizes:
  - state/speed/time
  - vehicles/routes
  - selected tool text
  - hint text
  - stats lines
- Information panel title now wraps instead of clipping.

### 3) Center map area behavior corrected

Map navigation/panning issues were reduced by adjusting center sizing:
- Center map container set to `minWidth="0"` so side panels are not collapsed by map content.
- `ScrollPane` viewport hints added:
  - `prefViewportWidth="920"`
  - `prefViewportHeight="560"`
- `mapScrollPane` remains `pannable="true"` for map movement.

### 4) Layout structure preserved

The 4-zone wireframe structure remains:
- top control bar
- left minimap block
- center map area
- right information panel
- bottom action toolbar

All controller bindings were preserved (`fx:id` and `onAction` handlers used by `GameController`).

## Notes

- This update is layout/readability only; game logic was not changed.
- Button behavior currently follows existing controller methods (e.g., `Manage Routes` still points to route creation mode handler).
