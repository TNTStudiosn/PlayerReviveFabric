package com.TNTStudios.playerrevivefabric.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.UUID;

public class ReviveProgressHud {

    private static UUID currentTarget = null;
    private static int maxTicks = 100;
    private static int currentTicks = 0;

    public static void updateProgress(UUID target, int ticksLeft) {
        if (!target.equals(currentTarget)) {
            currentTarget = target;
            maxTicks = ticksLeft;
        }
        currentTicks = ticksLeft;
    }

    public static void clear() {
        currentTarget = null;
        currentTicks = 0;
        maxTicks = 100;
    }

    public static void render(DrawContext context) {
        if (currentTarget == null || currentTicks <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        String name = client.getNetworkHandler().getPlayerListEntry(currentTarget) != null
                ? client.getNetworkHandler().getPlayerListEntry(currentTarget).getProfile().getName()
                : "Jugador";

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        int barWidth = 150;
        int barHeight = 12;
        int filled = (int) ((1 - (currentTicks / (float) maxTicks)) * barWidth);

        int x = (width - barWidth) / 2;
        int y = height / 2 + 60;

        context.fill(x, y, x + barWidth, y + barHeight, 0xAA000000);
        context.fill(x, y, x + filled, y + barHeight, 0xAA00FF00);

        Text text = Text.literal("Levantando a " + name);
        context.drawCenteredTextWithShadow(client.textRenderer, text, width / 2, y - 12, 0x00FF00);
    }
}
