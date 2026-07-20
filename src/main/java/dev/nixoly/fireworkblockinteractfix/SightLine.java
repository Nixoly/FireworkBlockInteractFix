package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Optional;

final class SightLine {

    private SightLine() {
    }

    static Optional<Block> cobwebToRedirect(Player player) {
        Block eye = player.getEyeLocation().getBlock();
        if (!isWeb(eye) || RayOcclusion.overlappingOtherPlayerOnSight(player)) {
            return Optional.empty();
        }
        return Optional.of(eye);
    }

    static Optional<Block> blockInFrontOf(Player player) {
        if (isWeb(player.getEyeLocation().getBlock())) {
            return Optional.empty();
        }

        Vector origin = player.getEyeLocation().toVector();
        Vector direction = player.getEyeLocation().getDirection();

        double occludingPlayerAlong = RayOcclusion.nearestOtherPlayerAlongRay(player, origin, direction);

        RayTraceResult blockTrace = player.rayTraceBlocks(Settings.REACH, FluidCollisionMode.NEVER);
        Block hit = blockTrace == null ? null : blockTrace.getHitBlock();

        if (hit == null || hit.getType().isAir()) {
            return Optional.empty();
        }

        double verticalDist = hit.getLocation().add(0.5, 0.5, 0.5).getY() - origin.getY();
        if (verticalDist > Settings.MAX_VERTICAL_BLOCK_DISTANCE) {
            return Optional.empty();
        }

        Vector hitPosition = blockTrace.getHitPosition();
        double along = direction.dot(hitPosition.clone().subtract(origin));

        if (occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON) {
            return Optional.empty();
        }

        return Optional.of(hit);
    }

    private static boolean isWeb(Block block) {
        return block.getType().name().contains("WEB");
    }
}
