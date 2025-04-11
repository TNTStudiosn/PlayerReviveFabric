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
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (player.isDead() || player.getHealth() <= 0) return;
        ReviveTimerManager.tick(player);
        if (PlayerReviveData.isDowned(player.getUuid())) {
            player.setPose(EntityPose.SWIMMING);
        }
        // Eliminado: if (player.getServer() != null && player.getServer().getTicks() % 2 == 0) {
        //     ReviveInteractionManager.tickAll();
        // }
    }
}