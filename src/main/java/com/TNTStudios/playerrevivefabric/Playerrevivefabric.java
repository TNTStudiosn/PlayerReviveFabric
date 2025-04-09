package com.TNTStudios.playerrevivefabric;

import com.TNTStudios.playerrevivefabric.commands.AcceptDeathCommand;
import com.TNTStudios.playerrevivefabric.config.ReviveConfig;
import com.TNTStudios.playerrevivefabric.events.DeathEventHandler;
import com.TNTStudios.playerrevivefabric.network.RevivePackets;
import com.TNTStudios.playerrevivefabric.server.PlayerReviveHandler;
import com.TNTStudios.playerrevivefabric.data.ReviveManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class Playerrevivefabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReviveConfig.load();

        DeathEventHandler.init(); // Se inicializa el manejo de eventos de muerte
        RevivePackets.registerC2SPackets();

        // Manejo de tick para procesar los tiempos de revivir o forzar respawn
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                DeathEventHandler.handleRespawnTimeouts(world);
            }
            PlayerReviveHandler.tick(server);
        });

        // Aquí quitamos la llamada a `onPlayerDamaged` y se realiza la lógica de daño en el evento correspondiente
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Si el jugador está derribado, impedir el daño
                if (ReviveManager.isDowned(player)) {
                    return false;  // Cancelamos el daño si el jugador está derribado (retornamos 'false')
                }
            }
            return true; // Permite daño por defecto (retornamos 'true')
        });

        // Registro de comandos
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AcceptDeathCommand.register(dispatcher);
        });
    }
}
