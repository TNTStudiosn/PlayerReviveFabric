package com.TNTStudios.playerrevivefabric.revive;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveInteractionManager {

    private static final Map<UUID, ReviveProgressTracker> activeRevives = new ConcurrentHashMap<>();

    public static void handleStartRevive(MinecraftServer server, ServerPlayerEntity reviver, UUID downed) {
        ServerPlayerEntity downedPlayer = server.getPlayerManager().getPlayer(downed);
        if (downedPlayer == null) return;

        if (PlayerReviveData.isBeingRevived(downed)) {
            reviver.sendMessage(Text.literal("Este jugador ya está siendo levantado por alguien").formatted(Formatting.RED), false);
            return;
        }

        PlayerReviveData.setBeingRevivedBy(downed, reviver.getUuid());
        ReviveTimerManager.stopTimer(downed); // Detener muerte
        ReviveProgressTracker tracker = new ReviveProgressTracker(downed, reviver, 100); // 5 segundos
        activeRevives.put(downed, tracker);
    }

    public static void handleCancelRevive(MinecraftServer server, ServerPlayerEntity reviver, UUID downed) {
        ReviveProgressTracker tracker = activeRevives.get(downed);
        if (tracker != null && tracker.getReviver().equals(reviver.getUuid())) {
            activeRevives.remove(downed);
            PlayerReviveData.clearReviving(downed);
        }
    }

    public static void cancelIfBeingRevived(UUID downed) {
        ReviveProgressTracker tracker = activeRevives.remove(downed);
        if (tracker != null) {
            PlayerReviveData.clearReviving(downed);
        }
    }

    public static void tickAll() {
        activeRevives.values().removeIf(ReviveProgressTracker::tick); // true si terminó
    }
}
