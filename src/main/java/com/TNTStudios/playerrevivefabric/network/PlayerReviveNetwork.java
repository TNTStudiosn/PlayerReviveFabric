package com.TNTStudios.playerrevivefabric.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

public class PlayerReviveNetwork {
    public static final Identifier SET_DOWNED_PACKET = new Identifier("playerrevivefabric", "set_downed");

    public static void sendDownedState(ServerPlayerEntity player, boolean downed) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(downed);
        ServerPlayNetworking.send(player, SET_DOWNED_PACKET, buf);
    }
}
