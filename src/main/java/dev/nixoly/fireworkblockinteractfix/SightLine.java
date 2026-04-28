package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

final class SightLine {

    private SightLine() {
    }

    static Optional<Block> blockInFrontOf(Player player) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        Vector origin = eye.toVector();
        double occludingPlayerAlong = RayOcclusion.nearestOtherPlayerAlongRay(player, origin, direction);

        Block previous = null;
        for (double travelled = 0.0D; travelled <= Settings.REACH; travelled += Settings.SAMPLE_STEP) {
            Block current = eye.clone().add(direction.clone().multiply(travelled)).getBlock();
            if (current.equals(previous)) {
                continue;
            }
            previous = current;

            if (!current.getType().isAir()) {
                if (occludingPlayerAlong <= travelled + Settings.RAY_PARAMETER_EPSILON) {
                    return Optional.empty();
                }
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }
}
