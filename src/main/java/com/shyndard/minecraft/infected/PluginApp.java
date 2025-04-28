package com.shyndard.minecraft.infected;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.shyndard.minecraft.infected.command.InfectedCmd;
import com.shyndard.minecraft.infected.event.ConnectionEvent;
import com.shyndard.minecraft.infected.event.InteractionEvent;
import com.shyndard.minecraft.infected.game.BossBarUtils;
import com.shyndard.minecraft.infected.game.Infected;

public class PluginApp extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Load config
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        // Register events
        Bukkit.getPluginManager().registerEvents(new ConnectionEvent(), this);
        Bukkit.getPluginManager().registerEvents(new InteractionEvent(), this);
        Bukkit.getPluginCommand("infected").setExecutor(new InfectedCmd(this));
        // Init game instance
        Infected.init(this);
        Bukkit.getOnlinePlayers().forEach(player -> Infected.get().addPlayer(player));
    }

    @Override
    public void onDisable() {
        BossBarUtils.clean();
    }

}