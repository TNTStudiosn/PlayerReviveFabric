package com.TNTStudios.playerrevivefabric.revive;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerReviveData {
    private static final Set<UUID> downedPlayers = ConcurrentHashMap.newKeySet();

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
}
