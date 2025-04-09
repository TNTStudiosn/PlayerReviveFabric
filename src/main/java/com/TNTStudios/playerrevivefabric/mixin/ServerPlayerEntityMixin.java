package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin para manipular el movimiento y los ataques del jugador
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    // Apunta a la función tick (o cualquier función similar de movimiento)
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void preventMovementIfDowned(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        // Si el jugador está "downed", prevenimos el movimiento
        if (ReviveManager.isDowned(player)) {
            player.setVelocity(Vec3d.ZERO);  // Detener el movimiento
            ci.cancel();  // Cancelamos la ejecución del tick normal
        }
    }

    // Evita que el jugador ataque si está "downed"
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventAttackIfDowned(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        // Si el jugador está "downed", cancelamos el ataque
        if (ReviveManager.isDowned(player)) {
            ci.cancel();  // Cancelamos el ataque
        }
    }
}
