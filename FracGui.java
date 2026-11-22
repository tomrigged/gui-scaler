package nl.fracgui;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;

public class FracGui implements ClientModInitializer {
    /** GUI-schaal in tienden: 23 = 2.3x */
    public static int tenths = 20;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("fracgui.txt");
    }

    public static void setTenths(int value) {
        tenths = Math.max(10, Math.min(60, value));
        try {
            Files.writeString(configPath(), Integer.toString(tenths));
        } catch (Exception ignored) {
        }
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

        // Elke tick: als vanilla de schaal terugzet naar een heel getal, zetten wij hem weer goed.
        ClientTickEvents.END_CLIENT_TICK.register(FracGui::apply);

        // /guiscale        -> opent het slider-scherm
        // /guiscale 2.3    -> zet direct 2.3x
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("guiscale")
                        .executes(ctx -> {
                            MinecraftClient c = MinecraftClient.getInstance();
                            c.send(() -> c.setScreen(new GuiScaleScreen()));
                            return 1;
                        })
                        .then(ClientCommandManager.argument("factor", DoubleArgumentType.doubleArg(1.0, 6.0))
                                .executes(ctx -> {
                                    double f = DoubleArgumentType.getDouble(ctx, "factor");
                                    setTenths((int) Math.round(f * 10));
                                    ctx.getSource().sendFeedback(Text.literal("GUI-schaal: " + (tenths / 10.0) + "x"));
                                    return 1;
                                }))));
    }

    private static void apply(MinecraftClient client) {
        Window w = client.getWindow();
        if (w == null) return;
        // Houd het scherm minstens 320x240 GUI-pixels, net als vanilla.
        double max = Math.min(w.getFramebufferWidth() / 320.0, w.getFramebufferHeight() / 240.0);
        double target = Math.max(1.0, Math.min(tenths / 10.0, max));
        if (Math.abs(w.getScaleFactor() - target) > 1.0E-6) {
            w.setScaleFactor(target);
            if (client.currentScreen != null) {
                client.currentScreen.resize(client, w.getScaledWidth(), w.getScaledHeight());
            }
        }
    }
}
