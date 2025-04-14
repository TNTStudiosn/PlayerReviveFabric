package com.TNTStudios.playerrevivefabric.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ServerUtil {
    private static MinecraftServer server;

    public static void setServer(MinecraftServer srv) {
        server = srv;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return server != null ? server.getPlayerManager().getPlayer(uuid) : null;
    }
}
