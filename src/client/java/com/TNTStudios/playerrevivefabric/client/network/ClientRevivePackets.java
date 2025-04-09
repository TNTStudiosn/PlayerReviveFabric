package com.TNTStudios.playerrevivefabric.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class ClientRevivePackets {

    public static final Identifier REVIVE_ATTEMPT = new Identifier("playerrevivefabric", "revive_attempt");

    public static void sendReviveAttempt(PlayerEntity target) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(target.getUuid());
        ClientPlayNetworking.send(REVIVE_ATTEMPT, buf);
    }
}
