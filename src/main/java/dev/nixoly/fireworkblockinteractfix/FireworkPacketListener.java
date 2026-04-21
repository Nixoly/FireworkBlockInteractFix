package dev.nixoly.fireworkblockinteractfix;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Logger;

public final class FireworkPacketListener extends PacketListenerAbstract {

    private final Plugin plugin;
    private final PlayerStateManager states;
    private final Logger log;

    public FireworkPacketListener(Plugin plugin, PlayerStateManager states) {
        super(PacketListenerPriority.HIGH);
        this.plugin = plugin;
        this.states = states;
        this.log = plugin.getLogger();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.USE_ITEM) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        if (states.isInteractLocked(player.getUniqueId())) {
            event.setCancelled(true);
            if (Tunables.DEBUG) {
                log.info("[packet] blocked USE_ITEM from " + player.getName() + " (interact locked)");
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_VELOCITY) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(event);
        if (packet.getEntityId() != player.getEntityId()) return;

        Vector3d v = packet.getVelocity();
        double speed = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);

        if (states.isVelocityMuted(uuid, speed)) {
            event.setCancelled(true);
            if (Tunables.DEBUG) {
                log.info("[packet] muted velocity for " + player.getName()
                        + " | speed=" + String.format("%.3f", speed));
            }
        }
    }
}
