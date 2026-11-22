package nl.fracgui.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import nl.fracgui.FracGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
public abstract class VideoOptionsScreenMixin extends GameOptionsScreen {

    protected VideoOptionsScreenMixin(Screen parent, GameOptions options, Text title) {
        super(parent, options, title);
    }

    // Voegt de precieze slider onderaan de Video Settings toe.
    @Inject(method = "addOptions", at = @At("TAIL"))
    private void fracgui$addSlider(CallbackInfo ci) {
        this.body.addAll(new SimpleOption<?>[]{FracGui.option()});
    }
}
