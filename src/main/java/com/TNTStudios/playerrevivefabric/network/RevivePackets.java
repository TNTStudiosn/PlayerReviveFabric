package com.TNTStudios.playerrevivefabric.network;

import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RevivePackets {

    public static final Identifier REVIVE_TIMER_SYNC = new Identifier("playerrevivefabric", "revive_timer_sync");
    public static final Identifier ACCEPT_DEATH = new Identifier("playerrevivefabric", "accept_death");

    // Servidor: registro del receptor
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ACCEPT_DEATH, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                ReviveTimerManager.forceDeath(player);
            });
        });
    }

    // Servidor → Cliente
    public static void sendTimerUpdate(ServerPlayerEntity player, int ticks) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        buf.writeInt(ticks);

        ServerPlayNetworking.send(player, REVIVE_TIMER_SYNC, buf);
    }
}
