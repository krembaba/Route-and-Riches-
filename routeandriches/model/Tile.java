package routeandriches.model;

/**

 * Represents the Tile component.

 */

public class Tile {
    private TileType type;
    private RoadShape roadShape;
    private DecorationType decorationType;
    private boolean buildable;
    private int visualVariant;

    /**
     * Creates a new Tile instance.
     */
    public Tile(TileType type) {
        this(type, DecorationType.NONE, type == TileType.EMPTY || type == TileType.PARK, 0);
    }

    /**
     * Creates a new Tile instance.
     */
    public Tile(TileType type, DecorationType decorationType, boolean buildable, int visualVariant) {
        this.type = type;
        this.roadShape = RoadShape.NONE;
        this.decorationType = decorationType == null ? DecorationType.NONE : decorationType;
        this.buildable = buildable;
        this.visualVariant = Math.max(0, visualVariant);
    }

    /**
     * Executes getType.
     */
    public TileType getType() {
        return type;
    }

    /**
     * Executes setType.
     */
    public void setType(TileType type) {
        this.type = type;
        if (type != TileType.ROAD) {
            this.roadShape = RoadShape.NONE;
        }
    }

    /**
     * Executes getRoadShape.
     */
    public RoadShape getRoadShape() {
        return roadShape;
    }

    /**
     * Executes setRoadShape.
     */
    public void setRoadShape(RoadShape roadShape) {
        this.roadShape = roadShape == null ? RoadShape.NONE : roadShape;
    }

    /**
     * Executes getDecorationType.
     */
    public DecorationType getDecorationType() {
        return decorationType;
    }

    /**
     * Executes setDecorationType.
     */
    public void setDecorationType(DecorationType decorationType) {
        this.decorationType = decorationType == null ? DecorationType.NONE : decorationType;
    }

    /**
     * Executes isBuildable.
     */
    public boolean isBuildable() {
        return buildable;
    }

    /**
     * Executes setBuildable.
     */
    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    /**
     * Executes getVisualVariant.
     */
    public int getVisualVariant() {
        return visualVariant;
    }

    /**
     * Executes setVisualVariant.
     */
    public void setVisualVariant(int visualVariant) {
        this.visualVariant = Math.max(0, visualVariant);
    }

    /**
     * Executes isRoad.
     */
    public boolean isRoad() {
        return type == TileType.ROAD;
    }

    /**
     * Executes isStop.
     */
    public boolean isStop() {
        return type == TileType.STOP;
    }

    /**
     * Executes isRoadLike.
     */
    public boolean isRoadLike() {
        return type == TileType.ROAD || type == TileType.STOP;
    }
}

