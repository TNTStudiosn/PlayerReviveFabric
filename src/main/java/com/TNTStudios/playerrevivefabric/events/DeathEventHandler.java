package com.TNTStudios.playerrevivefabric.events;

import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameRules;

import java.util.HashSet;
import java.util.Set;

public class DeathEventHandler {

    // Para evitar múltiples ejecuciones al mismo tick
    private static final Set<ServerPlayerEntity> processedThisTick = new HashSet<>();

    public static void init() {
        // Evento por tick para limpiar jugadores procesados
        ServerTickEvents.END_SERVER_TICK.register(server -> processedThisTick.clear());
    }

    public static ActionResult onPlayerDamaged(ServerPlayerEntity player, DamageSource source, float amount) {
        if (processedThisTick.contains(player)) return ActionResult.PASS;
        processedThisTick.add(player);

        if (amount >= player.getHealth()) {
            if (!ReviveManager.isDowned(player)) {
                ReviveManager.downPlayer(player);
                // ✅ PERMITIMOS la muerte, pero ya sabremos que está en estado downed
            }
        }
        return ActionResult.PASS;


    }


    public static void handleRespawnTimeouts(ServerWorld world) {
        ReviveManager.tick(world); // Tiempos y revivir/respawn forzado
    }
}
