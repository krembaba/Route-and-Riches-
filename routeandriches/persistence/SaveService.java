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

/**
 * Serializes and deserializes {@link GameSnapshot} instances to JSON files.
 */
public class SaveService {

    /**
     * Writes a snapshot to disk in JSON format.
     *
     * @param snapshot snapshot to persist
     * @param filePath target file path
     * @throws IOException if file writing fails
     */
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

    /**
     * Loads a snapshot from disk.
     *
     * @param filePath source file path
     * @return loaded snapshot
     * @throws IOException if reading fails
     */
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
        sb.append("  \"gameState\": \"").append(escapeJson(snapshot.getGameState())).append("\",\n");
        sb.append("  \"gameSpeed\": \"").append(escapeJson(snapshot.getGameSpeed())).append("\",\n");
        sb.append("  \"mapData\": ").append(toJsonArray(snapshot.getMapData())).append(",\n");
        sb.append("  \"routeData\": ").append(toJsonArray(snapshot.getRouteData())).append(",\n");
        sb.append("  \"vehicleData\": ").append(toJsonArray(snapshot.getVehicleData())).append(",\n");
        sb.append("  \"trafficLightData\": ").append(toJsonArray(snapshot.getTrafficLightData())).append(",\n");
        sb.append("  \"passengerData\": ").append(toJsonArray(snapshot.getPassengerData())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toJsonArray(java.util.List<String> list) {
        if (list == null) {
            return "[]";
        }
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
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private GameSnapshot fromJson(String json) {
        GameSnapshot snapshot = new GameSnapshot();

        snapshot.setMoney(parseIntOrDefault(json, "money", 0));
        snapshot.setElapsedSeconds(parseDoubleOrDefault(json, "elapsedSeconds", 0.0));
        snapshot.setGameState(parseStringOrDefault(json, "gameState", "PAUSED"));
        snapshot.setGameSpeed(parseStringOrDefault(json, "gameSpeed", "PAUSED"));
        snapshot.setMapData(parseStringArrayOrDefault(json, "mapData"));
        snapshot.setRouteData(parseStringArrayOrDefault(json, "routeData"));
        snapshot.setVehicleData(parseStringArrayOrDefault(json, "vehicleData"));
        snapshot.setTrafficLightData(parseStringArrayOrDefault(json, "trafficLightData"));
        snapshot.setPassengerData(parseStringArrayOrDefault(json, "passengerData"));

        return snapshot;
    }

    private int parseIntOrDefault(String json, String key, int fallback) {
        String value = extractRawValueOrNull(json, key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private double parseDoubleOrDefault(String json, String key, double fallback) {
        String value = extractRawValueOrNull(json, key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String parseStringOrDefault(String json, String key, String fallback) {
        String raw = extractRawValueOrNull(json, key);
        if (raw == null) {
            return fallback;
        }

        String cleaned = raw.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        cleaned = cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private java.util.List<String> parseStringArrayOrDefault(String json, String key) {
        java.util.List<String> result = new java.util.ArrayList<>();
        String rawArray = extractArrayOrNull(json, key);
        if (rawArray == null) {
            return result;
        }

        String raw = rawArray.trim();

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

    private String extractRawValueOrNull(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();

        int end = json.indexOf(",", start);
        if (end < 0) {
            end = json.indexOf("}", start);
        }
        if (end < 0) {
            return null;
        }
        return json.substring(start, end);
    }

    private String extractArrayOrNull(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start = json.indexOf("[", start);
        if (start < 0) {
            return null;
        }
        int end = json.indexOf("]", start);
        if (end < 0) {
            return null;
        }
        return json.substring(start + 1, end);
    }
}
