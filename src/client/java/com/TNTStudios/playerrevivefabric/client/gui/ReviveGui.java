package com.TNTStudios.playerrevivefabric.client.gui;

import com.TNTStudios.playerrevivefabric.client.RevivePacketsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReviveGui extends Screen {

    private static int remainingTicks = 0;

    public ReviveGui() {
        super(Text.translatable("gui.revive.title"));
    }

    public static void setRemainingTicks(int ticks) {
        remainingTicks = ticks;
    }

    private int getSecondsRemaining() {
        return Math.max(remainingTicks / 20, 0); // evita negativos
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Text text = Text.translatable("gui.revive.message", String.valueOf(getSecondsRemaining()))
                .formatted(Formatting.RED);

        context.drawCenteredTextWithShadow(this.textRenderer, text, centerX, centerY - 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
