package com.shyndard.minecraft.infected.event;

import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.shyndard.minecraft.infected.game.Infected;
import com.shyndard.minecraft.infected.game.State;

import io.papermc.paper.event.player.PlayerOpenSignEvent;

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
        event.setCancelled(isUserNotAllow(event.getPlayer()));
    }

    @EventHandler
    public void onPlaceBlock(final BlockPlaceEvent event) {
        event.setCancelled(isUserNotAllow(event.getPlayer()));
    }

    @EventHandler
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            event.setCancelled(isUserNotAllow(event.getPlayer()));
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(final BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onMobSpawn(final EntitySpawnEvent event) {
        event.setCancelled(!(event.getEntity() instanceof Player));
    }

    @EventHandler
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Block block) {
            event.setCancelled(!Tag.BUTTONS.isTagged(block.getType()));
        } else {
            event.setCancelled(isUserNotAllow(event.getPlayer()));
        }
    }

    @EventHandler
    public void onEditSign(final PlayerOpenSignEvent event) {
        event.setCancelled(isUserNotAllow(event.getPlayer()));
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            event.setCancelled(isUserNotAllow(player) && event.getSlotType() == SlotType.ARMOR);
        }
    }

    private boolean isUserNotAllow(final Player player) {
        return player.getGameMode() != GameMode.CREATIVE || !player.isOp();
    }

}
