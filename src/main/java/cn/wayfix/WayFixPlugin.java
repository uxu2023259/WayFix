package cn.wayfix;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import cn.wayfix.listener.PlayerLifecycleListener;
import cn.wayfix.listener.WaypointPacketListener;
import cn.wayfix.state.WaypointStateTracker;
import org.bukkit.plugin.java.JavaPlugin;

public final class WayFixPlugin extends JavaPlugin {

    private final WaypointStateTracker stateTracker = new WaypointStateTracker();
    private PacketListenerCommon registeredPacketListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        WaypointPacketListener packetListener = new WaypointPacketListener(
                stateTracker,
                getLogger(),
                getConfig().getBoolean("log-blocked-updates", false)
        );
        registeredPacketListener = PacketEvents.getAPI().getEventManager().registerListener(
                packetListener,
                PacketListenerPriority.MONITOR
        );

        getServer().getPluginManager().registerEvents(
                new PlayerLifecycleListener(stateTracker),
                this
        );

        getLogger().info("航点数据包修复已启用。异常更新包将被拦截，正常航点功能保持不变。");
    }

    @Override
    public void onDisable() {
        if (registeredPacketListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(registeredPacketListener);
            registeredPacketListener = null;
        }
        stateTracker.clear();
        getLogger().info("航点数据包修复已停用。");
    }
}
