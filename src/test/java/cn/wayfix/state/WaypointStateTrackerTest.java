package cn.wayfix.state;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaypointStateTrackerTest {

    private static final WrapperPlayServerWaypoint.Operation TRACK = WrapperPlayServerWaypoint.Operation.TRACK;
    private static final WrapperPlayServerWaypoint.Operation UPDATE = WrapperPlayServerWaypoint.Operation.UPDATE;
    private static final WrapperPlayServerWaypoint.Operation UNTRACK = WrapperPlayServerWaypoint.Operation.UNTRACK;

    @Test
    void allowsNormalTrackUpdateAndUntrackFlow() {
        WaypointStateTracker tracker = new WaypointStateTracker();
        UUID playerId = UUID.randomUUID();
        UUID waypointId = UUID.randomUUID();

        assertTrue(tracker.handle(playerId, TRACK, waypointId));
        assertTrue(tracker.handle(playerId, UPDATE, waypointId));
        assertTrue(tracker.handle(playerId, UNTRACK, waypointId));
        assertFalse(tracker.isTracked(playerId, waypointId));
    }

    @Test
    void rejectsUpdateWithoutTrack() {
        WaypointStateTracker tracker = new WaypointStateTracker();

        assertFalse(tracker.handle(UUID.randomUUID(), UPDATE, UUID.randomUUID()));
    }

    @Test
    void supportsStringIdentifiers() {
        WaypointStateTracker tracker = new WaypointStateTracker();
        UUID playerId = UUID.randomUUID();

        assertTrue(tracker.handle(playerId, TRACK, "服务器大厅"));
        assertTrue(tracker.handle(playerId, UPDATE, "服务器大厅"));
        assertTrue(tracker.handle(playerId, UNTRACK, "服务器大厅"));
        assertFalse(tracker.handle(playerId, UPDATE, "服务器大厅"));
    }

    @Test
    void repeatedUntrackIsHarmless() {
        WaypointStateTracker tracker = new WaypointStateTracker();
        UUID playerId = UUID.randomUUID();
        UUID waypointId = UUID.randomUUID();

        assertTrue(tracker.handle(playerId, UNTRACK, waypointId));
        assertTrue(tracker.handle(playerId, UNTRACK, waypointId));
    }

    @Test
    void resetInvalidatesPreviouslyTrackedWaypoints() {
        WaypointStateTracker tracker = new WaypointStateTracker();
        UUID playerId = UUID.randomUUID();
        UUID waypointId = UUID.randomUUID();

        assertTrue(tracker.handle(playerId, TRACK, waypointId));
        tracker.reset(playerId);

        assertFalse(tracker.handle(playerId, UPDATE, waypointId));
    }

    @Test
    void nullInputIsRejected() {
        WaypointStateTracker tracker = new WaypointStateTracker();

        assertFalse(tracker.handle(null, TRACK, UUID.randomUUID()));
        assertFalse(tracker.handle(UUID.randomUUID(), TRACK, null));
        assertFalse(tracker.handle(UUID.randomUUID(), null, UUID.randomUUID()));
    }
}
