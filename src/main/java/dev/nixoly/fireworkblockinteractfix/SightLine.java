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
        Vector origin = player.getEyeLocation().toVector();
        Vector direction = player.getEyeLocation().getDirection();

        double occludingPlayerAlong = RayOcclusion.nearestOtherPlayerAlongRay(player, origin, direction);

        Block hit = player.getTargetBlockExact((int) Math.ceil(Settings.REACH), FluidCollisionMode.NEVER);

        if (hit == null || hit.getType().isAir()) {
            return Optional.empty();
        }

        double along = direction.dot(hit.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(origin));

        if (occludingPlayerAlong <= along + Settings.RAY_PARAMETER_EPSILON) {
            return Optional.empty();
        }

        return Optional.of(hit);
    }
}
