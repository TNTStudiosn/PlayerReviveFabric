package com.TNTStudios.playerrevivefabric.revive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviveConfig {

    private static final Logger LOGGER = Logger.getLogger(ReviveConfig.class.getName());
    private static final File CONFIG_FILE = new File("config/playerrevivefabric/revive_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ReviveConfig INSTANCE;

    public int defaultReviveTicks = 2400;

    /**
     * Inicializa la configuración.
     * Si la instancia ya existe, no se recarga.
     */
    public static synchronized void init() {
        if (INSTANCE == null) {
            loadConfig();
        }
    }

    /**
     * Obtiene la instancia única de la configuración.
     * Se asegura de que esté inicializada.
     *
     * @return La instancia de ReviveConfig
     */
    public static ReviveConfig get() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }

    /**
     * Carga la configuración desde el archivo.
     * Si el archivo no existe, crea uno por defecto.
     */
    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) {
                LOGGER.info("El archivo de configuración no existe. Creando uno por defecto.");
                saveDefault();
            }
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ReviveConfig.class);
                if (INSTANCE.defaultReviveTicks <= 0) {
                    LOGGER.warning("El valor de defaultReviveTicks no es válido. Se restablecerá a 2400.");
                    INSTANCE.defaultReviveTicks = 2400;
                }
            }
            LOGGER.info("Configuración cargada correctamente.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar revive_config.json: " + e.getMessage(), e);
            INSTANCE = new ReviveConfig();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error inesperado al cargar la configuración: " + ex.getMessage(), ex);
            INSTANCE = new ReviveConfig();
        }
    }

    /**
     * Crea y guarda un archivo de configuración con los valores por defecto.
     *
     * @throws IOException Si ocurre algún error de escritura
     */
    private static void saveDefault() throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ReviveConfig(), writer);
        }
        LOGGER.info("Archivo de configuración por defecto creado en: " + CONFIG_FILE.getAbsolutePath());
    }

    /**
     * Guarda la configuración actual en el archivo.
     * Este método puede utilizarse para actualizar la configuración en tiempo de ejecución.
     */
    public static synchronized void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
            LOGGER.info("Configuración guardada correctamente.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar la configuración: " + e.getMessage(), e);
        }
    }
}
