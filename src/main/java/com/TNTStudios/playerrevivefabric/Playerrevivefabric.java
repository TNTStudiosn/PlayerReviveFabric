package com.TNTStudios.playerrevivefabric;

import com.TNTStudios.playerrevivefabric.revive.ReviveInteractionManager;
import com.TNTStudios.playerrevivefabric.util.ServerUtil;
import net.fabricmc.api.ModInitializer;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

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
    }
}
