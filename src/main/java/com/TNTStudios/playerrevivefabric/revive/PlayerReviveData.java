package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.util.ServerUtil;
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
    private static final Set<UUID> hasTimerExpired = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, UUID> beingRevivedBy = new ConcurrentHashMap<>();

    public static boolean isDowned(UUID playerId) {
        return downedPlayers.contains(playerId);
    }

    public static void setDowned(UUID playerId, boolean downed) {
        if (downed) {
            downedPlayers.add(playerId);
        } else {
            downedPlayers.remove(playerId);
        }
        // Notificar a todos los clientes
        ServerPlayerEntity player = ServerUtil.getPlayer(playerId);
        if (player != null) {
            PlayerReviveNetwork.sendDownedState(player, downed);
        }
    }

    public static void setLastDamageSource(UUID uuid, DamageSource source) {
        lastDamageSource.put(uuid, source);
    }

    public static DamageSource getLastDamageSource(ServerPlayerEntity player) {
        return lastDamageSource.getOrDefault(player.getUuid(), player.getDamageSources().genericKill());
    }

    public static void markDeathAccepted(UUID uuid) {
        hasAcceptedDeath.add(uuid);
    }

    public static boolean hasAcceptedDeath(UUID uuid) {
        return hasAcceptedDeath.contains(uuid);
    }

    public static void markTimerExpired(UUID uuid) {
        hasTimerExpired.add(uuid);
    }

    public static boolean hasTimerExpired(UUID uuid) {
        return hasTimerExpired.contains(uuid);
    }

    public static void setBeingRevivedBy(UUID downed, UUID reviver) {
        beingRevivedBy.put(downed, reviver);
    }

    public static UUID getReviver(UUID downed) {
        return beingRevivedBy.get(downed);
    }

    public static boolean isBeingRevived(UUID downed) {
        return beingRevivedBy.containsKey(downed);
    }

    public static void clearReviving(UUID downed) {
        beingRevivedBy.remove(downed);
    }

    public static void clear(UUID uuid) {
        downedPlayers.remove(uuid);
        lastDamageSource.remove(uuid);
        hasAcceptedDeath.remove(uuid);
        hasTimerExpired.remove(uuid);
    }
}