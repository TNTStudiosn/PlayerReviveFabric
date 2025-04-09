package com.TNTStudios.playerrevivefabric.data;

import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.network.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class ReviveManager {

    private static final Map<UUID, DownedPlayerData> downedPlayers = new HashMap<>();

    public static class DownedPlayerData {
        public final long downedAt;
        public final PlayerInventory savedInventory;
        public final BlockPos deathPos;

        public DownedPlayerData(long downedAt, PlayerInventory inventory, BlockPos pos) {
            this.downedAt = downedAt;
            this.savedInventory = inventory;
            this.deathPos = pos;
        }
    }

    public static void downPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        downedPlayers.put(uuid, new DownedPlayerData(System.currentTimeMillis(), player.getInventory(), player.getBlockPos()));

        // Enviar paquete para pantalla de revive
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(ReviveConfig.reviveTimeMs);
        ServerPlayNetworking.send(player, ModPackets.SET_DOWNED, buf);

        player.sendMessage(Text.literal("§cHas sido derribado. Espera a ser revivido o acepta tu muerte."), false);
    }



    public static boolean isDowned(ServerPlayerEntity player) {
        return downedPlayers.containsKey(player.getUuid());
    }

    public static void revivePlayer(ServerPlayerEntity player) {
        downedPlayers.remove(player.getUuid());
        player.setHealth(8.0F); // Revive con vida
        player.sendMessage(Text.literal("§a¡Has sido revivido!"), false);

        // Limpiar pantalla en cliente
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, ModPackets.CLEAR_DOWNED, buf);
    }


    public static void forceDeath(ServerPlayerEntity player) {
        downedPlayers.remove(player.getUuid());
        player.setHealth(0); // Morir naturalmente

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, ModPackets.CLEAR_DOWNED, buf);
    }



    public static void tick(ServerWorld world) {
        long now = System.currentTimeMillis();
        List<UUID> toKill = new ArrayList<>();

        for (Map.Entry<UUID, DownedPlayerData> entry : downedPlayers.entrySet()) {
            long elapsed = now - entry.getValue().downedAt;
            if (elapsed >= ReviveConfig.reviveTimeMs) {
                toKill.add(entry.getKey());
            }
        }

        for (UUID uuid : toKill) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
            if (player != null && isDowned(player)) {
                forceDeath(player);
            }
        }
    }
}
