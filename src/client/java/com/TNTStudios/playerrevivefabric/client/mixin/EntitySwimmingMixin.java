package com.TNTStudios.playerrevivefabric.client.mixin;

import com.TNTStudios.playerrevivefabric.client.PlayerReviveClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySwimmingMixin {

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void forceSwimmingStateClient(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        if (self instanceof PlayerEntity player &&
                player == MinecraftClient.getInstance().player &&
                PlayerReviveClientState.isDowned()) {
            cir.setReturnValue(true);
        }
    }
}
