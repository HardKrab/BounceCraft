package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraftConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = BounceCraftConstants.MOD_ID, dist = Dist.CLIENT)
public class BounceCraftClient {
    public BounceCraftClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(ClientKeys::register);
        ClientNetwork.install();
        NeoForge.EVENT_BUS.register(RenderPulse.class);
        NeoForge.EVENT_BUS.register(BounceHud.class);
        FilePicker.musicFolder();
    }
}
