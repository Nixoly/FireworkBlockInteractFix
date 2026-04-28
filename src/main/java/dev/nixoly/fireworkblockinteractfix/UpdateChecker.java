package dev.nixoly.fireworkblockinteractfix;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

final class UpdateChecker {

    private static final Gson GSON = new Gson();
    private static final String REPO = "Nixoly/FireworkBlockInteractFix";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";

    private final JavaPlugin plugin;

    UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    CompletableFuture<Optional<LatestRelease>> checkLatestRelease() {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "FireworkBlockInteractFix-UpdateCheck");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int code = connection.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    return Optional.empty();
                }

                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json == null || !json.has("tag_name") || !json.has("html_url")) {
                        return Optional.empty();
                    }
                    String tag = json.get("tag_name").getAsString();
                    String page = json.get("html_url").getAsString();
                    return Optional.of(new LatestRelease(tag, page));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Update check failed: " + e.getMessage());
                return Optional.empty();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    static final class LatestRelease {

        final String tagName;
        final String htmlUrl;

        LatestRelease(String tagName, String htmlUrl) {
            this.tagName = tagName;
            this.htmlUrl = htmlUrl;
        }
    }
}
