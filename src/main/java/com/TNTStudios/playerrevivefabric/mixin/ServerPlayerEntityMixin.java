package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    // Interceptamos el método "tick" para cancelar el movimiento si el jugador está "downed"
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void preventMovementIfDowned(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        // Si el jugador está "downed", cancelamos el movimiento
        if (ReviveManager.isDowned(player)) {
            player.setVelocity(Vec3d.ZERO);  // Establecemos la velocidad a cero para evitar movimiento
            ci.cancel();  // Cancelamos el tick normal (evitamos que el jugador se mueva)
        }
    }

    // Interceptamos el método de ataque para evitar que ataque si está "downed"
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventAttackIfDowned(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        // Si el jugador está "downed", cancelamos el ataque
        if (ReviveManager.isDowned(player)) {
            ci.cancel();  // Cancelamos el ataque
        }
    }
}
