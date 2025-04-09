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

    /**
     * Reemplaza la pantalla de muerte vanilla por la pantalla personalizada solo si el jugador está en estado downed.
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void replaceIfDowned(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null && ClientReviveState.isDowned) {
            // Mostramos nuestra pantalla personalizada
            client.setScreen(new ReviveScreen(ReviveConfig.reviveTimeMs));
            ci.cancel(); // Cancelamos el init vanilla para evitar botones innecesarios
        }
    }

    /**
     * Impide que se muestren botones vanilla si el jugador está downed (seguridad extra por si algún mod u override llama `init` luego).
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void blockTickIfDowned(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ClientReviveState.isDowned) {
            ci.cancel(); // Impide que se activen botones vanilla
        }
    }

    /**
     * Opcional: previene que la pantalla se cierre con ESC mientras estás derribado.
     */
    @Inject(method = "shouldCloseOnEsc", at = @At("HEAD"), cancellable = true)
    private void blockEscIfDowned(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ClientReviveState.isDowned) {
            // Bloquea cerrar pantalla con ESC si el jugador está tirado
            cir.setReturnValue(false);
        }
    }

}
