package com.TNTStudios.playerrevivefabric.client;

import com.TNTStudios.playerrevivefabric.network.PlayerReviveNetwork;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import java.util.UUID;

public class PlayerrevivefabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Registrar receptor para el paquete "set_downed"
        ClientPlayNetworking.registerGlobalReceiver(PlayerReviveNetwork.SET_DOWNED_PACKET, (client, handler, buf, responseSender) -> {
            boolean downed = buf.readBoolean();

            // Se debe actualizar la variable de estado en el hilo principal (client thread)
            client.execute(() -> {
                // Obtenemos el UUID del jugador local
                UUID playerUuid = client.player.getUuid();

                // Actualizamos el estado "downed" en la copia del cliente
                PlayerReviveData.setDowned(playerUuid, downed);
            });
        });
    }
}
