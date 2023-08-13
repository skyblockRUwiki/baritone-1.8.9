package baritone.launch.mixins;

import baritone.utils.accessor.IGuiScreen;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.URI;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen implements IGuiScreen {

    @Override
    @Invoker("openWebLink")
    public abstract void openLink(URI url);
}
