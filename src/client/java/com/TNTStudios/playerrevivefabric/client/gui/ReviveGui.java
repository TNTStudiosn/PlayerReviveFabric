package com.TNTStudios.playerrevivefabric.client.gui;

import com.TNTStudios.playerrevivefabric.client.RevivePacketsClient;
import com.TNTStudios.playerrevivefabric.revive.PlayerReviveData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class ReviveGui extends Screen {

    private int remainingTicks;
    private TextFieldWidget chatField; // Campo de texto para el chat

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

        // Botón de aceptar muerte
        ButtonWidget button = ButtonWidget.builder(
                Text.translatable("gui.revive.accept_death"),
                btn -> {
                    if (this.client != null && this.client.player != null) {
                        UUID uuid = this.client.player.getUuid();
                        PlayerReviveData.setDowned(uuid, false);
                        PlayerReviveData.markDeathAccepted(uuid);
                        this.client.setScreen(null);
                        RevivePacketsClient.sendAcceptDeath();
                    }
                }
        ).dimensions(centerX - 75, centerY + 20, 150, 20).build();

        this.addDrawableChild(button);

        // Agregar campo de texto para el chat
        this.chatField = new TextFieldWidget(this.textRenderer, centerX - 110, centerY + 70, 220, 20, Text.literal("Chat"));
        this.chatField.setMaxLength(256); // Límite de caracteres
        this.addDrawableChild(this.chatField); // Añadir al renderizado
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Título de la GUI
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY - 50, 0xFFFFFF);

        // Mensaje de estado
        Text text;
        if (PlayerReviveData.isBeingRevived(this.client.player.getUuid())) {
            text = Text.literal("Estás siendo levantado").formatted(Formatting.GREEN);
        } else {
            text = Text.translatable("gui.revive.message", String.valueOf(getSecondsRemaining()))
                    .formatted(Formatting.RED);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, text, centerX, centerY - 20, 0xFFFFFF);

        // Texto indicativo encima del campo de texto (opcional)
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Escribe aquí para hablar"), centerX, centerY + 50, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta); // Renderiza los elementos hijos, incluido el chatField
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Detectar Enter (código 257) cuando el campo de texto está enfocado
        if (keyCode == 257 && this.chatField.isFocused()) {
            String message = this.chatField.getText().trim();
            if (!message.isEmpty()) {
                if (this.client != null && this.client.player != null) {
                    // Enviar el mensaje al chat
                    this.client.player.networkHandler.sendChatMessage(message);
                }
                this.chatField.setText(""); // Limpiar el campo después de enviar
            }
            return true; // Indicar que el evento fue manejado
        }
        // Evitar cerrar con Esc (comportamiento original)
        if (keyCode == 256) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}