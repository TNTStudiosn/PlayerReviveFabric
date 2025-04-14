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

        // 1) Ignorar creativo / espectador
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        // 2) Ignorar si el jugador ya aceptó la muerte o el temporizador expiró
        if (PlayerReviveData.hasAcceptedDeath(player.getUuid()) || PlayerReviveData.hasTimerExpired(player.getUuid())) {
            return;
        }

        // 3) Si ya está downed, no aplicar daño
        if (PlayerReviveData.isDowned(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        // 4) Verificar si la vida después del daño quedará en < 1 (medio corazón)
        float currentHealth = player.getHealth();
        float resultingHealth = currentHealth - amount;

        if (resultingHealth < 1.0F) {
            // Activar estado "downed" y cancelar muerte
            PlayerReviveData.setDowned(player.getUuid(), true);
            PlayerReviveData.setLastDamageSource(player.getUuid(), source);
            PlayerReviveNetwork.sendDownedState(player, true);
            ReviveTimerManager.startTimer(player.getUuid());

            // Mensaje global al servidor
            String playerName = player.getName().getString();
            Text message = Text.literal("[" + playerName + "] está tirado en el suelo D:")
                    .formatted(Formatting.RED);
            if (player.getServer() != null) {
                player.getServer().getPlayerManager().broadcast(message, false);
            }

            // Cancelar el daño letal
            cir.setReturnValue(false);
        }
    }
}
