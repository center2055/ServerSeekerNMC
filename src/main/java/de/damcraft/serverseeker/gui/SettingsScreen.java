package de.damcraft.serverseeker.gui;

import de.damcraft.serverseeker.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
    private final MultiplayerScreen parent;
    private final Screen previous;
    private TextFieldWidget baseUrlField;
    private TextFieldWidget apiKeyField;

    public SettingsScreen(MultiplayerScreen parent, Screen previous) {
        super(Text.literal("ServerSeeker Settings"));
        this.parent = parent;
        this.previous = previous;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int y = 40;

        baseUrlField = new TextFieldWidget(this.textRenderer, 10, y, 360, 20, Text.of("Base URL"));
        baseUrlField.setText(Config.getBaseUrl());
        this.addSelectableChild(baseUrlField);
        y += 30;

        apiKeyField = new TextFieldWidget(this.textRenderer, 10, y, 360, 20, Text.of("API Key"));
        apiKeyField.setText(Config.getApiKey());
        this.addSelectableChild(apiKeyField);
        y += 30;

        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Save"), b -> save()).dimensions(10, y, 70, 20).build());
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Back"), b -> close()).dimensions(85, y, 70, 20).build());
    }

    private void save() {
        Config.setBaseUrl(baseUrlField.getText().trim());
        Config.setApiKey(apiKeyField.getText().trim());
        Config.save();
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(this.textRenderer, "Base URL", 10, 30, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "API Key", 10, 60, 0xAAAAAA);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.previous != null ? this.previous : this.parent);
    }
}



