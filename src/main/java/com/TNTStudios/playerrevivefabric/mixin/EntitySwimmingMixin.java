package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySwimmingMixin {

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void forceSwimmingStateServerAndClient(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        if (self instanceof PlayerEntity player &&
                PlayerReviveData.isDowned(player.getUuid()) &&
                !player.isDead() && player.getHealth() > 0) {
            cir.setReturnValue(true);
        }
    }
}
