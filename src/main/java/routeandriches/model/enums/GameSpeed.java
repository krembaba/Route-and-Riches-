/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package routeandriches.model.enums;

/**
 *
 * @author dell
 */
public enum GameSpeed {
    PAUSED(0.0),
    NORMAL(1.0),
    FAST(2.0);

    private final double multiplier;

    GameSpeed(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
