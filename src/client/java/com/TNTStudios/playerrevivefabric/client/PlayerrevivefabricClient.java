package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.client.network.PlayerReviveClientNetwork;
import net.fabricmc.api.ClientModInitializer;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PlayerReviveClientNetwork.register();
    }
}
