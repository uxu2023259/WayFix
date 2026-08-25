package cn.wayfix.state;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WaypointStateTracker {

    private final ConcurrentMap<UUID, Set<Object>> trackedWaypoints = new ConcurrentHashMap<>();

    public boolean handle(
            UUID playerId,
            WrapperPlayServerWaypoint.Operation operation,
            Object identifier
    ) {
        if (playerId == null || operation == null || identifier == null) {
            return false;
        }

        return switch (operation) {
            case TRACK -> {
                trackedWaypoints.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(identifier);
                yield true;
            }
            case UPDATE -> isTracked(playerId, identifier);
            case UNTRACK -> {
                remove(playerId, identifier);
                yield true;
            }
        };
    }

    public boolean isTracked(UUID playerId, Object identifier) {
        Set<Object> identifiers = trackedWaypoints.get(playerId);
        return identifiers != null && identifiers.contains(identifier);
    }

    public void reset(UUID playerId) {
        if (playerId != null) {
            trackedWaypoints.remove(playerId);
        }
    }

    public void clear() {
        trackedWaypoints.clear();
    }

    private void remove(UUID playerId, Object identifier) {
        Set<Object> identifiers = trackedWaypoints.get(playerId);
        if (identifiers == null) {
            return;
        }

        identifiers.remove(identifier);
        if (identifiers.isEmpty()) {
            trackedWaypoints.remove(playerId, identifiers);
        }
    }
}
