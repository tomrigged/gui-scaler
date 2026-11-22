package nl.fracgui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class GuiScaleScreen extends Screen {
    private int pending = FracGui.tenths;

    public GuiScaleScreen() {
        super(Text.literal("GUI Scale"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addDrawableChild(new SliderWidget(cx - 100, cy - 30, 200, 20, Text.empty(), (pending - 10) / 50.0) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                int t = 10 + (int) Math.round(this.value * 50);
                setMessage(Text.literal("GUI Scale: " + (t / 10.0) + "x"));
            }

            @Override
            protected void applyValue() {
                pending = 10 + (int) Math.round(this.value * 50);
            }
        });

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Toepassen"), b -> {
            MinecraftClient c = MinecraftClient.getInstance();
            FracGui.setTenths(pending);
            FracGui.apply(c);
            c.setScreen(new GuiScaleScreen());
        }).dimensions(cx - 100, cy, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Klaar"), b -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(cx - 100, cy + 24, 200, 20).build());
    }
}
