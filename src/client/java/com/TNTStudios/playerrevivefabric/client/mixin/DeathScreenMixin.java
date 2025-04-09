package com.TNTStudios.playerrevivefabric.client.mixin;

import com.TNTStudios.playerrevivefabric.client.ClientReviveState;
import com.TNTStudios.playerrevivefabric.client.gui.ReviveScreen;
import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    protected DeathScreenMixin() {
        super(NarratorManager.EMPTY);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void blockVanillaTick(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ClientReviveState.isDowned) {
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void overrideDeathScreen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player != null && ClientReviveState.isDowned) {
            client.setScreen(new ReviveScreen(ReviveConfig.reviveTimeMs));
            ci.cancel();
        }
    }

    @Inject(method = "shouldCloseOnEsc", at = @At("HEAD"), cancellable = true)
    private void preventEscClose(CallbackInfoReturnable<Boolean> cir) {
        if (ClientReviveState.isDowned) {
            cir.setReturnValue(false);
        }
    }
}

