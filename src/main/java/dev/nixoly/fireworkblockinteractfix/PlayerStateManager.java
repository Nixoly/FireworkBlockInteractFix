package dev.nixoly.fireworkblockinteractfix;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateManager {

    private final Map<UUID, Long> velocityMuteUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> interactLockUntil = new ConcurrentHashMap<>();

    public void muteVelocity(UUID player) {
        velocityMuteUntil.put(player, System.currentTimeMillis() + Tunables.VELOCITY_MUTE_MS);
    }

    public boolean isVelocityMuted(UUID player, double speed) {
        Long until = velocityMuteUntil.get(player);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            velocityMuteUntil.remove(player);
            return false;
        }
        return speed >= Tunables.VELOCITY_MUTE_FLOOR;
    }

    public void lockInteract(UUID player) {
        long holdMs = Tunables.HOLD_TICKS * 50L;
        interactLockUntil.put(player, System.currentTimeMillis() + holdMs);
    }

    public boolean isInteractLocked(UUID player) {
        Long until = interactLockUntil.get(player);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            interactLockUntil.remove(player);
            return false;
        }
        return true;
    }

    public void clearAll() {
        velocityMuteUntil.clear();
        interactLockUntil.clear();
    }
}
