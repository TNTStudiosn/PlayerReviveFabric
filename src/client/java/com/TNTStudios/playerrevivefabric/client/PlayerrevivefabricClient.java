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
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RevivePacketsClient.registerClient();

        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            UUID affectedPlayerUuid = buf.readUuid();
            boolean downed = buf.readBoolean();

            client.execute(() -> {
                // Siempre actualizar el estado downed, incluso si hasAcceptedDeath es true
                PlayerReviveData.setDowned(affectedPlayerUuid, downed);

                if (client.player != null && client.player.getUuid().equals(affectedPlayerUuid)) {
                    if (downed) {
                        if (!(client.currentScreen instanceof ReviveGui)) {
                            client.setScreen(new ReviveGui(ReviveConfig.get().defaultReviveTicks));
                        }
                    } else {
                        // Forzar limpieza del estado y cerrar GUI si está abierta
                        PlayerReviveData.clear(affectedPlayerUuid);
                        if (client.currentScreen instanceof ReviveGui) {
                            client.setScreen(null);
                        }
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
                });
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            ReviveProgressHud.render(context);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof PlayerEntity downed)) return ActionResult.PASS;

            if (!PlayerReviveData.isDowned(downed.getUuid())) return ActionResult.PASS;

            if (PlayerReviveData.isBeingRevived(downed.getUuid())) {
                player.sendMessage(Text.literal("Este jugador ya está siendo levantado por alguien").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }

            RevivePacketsClient.sendStartRevive(downed.getUuid());
            return ActionResult.SUCCESS;
        });

        ClientPlayNetworking.registerGlobalReceiver(RevivePackets.SET_BEING_REVIVED, (client, handler, buf, responseSender) -> {
            UUID downedUuid = buf.readUuid();
            boolean hasReviver = buf.readBoolean();
            UUID reviverUuid = hasReviver ? buf.readUuid() : null;
            client.execute(() -> {
                if (reviverUuid != null) {
                    PlayerReviveData.setBeingRevivedBy(downedUuid, reviverUuid);
                } else {
                    PlayerReviveData.clearReviving(downedUuid);
                }
            });
        });
    }
}
