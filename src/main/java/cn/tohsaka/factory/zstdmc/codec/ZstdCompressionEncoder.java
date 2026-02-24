package cn.tohsaka.factory.zstdmc.codec;

import cn.tohsaka.factory.zstdmc.Config;
import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.FriendlyByteBuf;

public class ZstdCompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private int threshold;

    public ZstdCompressionEncoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int readableBytes = msg.readableBytes();
        if (readableBytes < this.threshold) {
            new FriendlyByteBuf(out).writeVarInt(0);
            out.writeBytes(msg, msg.readerIndex(), readableBytes);
            ZstdMetric.update(ctx, 0, 0, readableBytes, readableBytes);
            return;
        }

        byte[] source = ByteBufUtil.getBytes(msg, msg.readerIndex(), readableBytes, false);
        int maxCompressedSize = Math.toIntExact(Zstd.compressBound(readableBytes));
        byte[] compressed = new byte[maxCompressedSize];

        long compressedSize = Zstd.compressByteArray(
                compressed,
                0,
                compressed.length,
                source,
                0,
                source.length,
                Config.getLevel()
        );

        if (Zstd.isError(compressedSize)) {
            throw new EncoderException("Zstd compression failed: " + Zstd.getErrorName(compressedSize));
        }

        int compressedSizeInt = Math.toIntExact(compressedSize);
        if (compressedSizeInt >= readableBytes) {
            // Keep bandwidth stable for incompressible payloads.
            new FriendlyByteBuf(out).writeVarInt(0);
            out.writeBytes(source);
            ZstdMetric.update(ctx, 0, 0, readableBytes, readableBytes);
            return;
        }

        new FriendlyByteBuf(out).writeVarInt(readableBytes);
        out.writeBytes(compressed, 0, compressedSizeInt);
        ZstdMetric.update(ctx, 0, 0, readableBytes, compressedSizeInt);
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
