package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveTimerManager {

    private static final Map<UUID, Integer> timers = new ConcurrentHashMap<>();

    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!PlayerReviveData.isDowned(uuid)) return;

        timers.computeIfPresent(uuid, (id, ticksLeft) -> {
            if (ticksLeft <= 1) {
                killPlayer(player);
                return null; // Eliminar de la lista
            }
            RevivePackets.sendTimerUpdate(player, ticksLeft - 1); // Enviar actualización al cliente
            return ticksLeft - 1;
        });
    }

    public static void startTimer(UUID uuid) {
        timers.put(uuid, ReviveConfig.get().defaultReviveTicks);
    }

    public static void stopTimer(UUID uuid) {
        timers.remove(uuid);
    }

    public static void forceDeath(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        stopTimer(uuid);
        // Marcar como no "downed"
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveNetwork.sendDownedState(player, false);
        // Agregar esta línea para limpiar la marca de muerte aceptada
        PlayerReviveData.clear(uuid);

        // Restaurar salud para evitar conflictos y forzar el daño letal
        player.setHealth(20.0F);
        boolean damaged = player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
        System.out.println("Daño aplicado: " + damaged);
    }


    private static void killPlayer(ServerPlayerEntity player) {
        PlayerReviveData.setDowned(player.getUuid(), false); // Marcar como no "downed"
        PlayerReviveNetwork.sendDownedState(player, false);
        PlayerReviveData.clear(player.getUuid());
        player.setHealth(20.0F); // Restaurar salud
        boolean damaged = player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
        System.out.println("Daño aplicado: " + damaged);
    }



    public static int getRemainingTicks(UUID uuid) {
        return timers.getOrDefault(uuid, 0);
    }

    public static boolean isRunning(UUID uuid) {
        return timers.containsKey(uuid);
    }
}
