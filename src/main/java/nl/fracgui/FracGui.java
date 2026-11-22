package nl.fracgui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;

public class FracGui implements ClientModInitializer {
    /** GUI-schaal in tienden: 23 = 2.3x */
    public static int tenths = 20;

    private static SimpleOption<Integer> option;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("fracgui.txt");
    }

    @Override
    public void onInitializeClient() {
        try {
            Path p = configPath();
            if (Files.exists(p)) {
                tenths = Math.max(10, Math.min(60, Integer.parseInt(Files.readString(p).trim())));
            }
        } catch (Exception ignored) {
        }
    }

    private static void save() {
        try {
            Files.writeString(configPath(), Integer.toString(tenths));
        } catch (Exception ignored) {
        }
    }

    public static SimpleOption<Integer> option() {
        if (option == null) {
            option = new SimpleOption<>(
                    "fracgui.option",
                    SimpleOption.emptyTooltip(),
                    (label, value) -> Text.literal("GUI Scale (precies): " + (value / 10.0) + "x"),
                    new SimpleOption.ValidatingIntSliderCallbacks(10, 60),
                    tenths,
                    value -> {
                        tenths = value;
                        save();
                        MinecraftClient.getInstance().onResolutionChanged();
                    });
        }
        return option;
    }
}
