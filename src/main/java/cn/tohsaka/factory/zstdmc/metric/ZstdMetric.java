package cn.tohsaka.factory.zstdmc.metric;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class ZstdMetric {
    private static final ConcurrentHashMap<UUID, MetricData> MAP = new ConcurrentHashMap<>();
    private static final LongAdder RX_BYTES_ORIGINAL = new LongAdder();
    private static final LongAdder TX_BYTES_ORIGINAL = new LongAdder();
    private static final LongAdder RX_BYTES_COMPRESSED = new LongAdder();
    private static final LongAdder TX_BYTES_COMPRESSED = new LongAdder();

    private ZstdMetric() {
    }

    public static void reset() {
        MAP.clear();
        RX_BYTES_ORIGINAL.reset();
        RX_BYTES_COMPRESSED.reset();
        TX_BYTES_ORIGINAL.reset();
        TX_BYTES_COMPRESSED.reset();
    }

    public static void update(ChannelHandlerContext ctx, long rxOriginal, long rxCompressed, long txOriginal, long txCompressed) {
        var packetHandler = ctx.channel().pipeline().get("packet_handler");
        if (packetHandler instanceof Connection connection) {
            var listener = connection.getPacketListener();
            if (listener instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
                var player = serverGamePacketListener.getPlayer();
                if (player != null) {
                    var uuid = player.getUUID();
                    MAP.computeIfAbsent(uuid, ignored -> new MetricData())
                            .update(player, player.getIpAddress(), rxOriginal, rxCompressed, txOriginal, txCompressed);
                }
            }
        }

        RX_BYTES_ORIGINAL.add(rxOriginal);
        TX_BYTES_ORIGINAL.add(txOriginal);
        RX_BYTES_COMPRESSED.add(rxCompressed);
        TX_BYTES_COMPRESSED.add(txCompressed);
    }

    public static long getRxbytes() {
        return RX_BYTES_ORIGINAL.longValue();
    }

    public static long getTxbytes() {
        return TX_BYTES_ORIGINAL.longValue();
    }

    public static long getRxbytes2() {
        return RX_BYTES_COMPRESSED.longValue();
    }

    public static long getTxbytes2() {
        return TX_BYTES_COMPRESSED.longValue();
    }

    public static Map<UUID, MetricData> getMap() {
        return Collections.unmodifiableMap(MAP);
    }
}
