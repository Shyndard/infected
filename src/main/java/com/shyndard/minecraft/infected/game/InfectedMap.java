package com.shyndard.minecraft.infected.game;

import org.bukkit.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class InfectedMap {

    private Location lobbySpawn;
    private Location zombieSpawn;
    private Location survivorSpawn;
    private int timer;
}
