package com.TNTStudios.playerrevivefabric.client.network;

import com.TNTStudios.playerrevivefabric.client.PlayerReviveClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class PlayerReviveClientNetwork {
    public static final Identifier SET_DOWNED_PACKET = new Identifier("playerrevivefabric", "set_downed");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            boolean isDowned = buf.readBoolean();
            client.execute(() -> PlayerReviveClientState.setDowned(isDowned));
        });
    }
}
