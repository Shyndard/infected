package com.shyndard.minecraft.infected.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

public class InfectedConfig {

    private final JavaPlugin app;
    @Getter
    private Map<String, InfectedMap> maps = new HashMap<>();
    @Getter
    private Location lobby;

    public InfectedConfig(final JavaPlugin app) {
        this.app = app;
        load();
    }

    private void load() {
        app.getLogger().info("Loading lobby from config file...");
        lobby = loadLocation("lobby");
        // Load every map from config file with prefix "arena"
        app.getLogger().info("Loading maps from config file...");
        app.getConfig().getConfigurationSection("arena").getKeys(false).forEach(key -> {
            try {
                String prefix = "arena.%s".formatted(key);
                app.getLogger().info("Loading %s arena (%s)".formatted(key, prefix));
                maps.put(key, createMap(prefix));
            } catch (final Exception ex) {
                app.getLogger().warning("Failed to load map %s: %s".formatted(key, ex.getMessage()));
            }
        });
    }

    private InfectedMap createMap(final String prefix) {
        Integer timer = app.getConfig().getInt(prefix + ".timer");
        if (timer == 0) {
            throw new RuntimeException("Timer not found for prefix %s".formatted(prefix));
        }
        return InfectedMap.builder()
                .lobbySpawn(loadLocation("%s.lobby".formatted(prefix)))
                .zombieSpawn(loadLocation("%s.zombie".formatted(prefix)))
                .survivorSpawn(loadLocation("%s.survivor".formatted(prefix)))
                .timer(timer)
                .build();
    }

    private Location loadLocation(final String prefix) {
        return new Location(
                Bukkit.getWorld(app.getConfig().getString(prefix + ".world")),
                app.getConfig().getDouble(prefix + ".x"),
                app.getConfig().getDouble(prefix + ".y"),
                app.getConfig().getDouble(prefix + ".z"),
                app.getConfig().getInt(prefix + ".yaw"),
                app.getConfig().getInt(prefix + ".pitch"));
    }

}
