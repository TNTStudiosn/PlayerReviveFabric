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

        PlayerInventory inventoryCopy = new PlayerInventory(null);
        for (int i = 0; i < player.getInventory().size(); i++) {
            inventoryCopy.setStack(i, player.getInventory().getStack(i).copy());
        }

        downedPlayers.put(uuid, new DownedPlayerData(System.currentTimeMillis(), inventoryCopy, player.getBlockPos()));

        player.setHealth(1.0F);
        player.changeGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();

        // ✅ Crear y enviar el paquete
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(ReviveConfig.reviveTimeMs);
        ServerPlayNetworking.send(player, ModPackets.SET_DOWNED, buf);

        player.sendMessage(Text.literal("§cHas sido derribado. Espera a ser revivido o acepta tu muerte."), false);
    }


    public static boolean isDowned(ServerPlayerEntity player) {
        return downedPlayers.containsKey(player.getUuid());
    }

    public static void revivePlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        DownedPlayerData data = downedPlayers.get(uuid);
        if (data == null) return;

        // Restaurar inventario
        for (int i = 0; i < data.savedInventory.size(); i++) {
            player.getInventory().setStack(i, data.savedInventory.getStack(i).copy());
        }

        player.changeGameMode(GameMode.SURVIVAL);
        player.setHealth(8.0F); // Revivido con 4 corazones

        downedPlayers.remove(uuid);
        player.sendMessage(Text.literal("§a¡Has sido revivido!"), false);
    }

    public static void forceDeath(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        downedPlayers.remove(uuid);

        player.changeGameMode(GameMode.SURVIVAL);
        player.setHealth(0); // Esto sí aplicará muerte real
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
