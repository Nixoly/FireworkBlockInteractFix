package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

final class BlockRocket {

    private BlockRocket() {
    }

    static void shootUpFrom(Block block, Player owner, FireworkMeta meta) {
        Location origin = block.getLocation().add(0.5D, 0.55D, 0.5D);
        Firework rocket = owner.getWorld().spawn(origin, Firework.class);
        rocket.setFireworkMeta(meta);
        rocket.setVelocity(new Vector(0.0D, Settings.REPLACEMENT_LIFT, 0.0D));
    }
}
