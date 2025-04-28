package com.shyndard.minecraft.infected.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shyndard.minecraft.infected.game.Infected;

public class ConnectionEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Infected.get().addPlayer(event.getPlayer());
        event.joinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Infected.get().removePlayer(event.getPlayer());
        event.quitMessage(null);
    }

}
