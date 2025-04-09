package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.client.network.ClientRevivePackets;
import net.fabricmc.api.ClientModInitializer;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientRevivePackets.REVIVE_ATTEMPT.toString();
        ClientRevivePackets.registerS2CPackets();
    }
}
