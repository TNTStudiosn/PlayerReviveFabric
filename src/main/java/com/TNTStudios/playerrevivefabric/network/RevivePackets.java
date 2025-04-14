package com.TNTStudios.playerrevivefabric.network;

import com.TNTStudios.playerrevivefabric.revive.ReviveInteractionManager;
import com.TNTStudios.playerrevivefabric.revive.ReviveTimerManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class RevivePackets {

    public static final Identifier REVIVE_TIMER_SYNC = new Identifier("playerrevivefabric", "revive_timer_sync");
    public static final Identifier ACCEPT_DEATH = new Identifier("playerrevivefabric", "accept_death");
    public static final Identifier START_REVIVE = new Identifier("playerrevivefabric", "start_revive");
    public static final Identifier CANCEL_REVIVE = new Identifier("playerrevivefabric", "cancel_revive");
    public static final Identifier REVIVE_SUCCESS = new Identifier("playerrevivefabric", "revive_success");
    public static final Identifier REVIVE_PROGRESS = new Identifier("playerrevivefabric", "revive_progress");
    public static final Identifier REVIVE_CANCELLED = new Identifier("playerrevivefabric", "revive_cancelled");
    public static final Identifier SET_BEING_REVIVED = new Identifier("playerrevivefabric", "set_being_revived");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ACCEPT_DEATH, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                ReviveTimerManager.forceDeath(player);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(START_REVIVE, (server, player, handler, buf, responseSender) -> {
            UUID target = buf.readUuid();
            server.execute(() -> {
                ReviveInteractionManager.handleStartRevive(server, player, target);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CANCEL_REVIVE, (server, player, handler, buf, responseSender) -> {
            UUID target = buf.readUuid();
            server.execute(() -> {
                ReviveInteractionManager.handleCancelRevive(server, player, target);
            });
        });

    }

    public static void sendProgress(ServerPlayerEntity player, UUID downedUuid, int ticksLeft) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(downedUuid);
        buf.writeInt(ticksLeft);
        ServerPlayNetworking.send(player, REVIVE_PROGRESS, buf);
    }

    public static void sendSuccess(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        ServerPlayNetworking.send(player, REVIVE_SUCCESS, buf);
    }

    public static void sendTimerUpdate(ServerPlayerEntity player, int ticks) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        buf.writeInt(ticks);

        ServerPlayNetworking.send(player, REVIVE_TIMER_SYNC, buf);
    }

    public static void sendBeingRevivedUpdate(ServerPlayerEntity recipient, UUID downedUuid, UUID reviverUuid) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(downedUuid);
        if (reviverUuid != null) {
            buf.writeBoolean(true);
            buf.writeUuid(reviverUuid);
        } else {
            buf.writeBoolean(false);
        }
        ServerPlayNetworking.send(recipient, SET_BEING_REVIVED, buf);
    }

    public static void sendCancelled(ServerPlayerEntity player, UUID downedUuid) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeUuid(downedUuid);
        ServerPlayNetworking.send(player, REVIVE_CANCELLED, buf);
    }

}
