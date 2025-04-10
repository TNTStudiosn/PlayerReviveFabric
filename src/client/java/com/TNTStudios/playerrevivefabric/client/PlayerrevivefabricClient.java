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
                if (PlayerReviveData.hasAcceptedDeath(affectedPlayerUuid)) return; // No hacer nada si ya aceptó la muerte

                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                if (client.player != null && client.player.getUuid().equals(affectedPlayerUuid)) {
                    if (downed) {
                        // Solo abrir la GUI si aún no está activa
                        if (!(client.currentScreen instanceof ReviveGui)) {
                            client.setScreen(new ReviveGui(ReviveConfig.get().defaultReviveTicks));
                        }
                    } else {
                        // Cerrar la GUI si el jugador local sale del estado "down"
                        if (client.currentScreen instanceof ReviveGui) {
                            client.setScreen(null);
                        }
                        PlayerReviveData.clear(affectedPlayerUuid); // Limpiar datos al salir del estado "down"
                    }

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
