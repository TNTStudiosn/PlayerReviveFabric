package com.TNTStudios.playerrevivefabric.revive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReviveConfig {

    private static final File CONFIG_FILE = new File("config/playerrevivefabric/revive_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ReviveConfig INSTANCE;

    public int defaultReviveTicks = 2400; // 120s por defecto

    public static void init() {
        if (INSTANCE == null) {
            loadConfig();
        }
    }

    public static ReviveConfig get() {
        return INSTANCE;
    }

    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
            }

            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ReviveConfig.class);
            }

        } catch (IOException e) {
            System.err.println("Error loading revive_config.json: " + e.getMessage());
            INSTANCE = new ReviveConfig(); // fallback
        }
    }

    private static void saveDefault() throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ReviveConfig(), writer);
        }
    }
}
