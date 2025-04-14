package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class LivingEntityMixin {

    // Evitar ataques al jugador en estado downed
    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void cancelAttackOnDownedPlayer(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof PlayerEntity player &&
                PlayerReviveData.isDowned(player.getUuid()) &&
                !player.isDead() && player.getHealth() > 0) {
            cir.setReturnValue(false);
        }
    }

    // Cancelar selección como objetivo
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingDownedPlayers(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity player &&
                PlayerReviveData.isDowned(player.getUuid()) &&
                !player.isDead() && player.getHealth() > 0) {
            ci.cancel(); // No se asigna como objetivo
        }
    }

    // Evitar que retengan al jugador como target si este cambia a estado downed
    @Inject(method = "tick", at = @At("HEAD"))
    private void clearTargetIfPlayerDowned(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        LivingEntity target = self.getTarget();
        if (target instanceof PlayerEntity player &&
                PlayerReviveData.isDowned(player.getUuid()) &&
                !player.isDead() && player.getHealth() > 0) {
            self.setTarget(null);
        }
    }
}
