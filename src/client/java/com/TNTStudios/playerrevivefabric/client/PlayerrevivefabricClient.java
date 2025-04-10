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

        // Registrar receptor para el paquete "set_downed"
        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            UUID affectedPlayerUuid = buf.readUuid();
            boolean downed = buf.readBoolean();

            client.execute(() -> {
                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                // Si es el jugador local y acaba de quedar downed, mostrar GUI
                if (downed &&
                        MinecraftClient.getInstance().player != null &&
                        MinecraftClient.getInstance().player.getUuid().equals(affectedPlayerUuid)) {

                    // Abrir GUI manualmente por primera vez (sin esperar packet de tiempo)
                    int initialTicks = ReviveConfig.get().defaultReviveTicks;
                    if (!(MinecraftClient.getInstance().currentScreen instanceof ReviveGui)) {
                        MinecraftClient.getInstance().setScreen(new ReviveGui(initialTicks));
                    }
                }
            });
        });

        // Registro del callback para sincronización de ticks en GUI
        ReviveClientHooks.registerCallbacks(new ReviveClientHooks.ReviveClientCallbacks() {
            @Override
            public void showOrUpdateReviveGui(int ticks) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    if (client.currentScreen instanceof ReviveGui gui) {
                        gui.updateTicks(ticks);
                    } else {
                        client.setScreen(new ReviveGui(ticks));
                    }
                }
            }
        });
    }
}
