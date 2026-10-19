package dev.stas.bouncecraft;

import com.mojang.logging.LogUtils;
import dev.stas.bouncecraft.config.BounceCraftClientConfig;
import dev.stas.bouncecraft.config.BounceCraftServerConfig;
import dev.stas.bouncecraft.gametest.BounceCraftGameTests;
import dev.stas.bouncecraft.network.BounceCraftNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;

@Mod(BounceCraftConstants.MOD_ID)
public class BounceCraft {
    public static final Logger LOGGER = LogUtils.getLogger();

    public BounceCraft(IEventBus modBus, ModContainer container) {
        modBus.addListener(BounceCraftNetwork::register);
        modBus.addListener(BounceCraft::registerGameTests);
        container.registerConfig(ModConfig.Type.SERVER, BounceCraftServerConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            container.registerConfig(ModConfig.Type.CLIENT, BounceCraftClientConfig.SPEC);
        }
        LOGGER.info("BounceCraft {} ready", container.getModInfo().getVersion());
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(BounceCraftGameTests.class);
    }
}
