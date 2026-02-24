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
    private static final LongAdder RX_BYTES = new LongAdder();
    private static final LongAdder TX_BYTES = new LongAdder();
    private static final LongAdder RX_BYTES_COMPRESSED = new LongAdder();
    private static final LongAdder TX_BYTES_COMPRESSED = new LongAdder();

    private ZstdMetric() {
    }

    public static void reset() {
        MAP.clear();
        RX_BYTES.reset();
        RX_BYTES_COMPRESSED.reset();
        TX_BYTES.reset();
        TX_BYTES_COMPRESSED.reset();
    }

    public static void update(ChannelHandlerContext ctx, long rx, long rx2, long tx, long tx2) {
        var packetHandler = ctx.channel().pipeline().get("packet_handler");
        if (packetHandler instanceof Connection connection) {
            var listener = connection.getPacketListener();
            if (listener instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
                var player = serverGamePacketListener.getPlayer();
                if (player != null) {
                    var uuid = player.getUUID();
                    MAP.computeIfAbsent(uuid, ignored -> new MetricData())
                            .update(player, player.getIpAddress(), rx, rx2, tx, tx2);
                }
            }
        }

        RX_BYTES.add(rx);
        TX_BYTES.add(tx);
        RX_BYTES_COMPRESSED.add(rx2);
        TX_BYTES_COMPRESSED.add(tx2);
    }

    public static Long getRxbytes() {
        return RX_BYTES.longValue();
    }

    public static Long getTxbytes() {
        return TX_BYTES.longValue();
    }

    public static Long getRxbytes2() {
        return RX_BYTES_COMPRESSED.longValue();
    }

    public static Long getTxbytes2() {
        return TX_BYTES_COMPRESSED.longValue();
    }

    public static Map<UUID, MetricData> getMap() {
        return Collections.unmodifiableMap(MAP);
    }
}
