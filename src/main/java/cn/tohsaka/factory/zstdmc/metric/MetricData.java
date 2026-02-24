package cn.tohsaka.factory.zstdmc.metric;

import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.LongAdder;

public class MetricData {
    private String name = "";
    private String addr = "";
    private final LongAdder rxbytes = new LongAdder();
    private final LongAdder txbytes = new LongAdder();
    private final LongAdder rxbytes2 = new LongAdder();
    private final LongAdder txbytes2 = new LongAdder();

    public void update(Player player, String addr, long rx, long rx2, long tx, long tx2) {
        this.name = player.getName().getString();
        this.addr = addr;
        rxbytes.add(rx);
        txbytes.add(tx);
        rxbytes2.add(rx2);
        txbytes2.add(tx2);
    }

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

    public Long getRxbytes() {
        return rxbytes.longValue();
    }

    public Long getTxbytes() {
        return txbytes.longValue();
    }

    public Long getRxbytes2() {
        return rxbytes2.longValue();
    }

    public Long getTxbytes2() {
        return txbytes2.longValue();
    }
}
