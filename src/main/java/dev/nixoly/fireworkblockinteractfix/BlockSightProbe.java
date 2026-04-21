package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class BlockSightProbe {

    private static final double SCAN_STEP = 0.1D;

    private BlockSightProbe() {
    }

    /**
     * Probes the player's surroundings to decide whether a firework
     * launch qualifies as a block interaction.
     *
     * Two independent rollback paths:
     *
     *   <b>Normal path</b> — crosshair hits any non-air block within
     *   {@link Tunables#SIGHT_REACH}. Velocity scan still required to
     *   confirm the boost actually happened.
     *
     *   <b>Cobweb path</b> — any part of the player's bounding box
     *   overlaps a cobweb block, AND the crosshair points at any
     *   non-air block (the cobweb itself counts). In this case the
     *   rollback is immediate; velocity cannot be trusted because
     *   the web dampens motion.
     */
    static Result probe(Player player) {
        boolean cobwebContact = bodyOverlapsCobweb(player);
        boolean crosshairHit = false;
        Material hitType = Material.AIR;

        Location eye = player.getEyeLocation();
        Vector step = eye.getDirection().normalize().multiply(SCAN_STEP);
        Location cursor = eye.clone();
        Block lastVisited = null;
        int totalSteps = (int) Math.ceil(Tunables.SIGHT_REACH / SCAN_STEP);

        for (int i = 1; i <= totalSteps; i++) {
            cursor.add(step);
            Block block = cursor.getBlock();
            if (block.equals(lastVisited)) continue;
            lastVisited = block;

            if (!block.getType().isAir()) {
                crosshairHit = true;
                hitType = block.getType();
                break;
            }
        }

        boolean immediateRollback = cobwebContact && crosshairHit;
        boolean deferredRollback = crosshairHit && !cobwebContact;

        return new Result(immediateRollback, deferredRollback,
                crosshairHit, cobwebContact, hitType);
    }

    private static boolean bodyOverlapsCobweb(Player player) {
        BoundingBox box = player.getBoundingBox();
        int minX = floor(box.getMinX());
        int minY = floor(box.getMinY());
        int minZ = floor(box.getMinZ());
        int maxX = floor(box.getMaxX());
        int maxY = floor(box.getMaxY());
        int maxZ = floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (player.getWorld().getBlockAt(x, y, z).getType() == Material.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int floor(double v) {
        return (int) Math.floor(v);
    }

    static final class Result {
        /** Cobweb overlap + crosshair on block — skip velocity, rollback now. */
        final boolean immediateRollback;
        /** Crosshair on block, no cobweb — arm snapshot, wait for velocity. */
        final boolean deferredRollback;
        final boolean crosshairHit;
        final boolean cobwebContact;
        final Material crosshairBlock;

        Result(boolean immediateRollback, boolean deferredRollback,
               boolean crosshairHit, boolean cobwebContact, Material crosshairBlock) {
            this.immediateRollback = immediateRollback;
            this.deferredRollback = deferredRollback;
            this.crosshairHit = crosshairHit;
            this.cobwebContact = cobwebContact;
            this.crosshairBlock = crosshairBlock;
        }
    }
}
