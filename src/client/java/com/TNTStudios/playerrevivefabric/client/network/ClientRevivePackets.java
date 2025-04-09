package com.TNTStudios.playerrevivefabric.client.network;

import com.TNTStudios.playerrevivefabric.client.ClientReviveState;
import com.TNTStudios.playerrevivefabric.client.gui.ReviveScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class ClientRevivePackets {

    public static final Identifier REVIVE_ATTEMPT = new Identifier("playerrevivefabric", "revive_attempt");
    public static final Identifier SET_DOWNED = new Identifier("playerrevivefabric", "set_downed");

    public static void sendReviveAttempt(PlayerEntity target) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(target.getUuid());
        ClientPlayNetworking.send(REVIVE_ATTEMPT, buf);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SET_DOWNED, (client, handler, buf, responseSender) -> {
            long duration = buf.readLong(); // reviveTimeMs enviado por el server
            client.execute(() -> {
                ClientReviveState.isDowned = true;
                client.setScreen(new ReviveScreen(duration));
            });
        });
    }
}
