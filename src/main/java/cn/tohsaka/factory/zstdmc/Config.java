package cn.tohsaka.factory.zstdmc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Zstdmc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue COMPRESSION_LEVEL = BUILDER
            .comment("Zstd compression level. Higher is better ratio with more CPU usage.")
            .defineInRange("compression_level", 10, 1, 15);

    private static final ForgeConfigSpec.BooleanValue AGGRESSIVE_THRESHOLD = BUILDER
            .comment("Lower vanilla network compression threshold to compress more packets.")
            .define("aggressive_threshold", true);

    private static final ForgeConfigSpec.IntValue MIN_NETWORK_THRESHOLD = BUILDER
            .comment("Target minimum threshold when aggressive_threshold=true. Effective threshold=min(vanilla, this).")
            .defineInRange("min_network_threshold", 256, 0, 1024);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static volatile int level = COMPRESSION_LEVEL.get();
    private static volatile boolean aggressiveThreshold = AGGRESSIVE_THRESHOLD.get();
    private static volatile int minNetworkThreshold = MIN_NETWORK_THRESHOLD.get();

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            level = COMPRESSION_LEVEL.get();
            aggressiveThreshold = AGGRESSIVE_THRESHOLD.get();
            minNetworkThreshold = MIN_NETWORK_THRESHOLD.get();
        }
    }

    public static int getLevel() {
        return Math.max(1, Math.min(15, level));
    }

    public static int getEffectiveThreshold(int vanillaThreshold) {
        if (vanillaThreshold < 0) {
            return vanillaThreshold;
        }

        if (!aggressiveThreshold) {
            return Math.max(0, vanillaThreshold);
        }

        int target = Math.max(0, minNetworkThreshold);
        return Math.max(0, Math.min(vanillaThreshold, target));
    }
}
