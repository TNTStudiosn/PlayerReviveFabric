package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.RevivePackets;
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

        if (!PlayerReviveData.isDowned(downed)) {
            reviver.sendMessage(
                    Text.literal("Este jugador ya ha sido revivido").formatted(Formatting.RED),
                    false
            );
            return;
        }

        if (PlayerReviveData.isBeingRevived(downed)) {
            reviver.sendMessage(
                    Text.literal("Este jugador ya está siendo levantado por alguien").formatted(Formatting.RED),
                    false
            );
            return;
        }

        PlayerReviveData.setBeingRevivedBy(downed, reviver.getUuid());
        ReviveProgressTracker tracker = new ReviveProgressTracker(downed, reviver, 100);
        activeRevives.put(downed, tracker);

        // Enviar actualización a todos los jugadores
        for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
            RevivePackets.sendBeingRevivedUpdate(recipient, downed, reviver.getUuid());
        }
    }


    public static void handleCancelRevive(MinecraftServer server, ServerPlayerEntity reviver, UUID downed) {
        ReviveProgressTracker tracker = activeRevives.get(downed);
        if (tracker != null && tracker.getReviver().equals(reviver.getUuid())) {
            activeRevives.remove(downed);
            PlayerReviveData.clearReviving(downed);
            // Enviar actualización a todos los jugadores
            for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
                RevivePackets.sendBeingRevivedUpdate(recipient, downed, null);
            }
        }
    }

    public static void cancelIfBeingRevived(UUID downed) {
        ReviveProgressTracker tracker = activeRevives.remove(downed);
        if (tracker != null) {
            PlayerReviveData.clearReviving(downed);
        }
    }

    public static void tickAll() {
        activeRevives.values().removeIf(ReviveProgressTracker::tick);
    }
}
