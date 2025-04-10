package com.TNTStudios.playerrevivefabric.revive;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerReviveData {
    private static final Set<UUID> downedPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, DamageSource> lastDamageSource = new ConcurrentHashMap<>();
    private static final Set<UUID> hasAcceptedDeath = ConcurrentHashMap.newKeySet();

    public static boolean isDowned(UUID playerId) {
        return downedPlayers.contains(playerId);
    }

    public static void setDowned(UUID playerId, boolean downed) {
        if (downed) {
            downedPlayers.add(playerId);
        } else {
            downedPlayers.remove(playerId);
        }
    }

    public static void setLastDamageSource(UUID uuid, DamageSource source) {
        lastDamageSource.put(uuid, source);
    }

    public static DamageSource getLastDamageSource(ServerPlayerEntity player) {
        return lastDamageSource.getOrDefault(player.getUuid(), player.getDamageSources().generic());
    }

    public static void markDeathAccepted(UUID uuid) {
        hasAcceptedDeath.add(uuid);
    }

    public static boolean hasAcceptedDeath(UUID uuid) {
        return hasAcceptedDeath.contains(uuid);
    }

    public static void clear(UUID uuid) {
        downedPlayers.remove(uuid);
        lastDamageSource.remove(uuid);
        hasAcceptedDeath.remove(uuid);
    }
}
