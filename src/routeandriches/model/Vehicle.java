/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.model;

/**
 *
 * @author dell
 */
public class Vehicle {

    private double x;
    private double y;
    private double speed = 50;

    public Vehicle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(double delta) {
        x += speed * delta;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
