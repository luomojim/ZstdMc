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
            .defineInRange("compression_level", 7, 1, 15);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static volatile int level = COMPRESSION_LEVEL.get();

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            level = COMPRESSION_LEVEL.get();
        }
    }

    public static int getLevel() {
        return Math.max(1, Math.min(15, level));
    }
}
