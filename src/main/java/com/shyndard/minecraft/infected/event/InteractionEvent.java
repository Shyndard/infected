package com.shyndard.minecraft.infected.event;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.shyndard.minecraft.infected.game.Infected;
import com.shyndard.minecraft.infected.game.State;

public class InteractionEvent implements Listener {

    @EventHandler
    public void onDamageEvent(final FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPvp(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(Infected.get().getState() != State.INGAME);
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        event.deathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
        Infected.get().playerDied(player);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        event.setRespawnLocation(Infected.get().onPlayerRespawn(event.getPlayer()));
    }

    @EventHandler
    public void onBreakBlock(final BlockBreakEvent event) {
        event.setCancelled(event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp());
    }

    @EventHandler
    public void onBreakBlock(final BlockPlaceEvent event) {
        event.setCancelled(event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp());
    }
}
