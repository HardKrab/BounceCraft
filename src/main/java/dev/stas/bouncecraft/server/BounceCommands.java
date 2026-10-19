package dev.stas.bouncecraft.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.stas.bouncecraft.config.BounceCraftServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class BounceCommands {
    private BounceCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bounce")
                .then(Commands.literal("stop").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    SessionManager.get(player.server).handleStopRequest(player);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.bouncecraft.stop"), false);
                    return 1;
                }))
                .then(Commands.literal("bpm")
                        .then(Commands.argument("bpm", IntegerArgumentType.integer(70, 180)).executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int bpm = IntegerArgumentType.getInteger(ctx, "bpm");
                            boolean ok = SessionManager.get(player.server).setBpm(player, bpm);
                            if (!ok) {
                                ctx.getSource().sendFailure(Component.translatable("command.bouncecraft.bpm.none"));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.bouncecraft.bpm", bpm), false);
                            return 1;
                        })))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.translatable(
                            "command.bouncecraft.reload",
                            BounceCraftServerConfig.RADIUS.getAsInt(),
                            BounceCraftServerConfig.MAX_TRACK_BYTES.getAsInt(),
                            BounceCraftServerConfig.MAX_DURATION_SEC.getAsInt(),
                            BounceCraftServerConfig.UPLOAD_COOLDOWN_SEC.getAsInt(),
                            BounceCraftServerConfig.MAX_CONCURRENT_DJS.getAsInt()
                    ), true);
                    return 1;
                }))
        );
    }
}
