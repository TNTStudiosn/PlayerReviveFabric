package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.network.RevivePackets;
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
    }

    public static void sendAcceptDeath() {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ClientPlayNetworking.send(RevivePackets.ACCEPT_DEATH, buf);
    }
}
