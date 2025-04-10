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

        if (PlayerReviveData.isDowned(player.getUuid()) &&
                source != player.getDamageSources().outOfWorld()) {
            cir.setReturnValue(false);
            return;
        }

        if (player.getHealth() - amount <= 0.0F) {
            if (source == player.getDamageSources().outOfWorld()) {
                return;
            }
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());
            cir.setReturnValue(false);
        }
    }
}

