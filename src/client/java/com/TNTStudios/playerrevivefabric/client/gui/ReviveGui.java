package com.TNTStudios.playerrevivefabric.client.gui;

import com.TNTStudios.playerrevivefabric.client.RevivePacketsClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReviveGui extends Screen {

    private static int remainingTicks = 0;
    private final int totalTicks;

    public ReviveGui(int totalTicks) {
        super(Text.translatable("gui.revive.title"));
        this.totalTicks = totalTicks;
    }

    public static void setRemainingTicks(int ticks) {
        remainingTicks = ticks;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ButtonWidget button = ButtonWidget.builder(
                Text.translatable("gui.revive.accept_death"),
                btn -> {
                    RevivePacketsClient.sendAcceptDeath();
                    this.close();
                }
        ).dimensions(centerX - 75, centerY + 20, 150, 20).build();

        this.addDrawableChild(button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        String seconds = String.valueOf(remainingTicks / 20); // ticks → s
        Text text = Text.translatable("gui.revive.message", seconds).formatted(Formatting.RED);

        drawCenteredText(matrices, this.textRenderer, text, centerX, centerY - 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
