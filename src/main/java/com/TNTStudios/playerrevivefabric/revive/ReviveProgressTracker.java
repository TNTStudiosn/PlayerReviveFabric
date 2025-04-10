package com.TNTStudios.playerrevivefabric.revive;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.util.ServerUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class ReviveProgressTracker {
    private final UUID downed;
    private final UUID reviver;
    private int ticksLeft;
    private static final double MAX_DISTANCE_SQUARED = 5.0 * 5.0;

    public ReviveProgressTracker(UUID downed, ServerPlayerEntity reviverPlayer, int duration) {
        this.downed = downed;
        this.reviver = reviverPlayer.getUuid();
        this.ticksLeft = duration;
    }

    public boolean tick() {
        ServerPlayerEntity downedPlayer = ServerUtil.getPlayer(downed);
        ServerPlayerEntity reviverPlayer = ServerUtil.getPlayer(reviver);

        if (downedPlayer == null || reviverPlayer == null || reviverPlayer.isDead()) {
            cancel("interrupción por desconexión o muerte");
            return true;
        }

        double distance = downedPlayer.squaredDistanceTo(reviverPlayer);
        if (distance > MAX_DISTANCE_SQUARED) {
            cancel("te alejaste demasiado");
            return true;
        }

        if (--ticksLeft <= 0) {
            finish(downedPlayer, reviverPlayer);
            return true;
        }

        RevivePackets.sendProgress(reviverPlayer, downed, ticksLeft);
        return false;
    }

    private void finish(ServerPlayerEntity downedPlayer, ServerPlayerEntity reviverPlayer) {
        PlayerReviveData.setDowned(downed, false);
        PlayerReviveData.clear(downed);
        PlayerReviveData.clearReviving(downed);

        downedPlayer.setHealth(downedPlayer.getMaxHealth());
        RevivePackets.sendTimerUpdate(downedPlayer, 0);
        PlayerReviveNetwork.sendDownedState(downedPlayer, false);
        RevivePackets.sendSuccess(downedPlayer);
        RevivePackets.sendSuccess(reviverPlayer);

        reduceFood(downedPlayer);
        reduceFood(reviverPlayer);
    }

    private void cancel(String reason) {
        ServerPlayerEntity reviverPlayer = ServerUtil.getPlayer(reviver);
        if (reviverPlayer != null) {
            reviverPlayer.sendMessage(Text.literal("Revive cancelado: " + reason).formatted(Formatting.RED), false);
            RevivePackets.sendCancelled(reviverPlayer, downed);
        }
        PlayerReviveData.clearReviving(downed);
    }

    public UUID getReviver() {
        return reviver;
    }

    private void reduceFood(ServerPlayerEntity player) {
        int current = player.getHungerManager().getFoodLevel();
        player.getHungerManager().setFoodLevel(Math.max(current / 2, 1));
    }
}

