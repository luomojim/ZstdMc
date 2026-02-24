package cn.tohsaka.factory.zstdmc;

import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.tohsaka.factory.zstdmc.metric.DebugScreenAppender.formatBytes;

@Mod.EventBusSubscriber(modid = Zstdmc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {
    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("zstd")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(ModCommands::runStatus))
                .then(Commands.literal("reset").executes(ModCommands::runReset))
                .then(Commands.literal("top10").executes(ModCommands::listTop10)));
    }

    private static int listTop10(CommandContext<CommandSourceStack> ctx) {
        var playerList = ctx.getSource().getServer().getPlayerList();
        var map = ZstdMetric.getMap();
        if (map.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("zstdmc.command.top.empty"), false);
            return 1;
        }
        var list = map.keySet().stream()
                .sorted(Comparator.comparingLong((UUID uuid) -> {
                    var entry = map.get(uuid);
                    return entry != null ? entry.getTxbytes2() : 0L;
                }).reversed())
                .limit(10)
                .collect(Collectors.toUnmodifiableList());

        int i = 0;
        List<String> msg = new ArrayList<>();
        for (UUID uuid : list) {
            i++;
            var entry = map.get(uuid);
            if (entry == null) {
                continue;
            }
            String rxFormatted = formatBytes(entry.getRxbytes2()) + "/" + formatBytes(entry.getRxbytes());
            String txFormatted = formatBytes(entry.getTxbytes()) + "/" + formatBytes(entry.getTxbytes2());
            var player = playerList.getPlayer(uuid);
            String playerName = player != null ? player.getName().getString() : uuid.toString();
            msg.add(String.format("[%d][%s] TX:%s | RX:%s", i, playerName, txFormatted, rxFormatted));
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n", msg)), true);
        return 1;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        String rxFormatted = ChatFormatting.GRAY + "RX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getRxbytes2()) + "/" + formatBytes(ZstdMetric.getRxbytes());
        String txFormatted = ChatFormatting.GRAY + "TX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getTxbytes()) + "/" + formatBytes(ZstdMetric.getTxbytes2());
        context.getSource().sendSuccess(() -> Component.literal(rxFormatted + "\n" + txFormatted), true);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        ZstdMetric.reset();
        context.getSource().sendSuccess(() -> Component.translatable("zstdmc.command.reset.success"), true);
        return 1;
    }
}
