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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class UpdateAnnouncer {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private UpdateAnnouncer() {
    }

    static boolean shouldNotify(String currentVersion, UpdateChecker.LatestRelease release) {
        if (currentVersion == null || release == null || release.tagName == null) {
            return false;
        }
        String cur = normalizeVersion(currentVersion);
        if (cur.isEmpty() || cur.toLowerCase().contains("dev") || cur.contains("${")) {
            return false;
        }
        SemanticVersion current = SemanticVersion.parse(cur);
        SemanticVersion latest = SemanticVersion.parse(normalizeVersion(release.tagName));
        return current != null && latest != null && latest.compareTo(current) > 0;
    }

    static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        return stripLeadingV(version.trim());
    }

    private static String stripLeadingV(String v) {
        return (v.length() > 1 && (v.charAt(0) == 'v' || v.charAt(0) == 'V')) ? v.substring(1) : v;
    }

    static Component buildMessage(String currentVersion, String latestTag, String releaseUrl) {
        Component github = Component.text("GitHub", NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(releaseUrl))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to download the lasest release on github.", TextColor.fromHexString("#A1DDCC"))));

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

    private static final class SemanticVersion implements Comparable<SemanticVersion> {

        private final List<BigInteger> numbers;
        private final List<String> preRelease;

        private SemanticVersion(List<BigInteger> numbers, List<String> preRelease) {
            this.numbers = numbers;
            this.preRelease = preRelease;
        }

        static SemanticVersion parse(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }

            String withoutBuild = value.split("\\+", 2)[0];
            String[] versionParts = withoutBuild.split("-", 2);
            String[] numberParts = versionParts[0].split("\\.", -1);
            List<BigInteger> numbers = new ArrayList<>();
            try {
                for (String number : numberParts) {
                    if (number.isEmpty()) {
                        return null;
                    }
                    numbers.add(new BigInteger(number));
                }
            } catch (NumberFormatException ignored) {
                return null;
            }

            List<String> preRelease = Collections.emptyList();
            if (versionParts.length == 2) {
                if (versionParts[1].isEmpty()) {
                    return null;
                }
                preRelease = new ArrayList<>();
                for (String identifier : versionParts[1].split("\\.", -1)) {
                    if (identifier.isEmpty() || !identifier.matches("[0-9A-Za-z-]+")) {
                        return null;
                    }
                    preRelease.add(identifier);
                }
            }
            return new SemanticVersion(numbers, preRelease);
        }

        @Override
        public int compareTo(SemanticVersion other) {
            int size = Math.max(numbers.size(), other.numbers.size());
            for (int index = 0; index < size; index++) {
                BigInteger left = index < numbers.size() ? numbers.get(index) : BigInteger.ZERO;
                BigInteger right = index < other.numbers.size() ? other.numbers.get(index) : BigInteger.ZERO;
                int comparison = left.compareTo(right);
                if (comparison != 0) {
                    return comparison;
                }
            }

            if (preRelease.isEmpty() || other.preRelease.isEmpty()) {
                return Boolean.compare(preRelease.isEmpty(), other.preRelease.isEmpty());
            }

            int identifiers = Math.min(preRelease.size(), other.preRelease.size());
            for (int index = 0; index < identifiers; index++) {
                int comparison = compareIdentifier(preRelease.get(index), other.preRelease.get(index));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(preRelease.size(), other.preRelease.size());
        }

        private static int compareIdentifier(String left, String right) {
            boolean leftNumeric = left.matches("\\d+");
            boolean rightNumeric = right.matches("\\d+");
            if (leftNumeric && rightNumeric) {
                return new BigInteger(left).compareTo(new BigInteger(right));
            }
            if (leftNumeric != rightNumeric) {
                return leftNumeric ? -1 : 1;
            }
            return left.compareTo(right);
        }
    }
}
