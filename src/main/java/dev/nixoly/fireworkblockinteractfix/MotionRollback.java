package dev.nixoly.fireworkblockinteractfix;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.logging.Logger;

final class MotionRollback {

    private static final Vector ZERO = new Vector(0, 0, 0);
    private static final Vector3d ZERO_PACKET = new Vector3d(0, 0, 0);

    private MotionRollback() {
    }

    static void apply(Plugin plugin, Player player, Location anchor, UUID firework, PlayerStateManager states) {
        Logger log = plugin.getLogger();
        UUID uuid = player.getUniqueId();

        despawnFirework(player, firework);
        snap(player, anchor);
        states.muteVelocity(uuid);
        states.lockInteract(uuid);

        if (Tunables.DEBUG) {
            log.info("[rollback] " + player.getName()
                    + " | anchor=" + formatLoc(anchor)
                    + " | firework=" + firework
                    + " | hold=" + Tunables.HOLD_TICKS
                    + " | decay=" + Tunables.DECAY_TICKS);
        }

        final Location target = anchor.clone();

        for (int tick = 1; tick <= Tunables.HOLD_TICKS; tick++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player held = Bukkit.getPlayer(uuid);
                if (held == null || !held.isOnline()) return;
                snap(held, target);
            }, tick);
        }

        int decayStart = Tunables.HOLD_TICKS + 1;
        int decayEnd = Tunables.HOLD_TICKS + Tunables.DECAY_TICKS;
        for (int tick = decayStart; tick <= decayEnd; tick++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player held = Bukkit.getPlayer(uuid);
                if (held == null || !held.isOnline()) return;
                held.setVelocity(ZERO);
                sendZeroVelocityPacket(held);
            }, tick);
        }
    }

    private static void snap(Player player, Location anchor) {
        player.teleport(anchor);
        player.setVelocity(ZERO);
        sendZeroVelocityPacket(player);
    }

    private static void sendZeroVelocityPacket(Player player) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(
                    player,
                    new WrapperPlayServerEntityVelocity(player.getEntityId(), ZERO_PACKET));
        } catch (Throwable ignored) {
        }
    }

    private static void despawnFirework(Player player, UUID firework) {
        if (firework == null) return;
        for (Entity e : player.getWorld().getEntities()) {
            if (firework.equals(e.getUniqueId())) {
                if (e instanceof Firework && e.isValid()) e.remove();
                return;
            }
        }
    }

    private static String formatLoc(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
}
