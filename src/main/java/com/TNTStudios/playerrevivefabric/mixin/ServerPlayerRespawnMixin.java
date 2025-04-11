package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerRespawnMixin {

    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void onRespawn(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        UUID uuid = player.getUuid();

        // Reiniciar todos los estados al reaparecer
        PlayerReviveData.setDowned(uuid, false);
        PlayerReviveData.clear(uuid);
        PlayerReviveNetwork.sendDownedState(player, false); // Forzar sincronización tras respawn
    }
}