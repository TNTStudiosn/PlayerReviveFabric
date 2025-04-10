package com.TNTStudios.playerrevivefabric.client;


import com.TNTStudios.playerrevivefabric.client.gui.ReviveProgressHud;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class RevivePacketsClient {

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(RevivePackets.REVIVE_TIMER_SYNC, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            int ticks = buf.readInt();

            client.execute(() -> {
                if (MinecraftClient.getInstance().player != null &&
                        MinecraftClient.getInstance().player.getUuid().equals(uuid)) {
                    ReviveClientHooks.updateReviveTimer(ticks);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RevivePackets.REVIVE_PROGRESS, (client, handler, buf, responseSender) -> {
            UUID downed = buf.readUuid();
            int ticks = buf.readInt();

            client.execute(() -> {
                ReviveProgressHud.updateProgress(downed, ticks);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RevivePackets.REVIVE_SUCCESS, (client, handler, buf, responseSender) -> {
            UUID downed = buf.readUuid();
            client.execute(() -> {
                ReviveProgressHud.clear();
                if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getUuid().equals(downed)) {
                    PlayerReviveData.setDowned(downed, false);
                    PlayerReviveData.clear(downed);
                    MinecraftClient.getInstance().setScreen(null);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RevivePackets.REVIVE_CANCELLED, (client, handler, buf, responseSender) -> {
            UUID target = buf.readUuid();
            client.execute(() -> {
                ReviveProgressHud.clear();
            });
        });


    }

    public static void sendAcceptDeath() {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ClientPlayNetworking.send(RevivePackets.ACCEPT_DEATH, buf);
    }

    public static void sendStartRevive(UUID downedUuid) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(downedUuid);
        ClientPlayNetworking.send(RevivePackets.START_REVIVE, buf);
    }

    public static void sendCancelRevive(UUID downedUuid) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(downedUuid);
        ClientPlayNetworking.send(RevivePackets.CANCEL_REVIVE, buf);
    }

}
