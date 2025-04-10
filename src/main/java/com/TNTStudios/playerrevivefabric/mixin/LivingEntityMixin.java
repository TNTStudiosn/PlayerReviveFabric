package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void cancelAttackOnDownedPlayer(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof PlayerEntity player &&
                PlayerReviveData.isDowned(player.getUuid()) &&
                !player.isDead() && player.getHealth() > 0) {
            cir.setReturnValue(false);
        }

    }
}
