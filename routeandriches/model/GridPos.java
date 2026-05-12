/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.model;

/**
 *
 * @author dell
 */
import java.util.Objects;

public class GridPos {
    private final int row;
    private final int col;

    /**
     * Creates a new GridPos instance.
     */
    public GridPos(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Grid position cannot be negative.");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * Executes getRow.
     */
    public int getRow() {
        return row;
    }

    /**
     * Executes getCol.
     */
    public int getCol() {
        return col;
    }

    @Override
    /**
     * Executes equals.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridPos)) return false;
        GridPos gridPos = (GridPos) o;
        return row == gridPos.row && col == gridPos.col;
    }

    @Override
    /**
     * Executes hashCode.
     */
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    /**
     * Executes toString.
     */
    public String toString() {
        return "GridPos{" + "row=" + row + ", col=" + col + '}';
    }
}
