package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Si ya está tumbado, cancelar daño completamente
        if (PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        // Si el daño lo mataría, lo tumbamos
        if (player.getHealth() - amount <= 0) {
            player.setHealth(1.0F);
            player.setSwimming(true);
            player.setVelocity(Vec3d.ZERO);
            player.velocityModified = true;
            PlayerReviveData.setDowned(player.getUuid(), true);
            cir.setReturnValue(false);
        }
    }
}
