package dev.nixoly.fireworkblockinteractfix;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

@RequiredArgsConstructor
public final class LaunchRedirectListener implements Listener {

    private final Plugin plugin;
    private final RocketCharge charge;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Firework)) {
            return;
        }

        Firework rocket = (Firework) event.getEntity();
        ProjectileSource shooter = rocket.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;
        Block target = SightLine.blockInFrontOf(player).orElse(null);
        if (target == null) {
            return;
        }
        FireworkMeta meta = rocket.getFireworkMeta().clone();

        charge.snapshot(player);

        event.setCancelled(true);
        if (rocket.isValid()) {
            rocket.remove();
        }

        BlockRocket.shootUpFrom(target, player, meta);
        charge.consumeNextTick(player);

        if (Settings.DEBUG) {
            plugin.getLogger().info(player.getName() + " rocket redirected at "
                    + target.getX() + " " + target.getY() + " " + target.getZ()
                    + " (" + target.getType() + ")");
        }
    }
}
