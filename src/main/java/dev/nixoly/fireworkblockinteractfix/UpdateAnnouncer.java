package dev.nixoly.fireworkblockinteractfix;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class UpdateAnnouncer {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private UpdateAnnouncer() {
    }

    static boolean shouldNotify(String currentVersion, UpdateChecker.LatestRelease release) {
        if (currentVersion == null || release == null) {
            return false;
        }
        String trimmed = currentVersion.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.toLowerCase().contains("dev")) {
            return false;
        }
        String a = normalize(trimmed);
        String b = normalize(release.tagName);
        return !a.equalsIgnoreCase(b);
    }

    private static String normalize(String version) {
        if (version.length() > 1 && (version.charAt(0) == 'v' || version.charAt(0) == 'V')) {
            return version.substring(1);
        }
        return version;
    }

    static Component buildMessage(String currentVersion, String latestTag, String releaseUrl) {
        String safeUrl = releaseUrl.replace("'", "\\'");
        String linkVisible = safeUrl.replace("<", "\\<").replace(">", "\\>");
        String[] templates = new String[] {
                "",
                " <b><gradient:#F64343:#F87B7B>FireworkBlockInteractFix</gradient></b> <dark_gray>»</dark_gray> <green>A new update is available!",
                "",
                "  <gray>Current: <red>%current%</red></gray>",
                "  <gray>Latest: <green>%latest%</green></gray>",
                "",
                "  <gray>Download: <click:open_url:'" + safeUrl + "'><aqua><underlined>{link}</underlined></aqua></click></gray>",
                ""
        };

        Component out = Component.empty();
        for (int i = 0; i < templates.length; i++) {
            if (i > 0) {
                out = out.append(Component.newline());
            }
            String line = templates[i]
                    .replace("%current%", escapeMiniMessage(currentVersion))
                    .replace("%latest%", escapeMiniMessage(latestTag))
                    .replace("{link}", linkVisible);
            out = out.append(MINI_MESSAGE.deserialize(line));
        }
        return out;
    }

    private static String escapeMiniMessage(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("<", "\\<");
    }

    static void broadcast(BukkitAudiences audiences, Component message) {
        audiences.console().sendMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                audiences.player(player).sendMessage(message);
            }
        }
    }
}
