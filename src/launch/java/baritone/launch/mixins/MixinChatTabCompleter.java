package baritone.launch.mixins;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.TabCompleteEvent;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(GuiChat.class)
public abstract class MixinChatTabCompleter {

    @Shadow
    protected GuiTextField inputField;

    @Shadow
    private boolean playerNamesFound;

    @Shadow
    private List<String> foundPlayerNames;

    @Inject(
            method = "autocompletePlayerNames",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onComplete(CallbackInfo ci) {
        String prefix = this.inputField.getText().substring(0, this.inputField.getCursorPosition());

        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

        TabCompleteEvent event = new TabCompleteEvent(prefix);
        baritone.getGameEventHandler().onPreTabComplete(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (event.completions != null) {
            try {
                this.playerNamesFound = true;
                foundPlayerNames.addAll(Arrays.asList(event.completions));
                if (foundPlayerNames.size() == 0) {
                    this.playerNamesFound = false;
                }
            } catch (Exception e) {
                this.playerNamesFound = false;
                ci.cancel();
            }
        }
    }
}
