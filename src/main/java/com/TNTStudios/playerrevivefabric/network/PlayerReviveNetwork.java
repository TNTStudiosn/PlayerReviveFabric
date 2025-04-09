package com.TNTStudios.playerrevivefabric.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

public class PlayerReviveNetwork {
    public static final Identifier SET_DOWNED_PACKET = new Identifier("playerrevivefabric", "set_downed");

    public static void sendDownedState(ServerPlayerEntity player, boolean downed) {
        // Se recorre la lista de jugadores y se envía el paquete a todos
        for (ServerPlayerEntity recipient : player.getServer().getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            // Escribe el UUID del jugador afectado
            buf.writeUuid(player.getUuid());
            buf.writeBoolean(downed);
            ServerPlayNetworking.send(recipient, SET_DOWNED_PACKET, buf);
        }
    }
}
