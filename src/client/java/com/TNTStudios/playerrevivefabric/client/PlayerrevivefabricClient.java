package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.client.gui.ReviveGui;
import com.TNTStudios.playerrevivefabric.client.gui.ReviveProgressHud;
import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import com.TNTStudios.playerrevivefabric.revive.ReviveConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RevivePacketsClient.registerClient();

        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            UUID affectedPlayerUuid = buf.readUuid();
            boolean downed = buf.readBoolean();

            client.execute(() -> {
                if (PlayerReviveData.hasAcceptedDeath(affectedPlayerUuid)) return;

                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                if (client.player != null && client.player.getUuid().equals(affectedPlayerUuid)) {
                    if (downed) {
                        if (!(client.currentScreen instanceof ReviveGui)) {
                            client.setScreen(new ReviveGui(ReviveConfig.get().defaultReviveTicks));
                        }
                    } else {
                        if (client.currentScreen instanceof ReviveGui) {
                            client.setScreen(null);
                        }
                        PlayerReviveData.clear(affectedPlayerUuid);
                    }

                }
            });
        });

        ReviveClientHooks.registerCallbacks(ticks -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                if (client.currentScreen instanceof ReviveGui gui) {
                    gui.updateTicks(ticks);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null && client.player != null) {
                client.execute(() -> {
                    // Lógica extra si es necesaria
                });
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            ReviveProgressHud.render(context);
        });


    }
}
