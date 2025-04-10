package com.TNTStudios.playerrevivefabric;

import net.fabricmc.api.ModInitializer;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;

public class Playerrevivefabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReviveConfig.init();
        RevivePackets.registerServer();
    }
}
