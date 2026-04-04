package com.TNTStudios.playerrevivefabric.command;

import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveInteractionManager;
import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class RevivirCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("revivir")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
                            return executeRevive(context, java.util.List.of(player));
                        }
                        return 0;
                    })
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                            .executes(context -> executeRevive(context, EntityArgumentType.getPlayers(context, "targets")))
                    )
            );
        });
    }

    private static int executeRevive(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int revivedCount = 0;
        
        for (ServerPlayerEntity player : targets) {
            if (PlayerReviveData.isDowned(player.getUuid())) {
                // Notificar a todos que ya no está siendo revivido
                if (PlayerReviveData.isBeingRevived(player.getUuid())) {
                    for (ServerPlayerEntity recipient : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                        RevivePackets.sendBeingRevivedUpdate(recipient, player.getUuid(), null);
                    }
                }
                
                // Cancelar cualquier intento de levantar en curso
                ReviveInteractionManager.cancelIfBeingRevived(player.getUuid());
                
                // Detener el temporizador de muerte
                ReviveTimerManager.stopTimer(player.getUuid());
                
                // Cambiar estado y limpiar datos
                PlayerReviveData.setDowned(player.getUuid(), false);
                PlayerReviveData.clear(player.getUuid());
                com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork.sendDownedState(player, false);
                
                // Dar la salud configurada
                float healthToSet = player.getMaxHealth() * ReviveConfig.get().reviveHealthPercentage;
                player.setHealth(Math.max(1.0F, healthToSet));
                
                revivedCount++;
            }
        }
        
        final int count = revivedCount;
        if (count > 0) {
            context.getSource().sendFeedback(() -> Text.literal("Se han revivido " + count + " jugador(es)."), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Ningún jugador objetivo estaba caído."), false);
        }
        
        return count;
    }
}