package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SwimmingPoseMixin {

    /**
     * This method decides if the entity is in a pose for swimming.
     * By forcing it to true when "downed", the game never reverts to STANDING.
     */
    @Inject(method = "isInSwimmingPose", at = @At("HEAD"), cancellable = true)
    private void forceSwimmingIfDowned(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        // Revisa si es un jugador con downed = true
        if (self instanceof PlayerEntity player && PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(true);  // Forzar "sí, está en pose de nadar"
        }
    }
}
