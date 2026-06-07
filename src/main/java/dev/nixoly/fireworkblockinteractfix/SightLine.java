package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

final class SightLine {

    private SightLine() {
    }

    static Optional<Block> cobwebToRedirect(Player player) {
        Block feet = player.getLocation().getBlock();
        Block eye = player.getEyeLocation().getBlock();
        Block web = isWeb(feet) ? feet : (isWeb(eye) ? eye : null);
        if (web == null || RayOcclusion.otherPlayerOnSight(player)) {
            return Optional.empty();
        }
        return Optional.of(web);
    }

    static Optional<Block> blockInFrontOf(Player player) {
        if (isWeb(player.getEyeLocation().getBlock())) {
            return Optional.empty();
        }

        Vector origin = player.getEyeLocation().toVector();
        Vector direction = player.getEyeLocation().getDirection();

        double occludingPlayerAlong = RayOcclusion.nearestOtherPlayerAlongRay(player, origin, direction);

        Block hit = player.getTargetBlockExact((int) Math.ceil(Settings.REACH), FluidCollisionMode.NEVER);

        if (hit == null || hit.getType().isAir()) {
            return Optional.empty();
        }

        double verticalDist = hit.getLocation().add(0.5, 0.5, 0.5).getY() - origin.getY();
        if (verticalDist > Settings.MAX_VERTICAL_BLOCK_DISTANCE) {
            return Optional.empty();
        }

        if (isWeb(hit)) {
            return Optional.of(hit);
        }

        double along = direction.dot(hit.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(origin));

        if (occludingPlayerAlong <= Settings.REACH) {
            if (occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON || along < 1.5D) {
                return Optional.empty();
            }
        }

        if (occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON) {
            return Optional.empty();
        }

        return Optional.of(hit);
    }

    private static boolean isWeb(Block block) {
        return block.getType().name().contains("WEB");
    }
}
