package com.TNTStudios.playerrevivefabric.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

public class PlayerReviveNetwork {
    public static final Identifier SET_DOWNED_PACKET = new Identifier("playerrevivefabric", "set_downed");

    public static void sendDownedState(ServerPlayerEntity player, boolean downed) {
        for (ServerPlayerEntity recipient : player.getServer().getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeUuid(player.getUuid());
            buf.writeBoolean(downed);
            ServerPlayNetworking.send(recipient, SET_DOWNED_PACKET, buf);
        }
    }

    public static void sendDownedState(ServerPlayerEntity downedPlayer, boolean downed, ServerPlayerEntity recipient) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(downedPlayer.getUuid());
        buf.writeBoolean(downed);
        ServerPlayNetworking.send(recipient, SET_DOWNED_PACKET, buf);
    }
}