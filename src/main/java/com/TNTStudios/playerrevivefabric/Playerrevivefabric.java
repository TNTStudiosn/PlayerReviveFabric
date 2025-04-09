package com.TNTStudios.playerrevivefabric;

import com.TNTStudios.playerrevivefabric.commands.AcceptDeathCommand;
import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.events.DeathEventHandler;
import com.TNTStudios.playerrevivefabric.server.PlayerReviveHandler;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class Playerrevivefabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReviveConfig.load();

        DeathEventHandler.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                DeathEventHandler.handleRespawnTimeouts(world);
            }
            PlayerReviveHandler.tick(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AcceptDeathCommand.register(dispatcher);
        });
    }
}
