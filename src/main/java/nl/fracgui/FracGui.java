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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FracGui implements ClientModInitializer {
    public static int tenths = 20;

    private static boolean probed = false;
    private static Field fieldScaledW;
    private static Field fieldScaledH;

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

        ClientTickEvents.END_CLIENT_TICK.register(FracGui::apply);

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

    private static List<Field> intFields() {
        List<Field> list = new ArrayList<>();
        for (Field f : Window.class.getDeclaredFields()) {
            if (f.getType() == int.class && !Modifier.isStatic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    list.add(f);
                } catch (Exception ignored) {
                }
            }
        }
        return list;
    }

    private static int[] snapshot(List<Field> fields, Window w) throws Exception {
        int[] out = new int[fields.size()];
        for (int i = 0; i < out.length; i++) out[i] = fields.get(i).getInt(w);
        return out;
    }

    // Zoekt de velden voor geschaalde breedte/hoogte zonder hun namen te kennen:
    // we zetten de schaal tijdelijk op 1 en 2 en kijken welke velden meebewegen.
    private static void probe(Window w) {
        probed = true;
        try {
            int orig = w.getScaleFactor();
