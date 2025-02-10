package com.example.demo1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DatabaseManager {
    private static final File DATABASE_FILE = new File("game_stats.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Object fileLock = new Object();

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        synchronized (fileLock) {
            if (!DATABASE_FILE.exists() || DATABASE_FILE.length() == 0) {
                try {
                    objectMapper.writeValue(DATABASE_FILE, Map.of("X_Wins", 0, "O_Wins", 0, "Draws", 0));
                    System.out.println("[DB] Database file created: " + DATABASE_FILE.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("[ERROR] Failed to create database file.");
                }
            }
        }
    }

    public void updateScore(char winner) {
        synchronized (fileLock) {
            try {
                Map<String, Integer> scores = objectMapper.readValue(DATABASE_FILE, Map.class);
                String column;
                switch (winner) {
                    case 'X': column = "X_Wins"; break;
                    case 'O': column = "O_Wins"; break;
                    case 'D': column = "Draws"; break;
                    default: throw new IllegalArgumentException("Invalid winner: " + winner);
                }
                scores.put(column, scores.getOrDefault(column, 0) + 1);
                objectMapper.writeValue(DATABASE_FILE, scores);
                System.out.println("[DB] Updated " + column);
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to update score: " + e.getMessage());
            }
        }
    }

    public int[] getScores() {
        synchronized (fileLock) {
            try {
                Map<String, Integer> scores = objectMapper.readValue(DATABASE_FILE, Map.class);
                return new int[]{scores.getOrDefault("X_Wins", 0), scores.getOrDefault("O_Wins", 0), scores.getOrDefault("Draws", 0)};
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to fetch scores: " + e.getMessage());
            }
        }
        return new int[]{0, 0, 0}; // Default values if error occurs
    }
}
