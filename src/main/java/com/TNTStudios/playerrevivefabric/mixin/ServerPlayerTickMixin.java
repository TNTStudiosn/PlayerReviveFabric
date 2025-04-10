package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTickTail(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.isDead() || player.getHealth() <= 0) return;

        // Solo ejecutar lógica si el jugador está "downed"
        if (PlayerReviveData.isDowned(player.getUuid())) {
            ReviveTimerManager.tick(player);
            player.setPose(EntityPose.SWIMMING);
        }
        // Si no está "downed", no hacemos nada
    }
}