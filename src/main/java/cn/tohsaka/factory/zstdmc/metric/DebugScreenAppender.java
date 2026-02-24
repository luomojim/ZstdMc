package cn.tohsaka.factory.zstdmc.metric;

import cn.tohsaka.factory.zstdmc.Zstdmc;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Zstdmc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DebugScreenAppender {
    private DebugScreenAppender() {
    }

    @SubscribeEvent
    public static void onGatherDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        List<String> rightText = event.getRight();
        rightText.add("");
        rightText.add(ChatFormatting.AQUA + Component.translatable("zstdmc.debug.title").getString());
        long rxOriginal = ZstdMetric.getRxbytes();
        long rxCompressed = ZstdMetric.getRxbytes2();
        long txOriginal = ZstdMetric.getTxbytes();
        long txCompressed = ZstdMetric.getTxbytes2();
        String rxFormatted = ChatFormatting.GRAY + "RX(orig/comp): " + ChatFormatting.WHITE
                + formatBytes(rxOriginal) + "/" + formatBytes(rxCompressed)
                + ChatFormatting.DARK_GRAY + " (" + formatReduction(rxOriginal, rxCompressed) + ")";
        String txFormatted = ChatFormatting.GRAY + "TX(orig/comp): " + ChatFormatting.WHITE
                + formatBytes(txOriginal) + "/" + formatBytes(txCompressed)
                + ChatFormatting.DARK_GRAY + " (" + formatReduction(txOriginal, txCompressed) + ")";
        rightText.add(rxFormatted);
        rightText.add(txFormatted);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onJoinServerEvent(PlayerEvent.PlayerLoggedInEvent loggedInEvent) {
        if (Minecraft.getInstance().player != null) {
            ZstdMetric.reset();
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static String formatReduction(long original, long compressed) {
        if (original <= 0) {
            return "n/a";
        }
        double ratio = 1.0 - ((double) compressed / (double) original);
        return String.format("saved %.1f%%", ratio * 100.0);
    }
}
