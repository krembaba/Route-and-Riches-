/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.persistence;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author dell
 */
public class GameSnapshot {
    private int money;
    private double elapsedSeconds;
    private String gameState;
    private String gameSpeed;

    private List<String> mapData;
    private List<String> routeData;
    private List<String> vehicleData;
    private List<String> trafficLightData;
    private List<String> passengerData;

    public GameSnapshot() {
        this.mapData = new ArrayList<>();
        this.routeData = new ArrayList<>();
        this.vehicleData = new ArrayList<>();
        this.trafficLightData = new ArrayList<>();
        this.passengerData = new ArrayList<>();
        this.gameState = "PAUSED";
        this.gameSpeed = "PAUSED";
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(String gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public List<String> getMapData() {
        return mapData;
    }

    public void setMapData(List<String> mapData) {
        this.mapData = mapData;
    }

    public List<String> getRouteData() {
        return routeData;
    }

    public void setRouteData(List<String> routeData) {
        this.routeData = routeData;
    }

    public List<String> getVehicleData() {
        return vehicleData;
    }

    public void setVehicleData(List<String> vehicleData) {
        this.vehicleData = vehicleData;
    }

    public List<String> getTrafficLightData() {
        return trafficLightData;
    }

    public void setTrafficLightData(List<String> trafficLightData) {
        this.trafficLightData = trafficLightData;
    }

    public List<String> getPassengerData() {
        return passengerData;
    }

    public void setPassengerData(List<String> passengerData) {
        this.passengerData = passengerData;
    }
}
