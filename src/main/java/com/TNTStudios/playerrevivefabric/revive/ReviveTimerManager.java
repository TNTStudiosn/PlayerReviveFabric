package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
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
        UUID uuid = player.getUuid();

        stopTimer(uuid);

        // ✅ Limpieza antes de dañar
        PlayerReviveData.clear(uuid);

        // ✅ Notificar a todos que ya no está downed (GUI se cerrará)
        PlayerReviveNetwork.sendDownedState(player, false);

        // ✅ Ahora sí, aplicar daño mortal (ya no es considerado downed)
        player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
    }



    private static void killPlayer(ServerPlayerEntity player) {
        DamageSource source = PlayerReviveData.getLastDamageSource(player);

        // Mandar "downed = false" a todos, para que el cliente cierre la GUI
        PlayerReviveNetwork.sendDownedState(player, false);

        PlayerReviveData.clear(player.getUuid());
        // Golpe mortal con OUT_OF_WORLD
        player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
    }



    public static int getRemainingTicks(UUID uuid) {
        return timers.getOrDefault(uuid, 0);
    }

    public static boolean isRunning(UUID uuid) {
        return timers.containsKey(uuid);
    }
}
