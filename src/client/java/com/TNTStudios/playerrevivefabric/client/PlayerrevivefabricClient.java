package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.client.gui.ReviveGui;
import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RevivePacketsClient.registerClient();

        // Registrar receptor para el paquete "set_downed"
        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            UUID affectedPlayerUuid = buf.readUuid();
            boolean downed = buf.readBoolean();

            client.execute(() -> {
                PlayerReviveData.setDowned(affectedPlayerUuid, downed);
            });
        });

        // Registro del callback para sincronización de ticks en GUI
        ReviveClientHooks.registerCallbacks(ticks -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                if (client.currentScreen instanceof ReviveGui gui) {
                    gui.updateTicks(ticks);
                } else {
                    client.setScreen(new ReviveGui(ticks));
                }
            }
        });
    }
}
