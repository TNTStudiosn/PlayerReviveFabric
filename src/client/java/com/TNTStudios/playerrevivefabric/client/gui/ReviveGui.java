package com.TNTStudios.playerrevivefabric.client.gui;

import com.TNTStudios.playerrevivefabric.client.RevivePacketsClient;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReviveGui extends Screen {

    private int remainingTicks;

    public ReviveGui(int initialTicks) {
        super(Text.translatable("gui.revive.title"));
        this.remainingTicks = initialTicks;
    }

    public void updateTicks(int ticks) {
        this.remainingTicks = ticks;
    }

    private int getSecondsRemaining() {
        return Math.max(remainingTicks / 20, 0);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ButtonWidget button = ButtonWidget.builder(
                Text.translatable("gui.revive.accept_death"),
                btn -> {
                    if (this.client != null && this.client.player != null) {
                        PlayerReviveData.setDowned(this.client.player.getUuid(), false);
                        this.client.setScreen(null); // Cierra la pantalla
                        RevivePacketsClient.sendAcceptDeath();
                    }
                }
        ).dimensions(centerX - 75, centerY + 20, 150, 20).build();

        this.addDrawableChild(button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // ✅ Dibuja el título en la parte superior
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY - 50, 0xFFFFFF);

        // ✅ Dibuja el mensaje con el contador
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
