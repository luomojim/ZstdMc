package cn.tohsaka.factory.zstdmc.mixin;

import cn.tohsaka.factory.zstdmc.Zstdmc;
import cn.tohsaka.factory.zstdmc.codec.ZstdCompressionDecoder;
import cn.tohsaka.factory.zstdmc.codec.ZstdCompressionEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Connection.class)
public abstract class MixinConnection {
    @Unique
    private static Field zstdmc$channelField;

    @Inject(method = "setupCompression(IZ)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void zstdmc$setupCompressionModern(int threshold, boolean validateDecompressed, CallbackInfo ci) {
        if (this.zstdmc$applyCompression(threshold, validateDecompressed)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean zstdmc$applyCompression(int threshold, boolean validateDecompressed) {
        Channel channel = this.zstdmc$getChannel();
        if (channel == null) {
            return false;
        }

        ChannelPipeline pipeline = channel.pipeline();
        if (threshold >= 0) {
            this.zstdmc$installDecoder(pipeline, threshold, validateDecompressed);
            this.zstdmc$installEncoder(pipeline, threshold);
            Zstdmc.LOGGER.debug("Installed Zstd compression handlers with threshold={} validateDecompressed={}", threshold, validateDecompressed);
            return true;
        }

        if (pipeline.get("decompress") instanceof ZstdCompressionDecoder) {
            pipeline.remove("decompress");
        }

        if (pipeline.get("compress") instanceof ZstdCompressionEncoder) {
            pipeline.remove("compress");
        }
        return true;
    }

    @Unique
    private void zstdmc$installDecoder(ChannelPipeline pipeline, int threshold, boolean validateDecompressed) {
        if (pipeline.get("decompress") instanceof ZstdCompressionDecoder zstdDecoder) {
            zstdDecoder.setThreshold(threshold, validateDecompressed);
            return;
        }

        if (pipeline.get("decompress") != null) {
            pipeline.remove("decompress");
        }

        if (pipeline.get("splitter") != null) {
            pipeline.addAfter("splitter", "decompress", new ZstdCompressionDecoder(threshold, validateDecompressed));
        } else if (pipeline.get("decoder") != null) {
            pipeline.addBefore("decoder", "decompress", new ZstdCompressionDecoder(threshold, validateDecompressed));
        } else {
            pipeline.addFirst("decompress", new ZstdCompressionDecoder(threshold, validateDecompressed));
        }
    }

    @Unique
    private void zstdmc$installEncoder(ChannelPipeline pipeline, int threshold) {
        if (pipeline.get("compress") instanceof ZstdCompressionEncoder zstdEncoder) {
            zstdEncoder.setThreshold(threshold);
            return;
        }

        if (pipeline.get("compress") != null) {
            pipeline.remove("compress");
        }

        if (pipeline.get("prepender") != null) {
            pipeline.addAfter("prepender", "compress", new ZstdCompressionEncoder(threshold));
        } else if (pipeline.get("encoder") != null) {
            pipeline.addBefore("encoder", "compress", new ZstdCompressionEncoder(threshold));
        } else {
            pipeline.addLast("compress", new ZstdCompressionEncoder(threshold));
        }
    }

    @Unique
    private Channel zstdmc$getChannel() {
        try {
            if (zstdmc$channelField == null) {
                for (Field field : Connection.class.getDeclaredFields()) {
                    if (Channel.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        zstdmc$channelField = field;
                        break;
                    }
                }
            }
            return zstdmc$channelField != null ? (Channel) zstdmc$channelField.get(this) : null;
        } catch (Throwable t) {
            Zstdmc.LOGGER.debug("Failed to access Connection channel field via reflection.", t);
            return null;
        }
    }
}
