package cn.wayfix.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;
import cn.wayfix.state.WaypointStateTracker;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WaypointPacketListener implements PacketListener {

    private static final long WARNING_INTERVAL_MILLIS = 60_000L;

    private final WaypointStateTracker stateTracker;
    private final Logger logger;
    private final boolean logBlockedUpdates;
    private final WarningRateLimiter warningRateLimiter = new WarningRateLimiter(WARNING_INTERVAL_MILLIS);

    public WaypointPacketListener(
            WaypointStateTracker stateTracker,
            Logger logger,
            boolean logBlockedUpdates
    ) {
        this.stateTracker = stateTracker;
        this.logger = logger;
        this.logBlockedUpdates = logBlockedUpdates;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        UUID playerId = event.getUser().getUUID();

        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME
                || event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            stateTracker.reset(playerId);
            return;
        }

        if (event.getPacketType() != PacketType.Play.Server.WAYPOINT) {
            return;
        }

        try {
            WrapperPlayServerWaypoint packet = new WrapperPlayServerWaypoint(event);
            WrapperPlayServerWaypoint.Operation operation = packet.getOperation();
            Object identifier = packet.getWaypoint().getIdentifier().get();

            if (operation == null || identifier == null) {
                cancelMalformedPacket(event, playerId, "航点操作或标识为空");
                return;
            }

            boolean allowed = stateTracker.handle(playerId, operation, identifier);
            if (!allowed) {
                event.setCancelled(true);
                if (logBlockedUpdates && warningRateLimiter.shouldLog("update:" + playerId)) {
                    logger.info("已拦截玩家 " + event.getUser().getName() + " 的异常航点更新包。");
                }
            }
        } catch (RuntimeException exception) {
            cancelMalformedPacket(event, playerId, "航点数据包解析失败");
            if (warningRateLimiter.shouldLog("parse:" + playerId)) {
                logger.log(Level.WARNING, "检测到无法解析的航点数据包，已拦截，避免发送给客户端。", exception);
            }
        }
    }

    private void cancelMalformedPacket(PacketSendEvent event, UUID playerId, String reason) {
        event.setCancelled(true);
        if (warningRateLimiter.shouldLog("malformed:" + playerId)) {
            logger.warning("检测到异常航点数据包，已拦截。原因：" + reason);
        }
    }
}
