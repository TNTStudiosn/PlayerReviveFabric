package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.EntityPose;
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

        // (1) Si ya está tumbado, cancela daño
        if (PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        // (2) Si el golpe mataría al jugador, evitar la muerte y marcarlo "downed"
        if (player.getHealth() - amount <= 0.0F) {
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveNetwork.sendDownedState(player, true); // Enviar al cliente
            cir.setReturnValue(false); // no muere
        }

    }
}
