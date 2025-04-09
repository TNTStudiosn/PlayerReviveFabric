package com.TNTStudios.playerrevivefabric.events;

import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class DeathEventHandler {

    // Para evitar múltiples ejecuciones al mismo tick
    private static final Set<ServerPlayerEntity> processedThisTick = new HashSet<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> processedThisTick.clear());

        // Manejo de muerte y daño a los jugadores
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (!ReviveManager.isDowned(player)) {
                    ReviveManager.downPlayer(player);
                    // ✅ Mantenerlo vivo
                    player.setHealth(1.0f); // mínima vida
                    player.setNoGravity(true);
                    player.setVelocity(Vec3d.ZERO);
                    player.setPose(EntityPose.SWIMMING);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100000, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100000, 255, false, false));
                    return false; // ❌ CANCELAR MUERTE
                }
            }
            return true; // Si ya está en estado downed, permitimos la muerte
        });
    }

    public static void handleRespawnTimeouts(ServerWorld world) {
        ReviveManager.tick(world); // Tiempos y revivir/respawn forzado
    }
}
