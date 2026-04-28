package dev.nixoly.fireworkblockinteractfix;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class UpdateAnnouncer {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private UpdateAnnouncer() {
    }

    static boolean shouldNotify(String currentVersion, UpdateChecker.LatestRelease release) {
        if (currentVersion == null || release == null) {
            return false;
        }
        String cur = currentVersion.trim();
        if (cur.isEmpty() || cur.toLowerCase().contains("dev")) {
            return false;
        }
        return !stripLeadingV(cur).equalsIgnoreCase(stripLeadingV(release.tagName));
    }

    private static String stripLeadingV(String v) {
        return (v.length() > 1 && (v.charAt(0) == 'v' || v.charAt(0) == 'V')) ? v.substring(1) : v;
    }

    static Component buildMessage(String currentVersion, String latestTag, String releaseUrl) {
        Component github = Component.text("GitHub", NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(releaseUrl))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Latest release on GitHub — click to open.", TextColor.fromHexString("#A1DDCC"))));

        Component download = Component.text("  Download: ", NamedTextColor.GRAY).append(github);

        String cur = escape(currentVersion);
        String lat = escape(latestTag);

        return Component.empty()
                .append(Component.newline())
                .append(MM.deserialize(
                        " <b><gradient:#F64343:#F87B7B>FireworkBlockInteractFix</gradient></b> <dark_gray>»</dark_gray> <green>A new update is available!"))
                .append(Component.newline())
                .append(Component.newline())
                .append(MM.deserialize("  <gray>Current: <red>" + cur + "</red></gray>"))
                .append(Component.newline())
                .append(MM.deserialize("  <gray>Latest: <green>" + lat + "</green></gray>"))
                .append(Component.newline())
                .append(Component.newline())
                .append(download)
                .append(Component.newline());
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("<", "\\<");
    }

    static void broadcast(BukkitAudiences audiences, Component message) {
        audiences.console().sendMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                sendToPlayer(player, message);
            }
        }
    }

    static void sendToPlayer(Player player, Component message) {
        player.spigot().sendMessage(BungeeComponentSerializer.get().serialize(message));
    }
}
