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

        // Si el jugador ya está marcado como "downed", se cancela el daño
        if (PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        // Si el daño es letal, se activa el estado "downed" y se inicia el temporizador de revivificación.
        if (player.getHealth() - amount <= 0.0F) {
            // Se elimina la condición que ignoraba el daño outOfWorld,
            // permitiendo que este tipo de daño también active la mecánica de revivir.
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());
            cir.setReturnValue(false);
        }
    }
}
