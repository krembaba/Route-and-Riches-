/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches.persistence;

/**
 *
 * @author dell
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveService {

    public void save(GameSnapshot snapshot, String filePath) throws IOException {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot cannot be null.");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        String json = toJson(snapshot);
        Files.writeString(Path.of(filePath), json, StandardCharsets.UTF_8);
    }

    public GameSnapshot load(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        String json = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        return fromJson(json);
    }

    private String toJson(GameSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"money\": ").append(snapshot.getMoney()).append(",\n");
        sb.append("  \"elapsedSeconds\": ").append(snapshot.getElapsedSeconds()).append(",\n");
        sb.append("  \"mapData\": ").append(toJsonArray(snapshot.getMapData())).append(",\n");
        sb.append("  \"routeData\": ").append(toJsonArray(snapshot.getRouteData())).append(",\n");
        sb.append("  \"vehicleData\": ").append(toJsonArray(snapshot.getVehicleData())).append(",\n");
        sb.append("  \"trafficLightData\": ").append(toJsonArray(snapshot.getTrafficLightData())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toJsonArray(java.util.List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private GameSnapshot fromJson(String json) {
        GameSnapshot snapshot = new GameSnapshot();

        snapshot.setMoney(parseInt(json, "money"));
        snapshot.setElapsedSeconds(parseDouble(json, "elapsedSeconds"));
        snapshot.setMapData(parseStringArray(json, "mapData"));
        snapshot.setRouteData(parseStringArray(json, "routeData"));
        snapshot.setVehicleData(parseStringArray(json, "vehicleData"));
        snapshot.setTrafficLightData(parseStringArray(json, "trafficLightData"));

        return snapshot;
    }

    private int parseInt(String json, String key) {
        String value = extractRawValue(json, key);
        return Integer.parseInt(value.trim());
    }

    private double parseDouble(String json, String key) {
        String value = extractRawValue(json, key);
        return Double.parseDouble(value.trim());
    }

    private java.util.List<String> parseStringArray(String json, String key) {
        java.util.List<String> result = new java.util.ArrayList<>();
        String raw = extractArray(json, key).trim();

        if (raw.isEmpty()) {
            return result;
        }

        String[] parts = raw.split("\",\\s*\"");
        for (String part : parts) {
            String cleaned = part.replaceAll("^\"", "").replaceAll("\"$", "");
            cleaned = cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
            result.add(cleaned);
        }

        return result;
    }

    private String extractRawValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        start += marker.length();

        int end = json.indexOf(",", start);
        if (end < 0) {
            end = json.indexOf("}", start);
        }
        return json.substring(start, end);
    }

    private String extractArray(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        start = json.indexOf("[", start);
        int end = json.indexOf("]", start);
        return json.substring(start + 1, end);
    }
}