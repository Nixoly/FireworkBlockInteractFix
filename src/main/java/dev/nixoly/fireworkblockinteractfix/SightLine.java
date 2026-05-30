package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

final class SightLine {

    private SightLine() {
    }

    static Optional<Block> blockInFrontOf(Player player) {
        if (player.getEyeLocation().getBlock().getType().name().contains("WEB")) {
            return Optional.empty();
        }

        Vector origin = player.getEyeLocation().toVector();
        Vector direction = player.getEyeLocation().getDirection();

        double occludingPlayerAlong = RayOcclusion.nearestOtherPlayerAlongRay(player, origin, direction);

        Block hit = player.getTargetBlockExact((int) Math.ceil(Settings.REACH), FluidCollisionMode.NEVER);

        if (hit == null || hit.getType().isAir()) {
            return Optional.empty();
        }

        boolean isCobweb = hit.getType().name().contains("WEB");

        double along = direction.dot(hit.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(origin));

        if (!isCobweb && occludingPlayerAlong <= Settings.REACH) {
            if (occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON || along < 1.5D) {
                return Optional.empty();
            }
        }

        double verticalDist = hit.getLocation().add(0.5, 0.5, 0.5).getY() - origin.getY();
        if (verticalDist > Settings.MAX_VERTICAL_BLOCK_DISTANCE) {
            return Optional.empty();
        }

        if (!isCobweb && occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON) {
            return Optional.empty();
        }

        return Optional.of(hit);
    }
}
