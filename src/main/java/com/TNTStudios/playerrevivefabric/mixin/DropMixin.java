package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class DropMixin {

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void onDropAll(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory)(Object)this;
        PlayerEntity player = inventory.player;

        if (player instanceof ServerPlayerEntity serverPlayer
                && ReviveManager.isDowned(serverPlayer)
                && serverPlayer.isAlive()) {
            // 🚫 Solo bloquea si el jugador está downed y aún NO ha muerto del todo
            ci.cancel();
        }
    }
}

