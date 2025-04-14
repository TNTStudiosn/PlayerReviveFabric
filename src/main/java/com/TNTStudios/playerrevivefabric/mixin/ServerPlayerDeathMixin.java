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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onInterceptDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (player.isCreative() || player.isSpectator()) return;
        if (PlayerReviveData.hasAcceptedDeath(player.getUuid()) || PlayerReviveData.hasTimerExpired(player.getUuid())) return;
        if (PlayerReviveData.isDowned(player.getUuid())) return;

        PlayerReviveData.setDowned(player.getUuid(), true);
        PlayerReviveData.setLastDamageSource(player.getUuid(), source);
        PlayerReviveNetwork.sendDownedState(player, true);
        ReviveTimerManager.startTimer(player.getUuid());

        Text message = Text.literal("[" + player.getName().getString() + "] está tirado en el suelo D:").formatted(Formatting.RED);
        if (player.getServer() != null) {
            player.getServer().getPlayerManager().broadcast(message, false);
        }
        player.setHealth(1.0F); // Mantener con vida mínima visible

        ci.cancel(); // ← esto evita la muerte real
    }
}
