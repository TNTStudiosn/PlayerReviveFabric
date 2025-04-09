package com.TNTStudios.playerrevivefabric.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.TNTStudios.playerrevivefabric.client.ClientReviveState;


public class ReviveScreen extends Screen {

    private long expireTime;
    private final long duration;

    public ReviveScreen(long durationMs) {
        super(Text.literal("Has muerto"));
        this.duration = durationMs;
        this.expireTime = System.currentTimeMillis() + duration;
    }

    @Override
    protected void init() {
        ClientReviveState.isDowned = true;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Aceptar muerte").formatted(Formatting.RED),
                btn -> forceDeath()
        ).dimensions(centerX - 60, centerY + 40, 120, 20).build());
    }

    private void forceDeath() {
        MinecraftClient.getInstance().player.networkHandler.sendChatCommand("acceptdeath"); // Lo implementaremos como comando
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() > expireTime) {
            forceDeath();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        long timeLeft = expireTime - System.currentTimeMillis();
        long seconds = Math.max(0, timeLeft / 1000);
        long minutes = seconds / 60;
        seconds %= 60;

        context.drawCenteredTextWithShadow(this.textRenderer, "§c* Has muerto *", centerX, centerY - 40, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "§eTe quedan §6[" + minutes + "m " + seconds + "s]§e para que alguien te reviva",
                centerX, centerY - 15, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ClientReviveState.isDowned = false;
        super.removed();
    }

}
