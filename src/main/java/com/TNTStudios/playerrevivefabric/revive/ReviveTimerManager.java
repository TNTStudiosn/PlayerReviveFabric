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
        PlayerReviveNetwork.sendDownedState(player, false);
        PlayerReviveData.clear(uuid);

        player.setHealth(20.0F);
        boolean damaged = player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
        System.out.println("Daño aplicado: " + damaged);
    }

    private static void killPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveNetwork.sendDownedState(player, false);
        PlayerReviveData.clear(uuid);
        player.setHealth(20.0F);
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
