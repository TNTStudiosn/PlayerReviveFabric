package com.TNTStudios.playerrevivefabric.network;

import com.TNTStudios.playerrevivefabric.server.PlayerReviveHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class RevivePackets {

    public static final Identifier REVIVE_ATTEMPT = new Identifier("playerrevivefabric", "revive_attempt");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(REVIVE_ATTEMPT, (server, player, handler, buf, responseSender) -> {
            UUID targetId = buf.readUuid();
            server.execute(() -> {
                ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetId);
                if (target != null) {
                    PlayerReviveHandler.onRightClick(player, target);
                }
            });
        });
    }
}
