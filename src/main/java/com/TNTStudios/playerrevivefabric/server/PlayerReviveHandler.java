package com.TNTStudios.playerrevivefabric.server;

import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

import java.util.*;

public class PlayerReviveHandler {

    private static final Map<UUID, RevivalSession> activeRevives = new HashMap<>();

    public static class RevivalSession {
        public final UUID targetUuid;
        public final long startTime;
        public final Vec3d startPos;

        public RevivalSession(UUID targetUuid, Vec3d startPos) {
            this.targetUuid = targetUuid;
            this.startTime = System.currentTimeMillis();
            this.startPos = startPos;
        }

        public boolean isComplete() {
            return System.currentTimeMillis() - startTime >= 10_000; // 10 segundos
        }
    }

    public static ActionResult onRightClick(ServerPlayerEntity reviver, ServerPlayerEntity target) {
        if (!ReviveManager.isDowned(target)) return ActionResult.PASS;
        if (activeRevives.containsKey(reviver.getUuid())) return ActionResult.PASS;

        activeRevives.put(reviver.getUuid(), new RevivalSession(target.getUuid(), reviver.getPos()));
        reviver.sendMessage(Text.literal("§eReviviendo a " + target.getName().getString() + "..."), true);
        return ActionResult.SUCCESS;
    }

    public static void tick(MinecraftServer server) {
        List<UUID> completed = new ArrayList<>();
        List<UUID> canceled = new ArrayList<>();

        for (Map.Entry<UUID, RevivalSession> entry : activeRevives.entrySet()) {
            ServerPlayerEntity reviver = server.getPlayerManager().getPlayer(entry.getKey());
            RevivalSession session = entry.getValue();
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(session.targetUuid);

            if (reviver == null || target == null || !ReviveManager.isDowned(target)) {
                canceled.add(entry.getKey());
                continue;
            }

            if (reviver.getPos().distanceTo(session.startPos) > 2.5) {
                reviver.sendMessage(Text.literal("§cRevival cancelado, te moviste."), false);
                canceled.add(entry.getKey());
                continue;
            }

            if (session.isComplete()) {
                ReviveManager.revivePlayer(target);
                reviver.sendMessage(Text.literal("§aHas revivido a " + target.getName().getString() + "!"), false);
                target.sendMessage(Text.literal("§aFuiste revivido por " + reviver.getName().getString() + "!"), false);
                drainFood(reviver);
                drainFood(target);
                completed.add(entry.getKey());
            }
        }

        completed.forEach(activeRevives::remove);
        canceled.forEach(activeRevives::remove);
    }

    private static void drainFood(ServerPlayerEntity player) {
        int current = player.getHungerManager().getFoodLevel();
        player.getHungerManager().setFoodLevel(Math.max(1, current / 2));
    }
}
