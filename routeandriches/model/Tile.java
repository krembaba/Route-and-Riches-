package routeandriches.model;

public class Tile {
    private TileType type;
    private RoadShape roadShape;
    private DecorationType decorationType;
    private boolean buildable;
    private int visualVariant;

    public Tile(TileType type) {
        this(type, DecorationType.NONE, type == TileType.EMPTY || type == TileType.PARK, 0);
    }

    public Tile(TileType type, DecorationType decorationType, boolean buildable, int visualVariant) {
        this.type = type;
        this.roadShape = RoadShape.NONE;
        this.decorationType = decorationType == null ? DecorationType.NONE : decorationType;
        this.buildable = buildable;
        this.visualVariant = Math.max(0, visualVariant);
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
        if (type != TileType.ROAD) {
            this.roadShape = RoadShape.NONE;
        }
    }

    public RoadShape getRoadShape() {
        return roadShape;
    }

    public void setRoadShape(RoadShape roadShape) {
        this.roadShape = roadShape == null ? RoadShape.NONE : roadShape;
    }

    public DecorationType getDecorationType() {
        return decorationType;
    }

    public void setDecorationType(DecorationType decorationType) {
        this.decorationType = decorationType == null ? DecorationType.NONE : decorationType;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public int getVisualVariant() {
        return visualVariant;
    }

    public void setVisualVariant(int visualVariant) {
        this.visualVariant = Math.max(0, visualVariant);
    }

    public boolean isRoad() {
        return type == TileType.ROAD;
    }

    public boolean isStop() {
        return type == TileType.STOP;
    }

    public boolean isRoadLike() {
        return type == TileType.ROAD || type == TileType.STOP;
    }
}
