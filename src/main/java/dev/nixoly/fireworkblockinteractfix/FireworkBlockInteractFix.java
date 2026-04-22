package dev.nixoly.fireworkblockinteractfix;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class FireworkBlockInteractFix extends JavaPlugin {

    @Override
    public void onEnable() {
        RocketCharge charge = new RocketCharge(this);
        getServer().getPluginManager().registerEvents(new LaunchRedirectListener(this, charge), this);
        new Metrics(this, 30893);
    }
}
