package dev.nixoly.fireworkblockinteractfix;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicReference;

public final class FireworkBlockInteractFix extends JavaPlugin {

    private TaskScheduler tasks;
    private BukkitAudiences adventure;
    private final AtomicReference<Component> pendingUpdateMessage = new AtomicReference<>();

    @Override
    public void onEnable() {
        tasks = UniversalScheduler.getScheduler(this);
        adventure = BukkitAudiences.create(this);
        RocketCharge charge = new RocketCharge(tasks);
        getServer().getPluginManager().registerEvents(new LaunchRedirectListener(this, charge), this);
        getServer().getPluginManager().registerEvents(new UpdateJoinListener(pendingUpdateMessage), this);
        new Metrics(this, 30893);
        scheduleUpdateCheck();
    }

    private void scheduleUpdateCheck() {
        String current = getDescription().getVersion();
        UpdateChecker checker = new UpdateChecker(this);
        checker.checkLatestRelease().whenComplete((remote, error) -> tasks.runTask(() -> {
            if (error != null || remote == null || !remote.isPresent()) {
                return;
            }
            UpdateChecker.LatestRelease release = remote.get();
            if (!UpdateAnnouncer.shouldNotify(current, release)) {
                return;
            }
            Component message = UpdateAnnouncer.buildMessage(current, release.tagName, release.htmlUrl);
            pendingUpdateMessage.set(message);
            UpdateAnnouncer.broadcast(adventure, message);
        }));
    }

    @Override
    public void onDisable() {
        pendingUpdateMessage.set(null);
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        if (tasks != null) {
            tasks.cancelTasks(this);
        }
    }
}
