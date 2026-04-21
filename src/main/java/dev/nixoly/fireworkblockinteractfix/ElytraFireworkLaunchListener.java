package dev.nixoly.fireworkblockinteractfix;

import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class ElytraFireworkLaunchListener implements Listener {

    private final Plugin plugin;
    private final PlayerStateManager states;
    private final Logger log;

    public ElytraFireworkLaunchListener(Plugin plugin, PlayerStateManager states) {
        this.plugin = plugin;
        this.states = states;
        this.log = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFireworkLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Firework)) return;
        Firework firework = (Firework) event.getEntity();
        if (!(firework.getShooter() instanceof Player)) return;

        Player player = (Player) firework.getShooter();
        if (!player.isGliding()) return;

        BlockSightProbe.Result result = BlockSightProbe.probe(player);

        if (Tunables.DEBUG) {
            log.info("[probe] " + player.getName()
                    + " | rollback=" + (result.immediateRollback || result.deferredRollback)
                    + " | immediate=" + result.immediateRollback
                    + " | deferred=" + result.deferredRollback
                    + " | crosshair=" + result.crosshairHit + " (" + result.crosshairBlock + ")"
                    + " | cobweb=" + result.cobwebContact
                    + " | vel=" + String.format("%.3f", player.getVelocity().length()));
        }

        if (!result.immediateRollback && !result.deferredRollback) return;

        Location anchor = player.getLocation();

        if (Tunables.DEBUG) {
            String mode = result.immediateRollback ? "immediate (cobweb)" : "deferred (block)";
            log.info("[arm] " + player.getName()
                    + " | mode=" + mode
                    + " | anchor=" + formatLoc(anchor));
        }

        MotionRollback.apply(plugin, player, anchor.clone(), firework.getUniqueId(), states);
    }

    private static String formatLoc(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
}
