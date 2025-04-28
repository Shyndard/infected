package com.shyndard.minecraft.infected.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.shyndard.minecraft.infected.PluginApp;
import com.shyndard.minecraft.infected.game.Infected;

public class InfectedCmd implements CommandExecutor {

    private final PluginApp app;

    public InfectedCmd(final PluginApp app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (sender instanceof Player player) {
            if (args.length == 0) {
                displayHelp(sender);
            } else if ("reload".equalsIgnoreCase(args[0])) {
                app.reloadConfig();
                Infected.get().reload();
            } else if ("setlobby".equalsIgnoreCase(args[0])) {
                String prefix = "lobby";
                setPosition(player, prefix);
                sender.sendMessage("Lobby spawn set.");
            } else if (args.length == 3 && "setspawn".equalsIgnoreCase(args[0])) {
                setMapSpawn(sender, args, player);
            } else if (args.length == 3 && "settimer".equalsIgnoreCase(args[0])) {
                setMapTimer(sender, args);
            } else {
                displayHelp(sender);
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
        return true;
    }

    private void setMapTimer(final CommandSender sender, final String[] args) {
        int timer = Integer.parseInt(args[2]);
        if (timer > 10) {
            app.getConfig().set("arena.%s.timer".formatted(args[1]), timer);
            app.saveConfig();
            sender.sendMessage("Timer set to %s for arena %s".formatted(timer, args[1]));
        } else {
            sender.sendMessage("Timer must be greater than 10 seconds.");
        }
    }

    private void setMapSpawn(final CommandSender sender, final String[] args, final Player player) {
        if ("zombie".equalsIgnoreCase(args[2]) || "survivor".equalsIgnoreCase(args[2])
                || "lobby".equalsIgnoreCase(args[2])) {
            setPosition(player, "arena.%s.%s".formatted(args[1], args[2]));
            sender.sendMessage("Spawn %s set for arena %s".formatted(args[2], args[1]));
        } else {
            sender.sendMessage(
                    "Use 'zombie', 'survivor' or 'lobby'. /cmd setspawn <map> <zombie|survivor|lobby>");
        }
    }

    private void setPosition(final Player player, final String prefix) {
        app.getConfig().set(prefix + ".world", player.getWorld().getName());
        app.getConfig().set(prefix + ".x", player.getLocation().getX());
        app.getConfig().set(prefix + ".y", player.getLocation().getY());
        app.getConfig().set(prefix + ".z", player.getLocation().getZ());
        app.getConfig().set(prefix + ".yaw", player.getLocation().getYaw());
        app.getConfig().set(prefix + ".pitch", player.getLocation().getPitch());
        app.saveConfig();
    }

    private void displayHelp(final CommandSender sender) {
        sender.sendMessage("Infected commands:");
        sender.sendMessage("/infected reload - Reload the plugin configuration.");
        sender.sendMessage("/infected setlobby - Set the lobby spawn point.");
        sender.sendMessage(
                "/infected setspawn <map> <zombie|survivor|lobby> - Set the spawn point for the specified map.");
        sender.sendMessage("/infected settimer <map> <time> - Set the timer for the specified map.");
    }

}
