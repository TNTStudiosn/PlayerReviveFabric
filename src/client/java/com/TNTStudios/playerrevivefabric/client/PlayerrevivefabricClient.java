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
                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                // Si el jugador es el cliente y está downed, mostrar GUI (si aún no está abierta)
                if (downed &&
                        MinecraftClient.getInstance().player != null &&
                        MinecraftClient.getInstance().player.getUuid().equals(affectedPlayerUuid)) {

                    if (!(MinecraftClient.getInstance().currentScreen instanceof ReviveGui)) {
                        int initialTicks = ReviveConfig.get().defaultReviveTicks;
                        MinecraftClient.getInstance().setScreen(new ReviveGui(initialTicks));
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
