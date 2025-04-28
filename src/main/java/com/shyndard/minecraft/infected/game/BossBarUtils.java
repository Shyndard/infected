package com.shyndard.minecraft.infected.game;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtils {

    private static BossBar bb;

    public static void createFor(List<Player> players) {
        bb = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SEGMENTED_10);
        players.forEach(player -> bb.addPlayer(player));
    }

    public static void setTitle(final String title) {
        if (bb == null || bb.getTitle().equalsIgnoreCase(title)) {
            return;
        }
        bb.setTitle(title);
    }

    public static void clean() {
        if (bb == null) {
            return;
        }
        bb.removeAll();
        bb = null;
    }

}
