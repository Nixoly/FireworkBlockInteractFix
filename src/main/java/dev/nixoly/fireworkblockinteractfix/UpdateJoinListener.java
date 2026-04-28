package dev.nixoly.fireworkblockinteractfix;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.atomic.AtomicReference;

final class UpdateJoinListener implements Listener {

    private final AtomicReference<Component> pendingMessage;

    UpdateJoinListener(AtomicReference<Component> pendingMessage) {
        this.pendingMessage = pendingMessage;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) {
            return;
        }
        Component message = pendingMessage.get();
        if (message != null) {
            UpdateAnnouncer.sendToPlayer(event.getPlayer(), message);
        }
    }
}
