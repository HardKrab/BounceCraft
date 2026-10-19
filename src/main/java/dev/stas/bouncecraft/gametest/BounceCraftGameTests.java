package dev.stas.bouncecraft.gametest;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.config.BounceCraftServerConfig;
import dev.stas.bouncecraft.logic.SessionRules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BounceCraftConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BounceCraftGameTests {
    private BounceCraftGameTests() {
    }

    @GameTest(template = "empty3")
    public static void sessionRulesAndConfig(GameTestHelper helper) {
        helper.assertTrue(SessionRules.inRange(0, 0, 0, 24), "origin should be in range");
        helper.assertTrue(SessionRules.canStartDj(false, 0, 8), "first DJ should be allowed");
        helper.assertTrue(!SessionRules.canStartDj(false, 8, 8), "ninth DJ should be rejected");
        helper.assertTrue(BounceCraftServerConfig.RADIUS.getAsInt() == 24, "default radius should be 24");
        helper.succeed();
    }
}
