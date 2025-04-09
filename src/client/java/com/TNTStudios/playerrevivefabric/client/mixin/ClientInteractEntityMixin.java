package com.TNTStudios.playerrevivefabric.client.mixin;

import com.TNTStudios.playerrevivefabric.client.network.ClientRevivePackets;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientInteractEntityMixin {

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onClientInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof PlayerEntity target && target != player) {
            ClientRevivePackets.sendReviveAttempt(target);
            cir.setReturnValue(ActionResult.SUCCESS); // bloquea interacción vanilla
        }
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
    private void onClientInteractEntityAt(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof PlayerEntity target && target != player) {
            ClientRevivePackets.sendReviveAttempt(target);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
