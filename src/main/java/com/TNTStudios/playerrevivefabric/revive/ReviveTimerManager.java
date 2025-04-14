package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviveTimerManager {
    private static final Logger LOGGER = Logger.getLogger(ReviveTimerManager.class.getName());
    private static final Map<UUID, Integer> timers = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastSentSeconds = new ConcurrentHashMap<>();

    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!PlayerReviveData.isDowned(uuid)) return;

        timers.computeIfPresent(uuid, (id, ticksLeft) -> {
            if (ticksLeft <= 1) {
                killPlayer(player);
                lastSentSeconds.remove(uuid);
                return null;
            }

            int newTicksLeft = ticksLeft - 1;
            int newSeconds = newTicksLeft / 20;
            int lastSeconds = lastSentSeconds.getOrDefault(uuid, -1);

            if (newSeconds != lastSeconds) {
                RevivePackets.sendTimerUpdate(player, newTicksLeft);
                lastSentSeconds.put(uuid, newSeconds);
            }
            return newTicksLeft;
        });
    }

    public static void startTimer(UUID uuid) {
        timers.put(uuid, ReviveConfig.get().defaultReviveTicks);
        lastSentSeconds.put(uuid, ReviveConfig.get().defaultReviveTicks / 20);
    }

    public static void stopTimer(UUID uuid) {
        timers.remove(uuid);
        lastSentSeconds.remove(uuid);
    }

    public static void forceDeath(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        stopTimer(uuid);
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveData.markDeathAccepted(uuid);
        PlayerReviveNetwork.sendDownedState(player, false);
        applyLethalDamage(player);
    }

    private static void killPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        stopTimer(uuid);
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveData.markTimerExpired(uuid);
        PlayerReviveNetwork.sendDownedState(player, false);
        applyLethalDamage(player);
        ReviveInteractionManager.cancelIfBeingRevived(uuid);
    }

    private static void applyLethalDamage(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        DamageSource originalSource = PlayerReviveData.getLastDamageSource(player);

        if (player.getServer() == null) {
            LOGGER.warning("No se puede aplicar daño letal: servidor no disponible para el jugador " + player.getName().getString());
            return;
        }

        player.getServer().execute(() -> {
            try {
                // Paso 1: Intentamos matar al jugador con el DamageSource original
                float currentHealth = player.getHealth();
                float lethalDamage = currentHealth + 10.0F; // Buffer para superar salud y efectos
                boolean damageApplied = player.damage(originalSource, lethalDamage);

                // Paso 2: Verificamos si el jugador murió
                if (player.isAlive()) {
                    LOGGER.warning("El daño original no mató al jugador " + player.getName().getString() +
                            ", DamageSource: " + originalSource.getName() +
                            ", intentando daño genérico");

                    // Paso 3: Fallback con daño genérico letal
                    DamageSource fallbackSource = player.getDamageSources().genericKill();
                    player.damage(fallbackSource, Float.MAX_VALUE);

                    // Paso 4: Última verificación (extremadamente raro)
                    if (player.isAlive()) {
                        LOGGER.warning("El daño genérico no mató al jugador " + player.getName().getString() +
                                ", forzando muerte manual");

                        // Simulamos la muerte manualmente para preservar el DamageSource original
                        player.setHealth(0.0F);
                        player.onDeath(originalSource); // Aseguramos eventos de muerte
                    }
                }

                // Paso 5: Limpiamos estados y sincronizamos solo si el jugador está muerto
                if (!player.isAlive()) {
                    PlayerReviveData.clear(uuid);
                    PlayerReviveNetwork.sendDownedState(player, false);
                } else {
                    LOGGER.severe("Fallo crítico: no se pudo matar al jugador " + player.getName().getString());
                }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al aplicar daño letal al jugador " + player.getName().getString(), e);
                // Limpieza de emergencia para evitar estados corruptos
                PlayerReviveData.clear(uuid);
                PlayerReviveNetwork.sendDownedState(player, false);
            }
        });
    }

    public static int getRemainingTicks(UUID uuid) {
        return timers.getOrDefault(uuid, 0);
    }

    public static boolean isRunning(UUID uuid) {
        return timers.containsKey(uuid);
    }
}