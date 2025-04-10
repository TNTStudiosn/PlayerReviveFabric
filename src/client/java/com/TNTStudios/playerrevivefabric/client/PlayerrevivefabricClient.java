package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.client.gui.ReviveGui;
import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RevivePacketsClient.registerClient();

        // Escuchar paquete que marca jugador como "downed"
        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            UUID affectedPlayerUuid = buf.readUuid();
            boolean downed = buf.readBoolean();

            client.execute(() -> {
                if (PlayerReviveData.hasAcceptedDeath(affectedPlayerUuid)) return; // ❌ ya aceptó la muerte

                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                if (!downed) {
                    PlayerReviveData.setDowned(affectedPlayerUuid, false);
                    PlayerReviveData.clear(affectedPlayerUuid); // <- ✅ importante, elimina marca de muerte aceptada
                    if (MinecraftClient.getInstance().currentScreen instanceof ReviveGui) {
                        MinecraftClient.getInstance().setScreen(null);
                    }
                    return;
                }
            });
        });

        // Tick updater desde REVIVE_TIMER_SYNC
        ReviveClientHooks.registerCallbacks(ticks -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                if (client.currentScreen instanceof ReviveGui gui) {
                    gui.updateTicks(ticks);
                }
            }
        });
    }
}
