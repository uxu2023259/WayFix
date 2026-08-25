package cn.wayfix.listener;

import cn.wayfix.state.WaypointStateTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {

    private final WaypointStateTracker stateTracker;

    public PlayerLifecycleListener(WaypointStateTracker stateTracker) {
        this.stateTracker = stateTracker;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stateTracker.reset(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        stateTracker.reset(event.getPlayer().getUniqueId());
    }
}
