package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

final class RayOcclusion {

    private RayOcclusion() {
    }

    static boolean overlappingOtherPlayerOnSight(Player viewer) {
        BoundingBox viewerBox = viewer.getBoundingBox();
        Vector origin = viewer.getEyeLocation().toVector();
        Vector direction = viewer.getEyeLocation().getDirection();
        for (Entity entity : viewer.getWorld().getNearbyEntities(viewerBox)) {
            if (!(entity instanceof Player)) {
                continue;
            }
            if (entity.getUniqueId().equals(viewer.getUniqueId())) {
                continue;
            }
            BoundingBox otherBox = entity.getBoundingBox();
            if (otherBox.overlaps(viewerBox) && otherBox.rayTrace(origin, direction, Settings.REACH) != null) {
                return true;
            }
        }
        return false;
    }

    static double nearestOtherPlayerAlongRay(Player viewer, Vector origin, Vector unitDirection) {
        World world = viewer.getWorld();
        Location center = origin.toLocation(world);
        double halfExtent = Settings.REACH;
        double nearest = Double.POSITIVE_INFINITY;

        for (Entity entity : world.getNearbyEntities(center, halfExtent, halfExtent, halfExtent)) {
            if (!(entity instanceof Player)) {
                continue;
            }
            Player other = (Player) entity;
            if (other.getUniqueId().equals(viewer.getUniqueId())) {
                continue;
            }
            BoundingBox box = other.getBoundingBox();
            RayTraceResult hit = box.rayTrace(origin, unitDirection, Settings.REACH);
            if (hit == null) {
                continue;
            }
            Vector hitPosition = hit.getHitPosition();
            if (hitPosition == null) {
                continue;
            }
            double along = unitDirection.dot(hitPosition.clone().subtract(origin));
            if (along < 0.0D || along > Settings.REACH + Settings.RAY_PARAMETER_EPSILON) {
                continue;
            }
            if (along < nearest) {
                nearest = along;
            }
        }
        return nearest;
    }
}
