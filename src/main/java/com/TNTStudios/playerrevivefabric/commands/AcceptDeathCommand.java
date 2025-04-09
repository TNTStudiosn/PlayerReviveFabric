package com.TNTStudios.playerrevivefabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AcceptDeathCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("acceptdeath")
                .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (ReviveManager.isDowned(player)) {
                        ReviveManager.forceDeath(player);
                    }
                    return 1;
                })
        );
    }
}
