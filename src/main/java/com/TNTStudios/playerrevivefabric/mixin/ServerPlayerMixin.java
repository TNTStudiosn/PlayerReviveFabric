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

        // Si el jugador está downed y el daño no es "outOfWorld", cancelar el daño.
        if (PlayerReviveData.isDowned(player.getUuid()) &&
                source != player.getDamageSources().outOfWorld()) {
            cir.setReturnValue(false);
            return;
        }

        // Si el daño es letal...
        if (player.getHealth() - amount <= 0.0F) {
            // Si la fuente es outOfWorld, permitimos la muerte normal.
            if (source == player.getDamageSources().outOfWorld()) {
                // No cancelar; dejar que el daño se procese para matar al jugador.
                return;
            }
            // De lo contrario, marcar al jugador como downed.
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());
            cir.setReturnValue(false);
        }
    }
}

