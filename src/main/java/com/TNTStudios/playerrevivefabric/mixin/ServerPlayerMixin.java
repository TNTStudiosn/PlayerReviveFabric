package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Solo aplicar lógica del mixin si el jugador está "downed"
        if (!PlayerReviveData.isDowned(player.getUuid())) {
            return; // Si no está "downed", dejar que el daño se procese normalmente
        }

        // Si está "downed", bloquear daño salvo que sea outOfWorld
        if (source != player.getDamageSources().outOfWorld()) {
            cir.setReturnValue(false);
            return;
        }

        // Si el golpe lo mataría y está "downed", marcar como "downed" (esto no debería ejecutarse aquí, pero lo dejamos por compatibilidad)
        if (player.getHealth() - amount <= 0.0F) {
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());
            cir.setReturnValue(false);
        }
    }
}
