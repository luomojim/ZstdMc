package cn.tohsaka.factory.zstdmc;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Zstdmc.MODID)
public class Zstdmc {
    public static final String MODID = "zstdmc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Zstdmc() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);

        // Prefer pure Java implementation for stability across environments.
        System.setProperty("zstd.disable.native", "true");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ZstdMC common setup completed.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ZstdMC active: replacing vanilla packet compression when threshold is enabled.");
    }
}
