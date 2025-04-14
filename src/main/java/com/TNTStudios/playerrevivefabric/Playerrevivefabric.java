package com.TNTStudios.playerrevivefabric;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveInteractionManager;
import com.TNTStudios.playerrevivefabric.util.ServerUtil;
import net.fabricmc.api.ModInitializer;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class Playerrevivefabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReviveConfig.init();
        RevivePackets.registerServer();

        ServerUtil.setServer(null);

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerUtil.setServer(server);
        });

        // Registrar el evento de tick del servidor para actualizar los revives una vez por tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ReviveInteractionManager.tickAll();
        });

        // Nuevo código: Sincronizar el estado "downed" con los nuevos jugadores
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity newPlayer = handler.player;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (PlayerReviveData.isDowned(player.getUuid())) {
                    PlayerReviveNetwork.sendDownedState(player, true, newPlayer);
                }
            }
        });
    }
}

