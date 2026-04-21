package dev.nixoly.fireworkblockinteractfix;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class FireworkBlockInteractFix extends JavaPlugin {

    private PlayerStateManager states;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        states = new PlayerStateManager();
        int pluginId = 30893;
        Metrics metrics = new Metrics(this, pluginId);

        PacketEvents.getAPI().getEventManager().registerListener(new FireworkPacketListener(this, states));
        PacketEvents.getAPI().init();

        getServer().getPluginManager().registerEvents(new ElytraFireworkLaunchListener(this, states), this);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        if (states != null) states.clearAll();
    }
}
