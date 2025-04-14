package com.TNTStudios.playerrevivefabric.mixin;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Permitir daño si el jugador ha aceptado la muerte o el temporizador ha expirado
        if (PlayerReviveData.hasAcceptedDeath(player.getUuid()) || PlayerReviveData.hasTimerExpired(player.getUuid())) {
            return;
        }

        // Si el jugador ya está derribado, cancelar el daño
        if (PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        float currentHealth = player.getHealth();
        float resultingHealth = currentHealth - amount;

        // Activar estado "downed" si se baja de 2 a 1 o menos (equivale a de 1 corazón a medio corazón o menos)
        if (currentHealth > 1.0F && resultingHealth <= 1.0F) {
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());

            String playerName = player.getName().getString();
            Text message = Text.literal("[" + playerName + "] está tirado en el suelo D:").formatted(Formatting.RED);
            if (player.getServer() != null) {
                player.getServer().getPlayerManager().broadcast(message, false);
            }

            cir.setReturnValue(false);
        }
    }

}