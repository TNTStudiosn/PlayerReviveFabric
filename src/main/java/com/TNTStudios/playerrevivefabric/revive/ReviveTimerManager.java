package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveTimerManager {

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

    public static final float DAMAGE_MULTIPLIER = 100.0F;

    public static void forceDeath(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        stopTimer(uuid);
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveData.markDeathAccepted(uuid);
        PlayerReviveNetwork.sendDownedState(player, false);

        DamageSource source = PlayerReviveData.getLastDamageSource(player);
        if (player.getServer() != null) {
            player.getServer().execute(() -> {
                // Restauramos la salud al máximo antes de aplicar el daño letal
                player.setHealth(player.getMaxHealth());

                // Se calcula el daño letal como un múltiplo de la salud máxima del jugador
                float lethalDamage = player.getMaxHealth() * DAMAGE_MULTIPLIER;
                player.damage(source, lethalDamage);

                // En caso de que el jugador siga vivo, se fuerza la muerte asignando 0 de salud
                if (player.isAlive()) {
                    player.setHealth(0.0F);
                    player.getServer().getPlayerManager().broadcast(
                            Text.literal(player.getName().getString() + " murió por " + source.getName()),
                            false
                    );
                }
                // Limpiar estados y forzar sincronización
                PlayerReviveData.clear(uuid);
                PlayerReviveNetwork.sendDownedState(player, false); // Enviar estado nuevamente tras la muerte
            });
        }
    }


    private static void killPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        stopTimer(uuid);
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveData.markTimerExpired(uuid);
        PlayerReviveNetwork.sendDownedState(player, false);

        DamageSource source = PlayerReviveData.getLastDamageSource(player);
        if (player.getServer() != null) {
            player.getServer().execute(() -> {
                player.setHealth(player.getMaxHealth());
                player.damage(source, Float.MAX_VALUE);

                if (player.isAlive()) {
                    player.setHealth(0.0F);
                    player.getServer().getPlayerManager().broadcast(
                            Text.literal(player.getName().getString() + " murió por " + source.getName()),
                            false
                    );
                }
                // Limpiar estados y forzar sincronización
                PlayerReviveData.clear(uuid);
                PlayerReviveNetwork.sendDownedState(player, false); // Enviar estado nuevamente tras muerte
                ReviveInteractionManager.cancelIfBeingRevived(uuid);
            });
        }
    }

    public static int getRemainingTicks(UUID uuid) {
        return timers.getOrDefault(uuid, 0);
    }

    public static boolean isRunning(UUID uuid) {
        return timers.containsKey(uuid);
    }
}