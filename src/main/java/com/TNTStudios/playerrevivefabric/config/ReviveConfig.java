package com.TNTStudios.playerrevivefabric.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReviveConfig {

    private static final Path CONFIG_PATH = Path.of("config", "playerrevivefabric.json");
    private static final Gson GSON = new Gson();

    public static long reviveTimeMs = 30000; // 30 segundos por defecto

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("reviveTimeMs")) {
                    reviveTimeMs = json.get("reviveTimeMs").getAsLong();
                }
            } catch (Exception e) {
                System.err.println("[PlayerReviveFabric] Error leyendo la config, usando valores por defecto.");
            }
        } else {
            saveDefault();
        }
    }

    private static void saveDefault() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("reviveTimeMs", reviveTimeMs);
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            System.err.println("[PlayerReviveFabric] No se pudo guardar config por defecto.");
        }
    }
}
