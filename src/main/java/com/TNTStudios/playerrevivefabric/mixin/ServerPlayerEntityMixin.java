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

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void preventMovementIfDowned(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (ReviveManager.isDowned(player)) {
            player.setVelocity(Vec3d.ZERO);
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventAttackIfDowned(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (ReviveManager.isDowned(player)) {
            ci.cancel();
        }
    }
}

