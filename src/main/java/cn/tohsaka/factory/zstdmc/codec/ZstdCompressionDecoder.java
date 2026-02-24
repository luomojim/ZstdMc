package cn.tohsaka.factory.zstdmc.codec;

import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ZstdCompressionDecoder extends ByteToMessageDecoder {
    private static final int MAX_PACKET_SIZE = 16777216;

    private int threshold;
    private boolean validateDecompressed;

    public ZstdCompressionDecoder(int threshold, boolean validateDecompressed) {
        this.threshold = threshold;
        this.validateDecompressed = validateDecompressed;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!in.isReadable()) {
            return;
        }

        int decompressedLength = new FriendlyByteBuf(in).readVarInt();
        if (decompressedLength == 0) {
            int readable = in.readableBytes();
            out.add(in.readRetainedSlice(readable));
            ZstdMetric.update(ctx, readable, readable, 0, 0);
            return;
        }

        if (decompressedLength < this.threshold) {
            throw new DecoderException("Zstd: Badly compressed packet - size of " + decompressedLength + " is below threshold of " + this.threshold);
        }

        if (decompressedLength > MAX_PACKET_SIZE) {
            throw new DecoderException("Zstd: Packet too large (" + decompressedLength + ")");
        }

        int readable = in.readableBytes();
        byte[] compressed = new byte[readable];
        in.readBytes(compressed);

        byte[] decompressed = new byte[decompressedLength];
        long result = Zstd.decompressByteArray(
                decompressed,
                0,
                decompressedLength,
                compressed,
                0,
                readable
        );

        if (Zstd.isError(result)) {
            throw new DecoderException("Zstd decompression failed: " + Zstd.getErrorName(result));
        }

        int actualLength = Math.toIntExact(result);
        if (this.validateDecompressed && actualLength != decompressedLength) {
            throw new DecoderException("Zstd: Invalid decompressed size. Expected " + decompressedLength + ", got " + actualLength);
        }

        out.add(Unpooled.wrappedBuffer(decompressed, 0, actualLength));
        ZstdMetric.update(ctx, actualLength, readable, 0, 0);
    }

    public void setThreshold(int threshold, boolean validateDecompressed) {
        this.threshold = threshold;
        this.validateDecompressed = validateDecompressed;
    }
}
