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

    /**
     * Creates a new GameSnapshot instance.
     */
    public GameSnapshot() {
        this.mapData = new ArrayList<>();
        this.routeData = new ArrayList<>();
        this.vehicleData = new ArrayList<>();
        this.trafficLightData = new ArrayList<>();
        this.passengerData = new ArrayList<>();
        this.gameState = "PAUSED";
        this.gameSpeed = "PAUSED";
    }

    /**
     * Executes getMoney.
     */
    public int getMoney() {
        return money;
    }

    /**
     * Executes setMoney.
     */
    public void setMoney(int money) {
        this.money = money;
    }

    /**
     * Executes getElapsedSeconds.
     */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Executes setElapsedSeconds.
     */
    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    /**
     * Executes getGameState.
     */
    public String getGameState() {
        return gameState;
    }

    /**
     * Executes setGameState.
     */
    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    /**
     * Executes getGameSpeed.
     */
    public String getGameSpeed() {
        return gameSpeed;
    }

    /**
     * Executes setGameSpeed.
     */
    public void setGameSpeed(String gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    /**
     * Executes getMapData.
     */
    public List<String> getMapData() {
        return mapData;
    }

    /**
     * Executes setMapData.
     */
    public void setMapData(List<String> mapData) {
        this.mapData = mapData;
    }

    /**
     * Executes getRouteData.
     */
    public List<String> getRouteData() {
        return routeData;
    }

    /**
     * Executes setRouteData.
     */
    public void setRouteData(List<String> routeData) {
        this.routeData = routeData;
    }

    /**
     * Executes getVehicleData.
     */
    public List<String> getVehicleData() {
        return vehicleData;
    }

    /**
     * Executes setVehicleData.
     */
    public void setVehicleData(List<String> vehicleData) {
        this.vehicleData = vehicleData;
    }

    /**
     * Executes getTrafficLightData.
     */
    public List<String> getTrafficLightData() {
        return trafficLightData;
    }

    /**
     * Executes setTrafficLightData.
     */
    public void setTrafficLightData(List<String> trafficLightData) {
        this.trafficLightData = trafficLightData;
    }

    /**
     * Executes getPassengerData.
     */
    public List<String> getPassengerData() {
        return passengerData;
    }

    /**
     * Executes setPassengerData.
     */
    public void setPassengerData(List<String> passengerData) {
        this.passengerData = passengerData;
    }
}
