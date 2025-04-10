package com.TNTStudios.playerrevivefabric.revive;

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
                return null; // eliminar de la lista
            }
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
        stopTimer(player.getUuid());
        killPlayer(player);
    }

    private static void killPlayer(ServerPlayerEntity player) {
        player.setHealth(0.0F); // fuerza muerte
    }

    public static int getRemainingTicks(UUID uuid) {
        return timers.getOrDefault(uuid, 0);
    }

    public static boolean isRunning(UUID uuid) {
        return timers.containsKey(uuid);
    }
}
