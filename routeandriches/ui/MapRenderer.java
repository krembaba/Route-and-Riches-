package routeandriches.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import routeandriches.model.DecorationType;
import routeandriches.model.GameMap;
import routeandriches.model.RoadShape;
import routeandriches.model.Tile;
import routeandriches.model.TileType;

public class MapRenderer {

    private final double tileSize;

    public MapRenderer(double tileSize) {
        this.tileSize = tileSize;
    }

    public double getTileSize() {
        return tileSize;
    }

    public void drawMap(GraphicsContext gc, GameMap map) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.web("#d9e6cf"));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                drawTile(gc, map, row, col);
            }
        }

        gc.setStroke(Color.rgb(255, 255, 255, 0.08));
        gc.setLineWidth(1);
        for (int row = 0; row <= map.getRows(); row++) {
            gc.strokeLine(0, row * tileSize, map.getCols() * tileSize, row * tileSize);
        }
        for (int col = 0; col <= map.getCols(); col++) {
            gc.strokeLine(col * tileSize, 0, col * tileSize, map.getRows() * tileSize);
        }
    }

    public void drawPlacementPreview(GraphicsContext gc, int row, int col, boolean valid, boolean stopMode) {
        double x = col * tileSize;
        double y = row * tileSize;
        gc.setFill(valid ? Color.rgb(80, 220, 140, 0.35) : Color.rgb(220, 80, 80, 0.35));
        gc.fillRoundRect(x + 2, y + 2, tileSize - 4, tileSize - 4, 8, 8);
        gc.setStroke(valid ? Color.rgb(185, 255, 210, 0.9) : Color.rgb(255, 190, 190, 0.9));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 2, y + 2, tileSize - 4, tileSize - 4, 8, 8);

        if (stopMode) {
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            gc.fillOval(x + tileSize * 0.33, y + tileSize * 0.33, tileSize * 0.34, tileSize * 0.34);
        }
    }

    private void drawTile(GraphicsContext gc, GameMap map, int row, int col) {
        Tile tile = map.getTile(row, col);
        double x = col * tileSize;
        double y = row * tileSize;

        drawGround(gc, tile, x, y);

        if (tile.getType() == TileType.BUILDING) {
            drawBuilding(gc, tile, x, y);
        } else if (tile.getType() == TileType.PARK || tile.getType() == TileType.EMPTY) {
            drawDecoration(gc, tile.getDecorationType(), x, y, tile.getVisualVariant());
        }

        if (tile.isRoadLike()) {
            drawRoadBase(gc, x, y);
            drawRoadShape(gc, tile.getRoadShape(), x, y);
            if (tile.getType() == TileType.STOP) {
                drawStopMarker(gc, x, y);
            }
        }
    }

    private void drawGround(GraphicsContext gc, Tile tile, double x, double y) {
        Color base;
        switch (tile.getType()) {
            case PARK:
                base = tile.getVisualVariant() % 2 == 0 ? Color.web("#6aa85b") : Color.web("#78b868");
                break;
            case BUILDING:
                base = Color.web(tile.getVisualVariant() % 2 == 0 ? "#c5b9a8" : "#b8ae9f");
                break;
            default:
                base = tile.getVisualVariant() % 2 == 0 ? Color.web("#8eb77a") : Color.web("#98bf85");
                break;
        }
        gc.setFill(base);
        gc.fillRect(x, y, tileSize, tileSize);

        // Subtle gradient bands to avoid flat tiles.
        gc.setFill(Color.rgb(255, 255, 255, 0.08));
        gc.fillRect(x, y, tileSize, tileSize * 0.16);
        gc.setFill(Color.rgb(0, 0, 0, 0.06));
        gc.fillRect(x, y + tileSize * 0.78, tileSize, tileSize * 0.22);

        // Tiny speckles to give terrain texture.
        gc.setFill(Color.rgb(255, 255, 255, 0.10));
        double step = Math.max(4, tileSize * 0.22);
        for (double sy = y + 3; sy < y + tileSize - 2; sy += step) {
            for (double sx = x + 3; sx < x + tileSize - 2; sx += step) {
                gc.fillOval(sx, sy, 1.2, 1.2);
            }
        }

        gc.setStroke(Color.rgb(255, 255, 255, 0.06));
        gc.strokeRect(x, y, tileSize, tileSize);
    }

    private void drawBuilding(GraphicsContext gc, Tile tile, double x, double y) {
        gc.setFill(Color.rgb(0, 0, 0, 0.12));
        gc.fillRoundRect(x + 5, y + 6, tileSize - 8, tileSize - 8, 6, 6);

        Color body = switch (tile.getVisualVariant() % 4) {
            case 0 -> Color.web("#857a70");
            case 1 -> Color.web("#91867b");
            case 2 -> Color.web("#756d66");
            default -> Color.web("#9e9388");
        };
        gc.setFill(body);
        gc.fillRoundRect(x + 4, y + 4, tileSize - 8, tileSize - 8, 6, 6);

        gc.setFill(Color.rgb(255, 255, 255, 0.12));
        gc.fillRoundRect(x + 6, y + 5, tileSize - 12, tileSize * 0.18, 4, 4);

        // Roof stripe and small rooftop unit for detail.
        gc.setFill(Color.rgb(255, 255, 255, 0.08));
        gc.fillRect(x + 6, y + tileSize * 0.52, tileSize - 12, 2.2);
        gc.setFill(Color.rgb(60, 60, 68, 0.45));
        gc.fillRoundRect(x + tileSize * 0.57, y + tileSize * 0.22, tileSize * 0.17, tileSize * 0.16, 2, 2);

        gc.setFill(Color.rgb(235, 215, 120, 0.55));
        double w = tileSize * 0.16;
        double h = tileSize * 0.16;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                gc.fillRect(x + 7 + c * (w + 4), y + 9 + r * (h + 4), w, h);
            }
        }

        drawDecoration(gc, tile.getDecorationType(), x, y, tile.getVisualVariant());
    }

    private void drawRoadBase(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#3d4349"));
        gc.fillRect(x, y, tileSize, tileSize);
        gc.setFill(Color.rgb(255, 255, 255, 0.04));
        gc.fillRect(x, y, tileSize, tileSize * 0.18);
        gc.setFill(Color.rgb(0, 0, 0, 0.08));
        gc.fillRect(x, y + tileSize * 0.82, tileSize, tileSize * 0.18);

        // Asphalt speckles.
        gc.setFill(Color.rgb(255, 255, 255, 0.05));
        for (int i = 0; i < 5; i++) {
            double px = x + 3 + i * (tileSize * 0.17);
            double py = y + 3 + (i % 3) * (tileSize * 0.22);
            gc.fillOval(px, py, 1.2, 1.2);
        }
    }

    private void drawRoadShape(GraphicsContext gc, RoadShape shape, double x, double y) {
        double margin = tileSize * 0.18;
        double center = tileSize * 0.36;

        gc.setFill(Color.web("#4f565d"));
        gc.fillRect(x + margin, y + margin, tileSize - 2 * margin, tileSize - 2 * margin);

        gc.setFill(Color.web("#5c636a"));
        switch (shape) {
            case STRAIGHT_HORIZONTAL -> gc.fillRect(x, y + margin, tileSize, tileSize - 2 * margin);
            case STRAIGHT_VERTICAL -> gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize);
            case CORNER_NE -> {
                gc.fillRect(x + margin, y, tileSize - margin, tileSize - center);
                gc.fillRect(x + center, y + margin, tileSize - center, tileSize - margin);
            }
            case CORNER_NW -> {
                gc.fillRect(x, y + margin, tileSize - margin, tileSize - center);
                gc.fillRect(x + margin, y, tileSize - center, tileSize - margin);
            }
            case CORNER_SE -> {
                gc.fillRect(x + center, y + margin, tileSize - center, tileSize - margin);
                gc.fillRect(x + margin, y + center, tileSize - margin, tileSize - center);
            }
            case CORNER_SW -> {
                gc.fillRect(x, y + center, tileSize - margin, tileSize - center);
                gc.fillRect(x + margin, y + margin, tileSize - center, tileSize - margin);
            }
            case T_UP -> {
                gc.fillRect(x, y + margin, tileSize, tileSize - 2 * margin);
                gc.fillRect(x + margin, y + margin, tileSize - 2 * margin, tileSize - margin);
            }
            case T_DOWN -> {
                gc.fillRect(x, y + margin, tileSize, tileSize - 2 * margin);
                gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize - margin);
            }
            case T_LEFT -> {
                gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize);
                gc.fillRect(x + margin, y + margin, tileSize - margin, tileSize - 2 * margin);
            }
            case T_RIGHT -> {
                gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize);
                gc.fillRect(x, y + margin, tileSize - margin, tileSize - 2 * margin);
            }
            case CROSS -> {
                gc.fillRect(x, y + margin, tileSize, tileSize - 2 * margin);
                gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize);
            }
            case DEAD_END_N -> {
                gc.fillRect(x + margin, y, tileSize - 2 * margin, tileSize - margin * 0.7);
                gc.fillOval(x + center, y + center, tileSize * 0.28, tileSize * 0.28);
            }
            case DEAD_END_S -> {
                gc.fillRect(x + margin, y + margin * 0.7, tileSize - 2 * margin, tileSize - margin * 0.7);
                gc.fillOval(x + center, y + center, tileSize * 0.28, tileSize * 0.28);
            }
            case DEAD_END_E -> {
                gc.fillRect(x + margin * 0.7, y + margin, tileSize - margin * 0.7, tileSize - 2 * margin);
                gc.fillOval(x + center, y + center, tileSize * 0.28, tileSize * 0.28);
            }
            case DEAD_END_W -> {
                gc.fillRect(x, y + margin, tileSize - margin * 0.7, tileSize - 2 * margin);
                gc.fillOval(x + center, y + center, tileSize * 0.28, tileSize * 0.28);
            }
            default -> gc.fillRect(x + margin, y + margin, tileSize - 2 * margin, tileSize - 2 * margin);
        }

        drawLaneMarkings(gc, shape, x, y, margin);
    }

    private void drawLaneMarkings(GraphicsContext gc, RoadShape shape, double x, double y, double margin) {
        gc.setStroke(Color.rgb(255, 227, 138, 0.8));
        gc.setLineWidth(Math.max(1.0, tileSize * 0.06));
        double midX = x + tileSize / 2.0;
        double midY = y + tileSize / 2.0;
        double inner = margin + 2;
        double outer = tileSize - margin - 2;

        switch (shape) {
            case STRAIGHT_HORIZONTAL, T_UP, T_DOWN, CROSS -> gc.strokeLine(x + 4, midY, x + tileSize - 4, midY);
            default -> {}
        }
        switch (shape) {
            case STRAIGHT_VERTICAL, T_LEFT, T_RIGHT, CROSS -> gc.strokeLine(midX, y + 4, midX, y + tileSize - 4);
            default -> {}
        }
        switch (shape) {
            case CORNER_NE -> gc.strokeArc(x + inner, y + inner, outer - inner, outer - inner, 0, 90, ArcType.OPEN);
            case CORNER_NW -> gc.strokeArc(x + margin, y + inner, outer - inner, outer - inner, 90, 90, ArcType.OPEN);
            case CORNER_SE -> gc.strokeArc(x + inner, y + margin, outer - inner, outer - inner, 270, 90, ArcType.OPEN);
            case CORNER_SW -> gc.strokeArc(x + margin, y + margin, outer - inner, outer - inner, 180, 90, ArcType.OPEN);
            default -> {}
        }

        gc.setLineDashes(3, 3);
        gc.setStroke(Color.rgb(235, 240, 245, 0.25));
        switch (shape) {
            case STRAIGHT_HORIZONTAL -> gc.strokeLine(x + 3, y + margin * 0.7, x + tileSize - 3, y + margin * 0.7);
            case STRAIGHT_VERTICAL -> gc.strokeLine(x + margin * 0.7, y + 3, x + margin * 0.7, y + tileSize - 3);
            default -> {
            }
        }
        gc.setLineDashes(0);
    }

    private void drawStopMarker(GraphicsContext gc, double x, double y) {
        double size = tileSize * 0.38;
        double px = x + (tileSize - size) / 2.0;
        double py = y + (tileSize - size) / 2.0;
        gc.setFill(Color.web("#1d8cf0"));
        gc.fillRoundRect(px, py, size, size, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + tileSize * 0.37, y + tileSize * 0.37, tileSize * 0.26, tileSize * 0.26);
        gc.setStroke(Color.rgb(0, 0, 0, 0.18));
        gc.setLineWidth(1);
        gc.strokeRoundRect(px, py, size, size, 8, 8);
    }

    private void drawDecoration(GraphicsContext gc, DecorationType decorationType, double x, double y, int variant) {
        switch (decorationType) {
            case TREE -> {
                gc.setFill(Color.rgb(0, 0, 0, 0.12));
                gc.fillOval(x + tileSize * 0.22, y + tileSize * 0.48, tileSize * 0.42, tileSize * 0.2);
                gc.setFill(Color.web("#694d31"));
                gc.fillRect(x + tileSize * 0.42, y + tileSize * 0.52, tileSize * 0.09, tileSize * 0.22);
                gc.setFill(variant % 2 == 0 ? Color.web("#2f7d46") : Color.web("#3a8d4f"));
                gc.fillOval(x + tileSize * 0.2, y + tileSize * 0.22, tileSize * 0.44, tileSize * 0.36);
            }
            case BUSH -> {
                gc.setFill(variant % 2 == 0 ? Color.web("#4f9a57") : Color.web("#5aa562"));
                gc.fillOval(x + tileSize * 0.2, y + tileSize * 0.5, tileSize * 0.28, tileSize * 0.16);
                gc.fillOval(x + tileSize * 0.36, y + tileSize * 0.46, tileSize * 0.26, tileSize * 0.18);
            }
            case FLOWER_BED -> {
                gc.setFill(Color.web("#6aa85b"));
                gc.fillRoundRect(x + tileSize * 0.18, y + tileSize * 0.55, tileSize * 0.48, tileSize * 0.15, 5, 5);
                gc.setFill(Color.web("#f36ca6"));
                gc.fillOval(x + tileSize * 0.24, y + tileSize * 0.5, tileSize * 0.08, tileSize * 0.08);
                gc.setFill(Color.web("#ffd166"));
                gc.fillOval(x + tileSize * 0.38, y + tileSize * 0.48, tileSize * 0.08, tileSize * 0.08);
                gc.setFill(Color.web("#7b61ff"));
                gc.fillOval(x + tileSize * 0.5, y + tileSize * 0.52, tileSize * 0.08, tileSize * 0.08);
            }
            case LAMP -> {
                gc.setStroke(Color.web("#585858"));
                gc.setLineWidth(2);
                gc.strokeLine(x + tileSize * 0.7, y + tileSize * 0.22, x + tileSize * 0.7, y + tileSize * 0.76);
                gc.setFill(Color.web("#ffe082"));
                gc.fillOval(x + tileSize * 0.62, y + tileSize * 0.18, tileSize * 0.16, tileSize * 0.16);
            }
            case PLAZA -> {
                gc.setFill(Color.rgb(255, 255, 255, 0.16));
                gc.fillRoundRect(x + tileSize * 0.2, y + tileSize * 0.2, tileSize * 0.5, tileSize * 0.5, 6, 6);
            }
            default -> {
            }
        }
    }
}
