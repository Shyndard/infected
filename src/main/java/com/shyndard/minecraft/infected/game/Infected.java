package com.shyndard.minecraft.infected.game;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

public class Infected {

    private static Infected instance;
    private List<Player> players;

    public static Infected get() {
        if (instance == null) {
            instance = new Infected();
        }
        return instance;
    }

    public void addPlayer(final Player player) {
        if (!players.contains(player)) {
            players.add(player);
            // cleanPlayer(player);
            // teleportPlayer(player);
            // Send message to all players
            Bukkit.broadcast(Component.text(player.getName() + " a rejoint le jeu"));
        }
    }

    public void removePlayer(final Player player) {
        if (players.contains(player)) {
            players.remove(player);
        }
    }

}
