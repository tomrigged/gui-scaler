package nl.fracgui.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import nl.fracgui.FracGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    // Vervangt de hele-getallen-schaal die vanilla uitrekent door onze eigen waarde.
    @ModifyArg(
            method = "onResolutionChanged",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setScaleFactor(D)V"),
            index = 0)
    private double fracgui$customScale(double vanilla) {
        Window w = ((MinecraftClient) (Object) this).getWindow();
        double wanted = FracGui.tenths / 10.0;
        // Zorg dat het scherm minstens 320x240 "GUI-pixels" blijft, net als vanilla.
        double max = Math.min(w.getFramebufferWidth() / 320.0, w.getFramebufferHeight() / 240.0);
        return Math.max(1.0, Math.min(wanted, max));
    }
}
