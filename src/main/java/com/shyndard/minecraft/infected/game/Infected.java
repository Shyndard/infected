package com.shyndard.minecraft.infected.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class Infected {

    private static final int COUNTDOWN = 10;

    private static Infected instance;

    private final int minPlayer = 2;
    private final List<Player> players = new ArrayList<>();

    private final List<Player> survivors = new ArrayList<>();
    private final List<Player> zombies = new ArrayList<>();
    @Getter
    private State state = State.WAITING_PLAYERS;
    private InfectedConfig config;
    private String nextMap = "default";

    private final JavaPlugin app;

    public Infected(final JavaPlugin app) {
        this.app = app;
        loadConfig();
    }

    private void loadConfig() {
        config = new InfectedConfig(app);
    }

    public static Infected init(JavaPlugin app) {
        return instance = new Infected(app);
    }

    public static Infected get() {
        return instance;
    }

    public void reset() {
        players.clear();
        players.addAll(Bukkit.getOnlinePlayers());
        players.forEach(player -> {
            teleportToLobby(player);
        });
        zombies.clear();
        survivors.clear();
        state = State.WAITING_PLAYERS;
        tryToStartCountdown();
    }

    public void addPlayer(final Player player) {
        if (state == State.WAITING_PLAYERS && !players.contains(player)) {
            app.getLogger().info(String.format("Player %s added to the game", player.getName()));
            players.add(player);
            broadcast(Component.text(String.format("%s a rejoint (%s/%s)", player.getName(),
                    players.size(), Bukkit.getMaxPlayers())));
            tryToStartCountdown();
        }
        cleanPlayer(player);
        teleportToLobby(player);
    }

    private void tryToStartCountdown() {
        if (state == State.WAITING_PLAYERS && players.size() >= minPlayer) {
            if (config.getMaps().isEmpty()) {
                app.getLogger().warning("No maps found, cannot start the game");
                broadcast(Component.text("[Infecté] Aucune map trouvée, le jeu ne peut pas commencer"));
            } else {
                app.getLogger().info("Starting the countdown");
                state = State.COUNTDOWN;
                AtomicInteger time = new AtomicInteger(COUNTDOWN);
                broadcast(
                        Component.text(
                                String.format("[Infecté] Prochaine map dans %s secondes, vote ouvert", time.get())));
                Bukkit.getScheduler().runTaskTimer(app, () -> {
                    int value = time.getAndDecrement();
                    if (value == 0) {
                        Bukkit.getScheduler().cancelTasks(app);
                        startGame();
                    } else {
                        sendRemainingTime(COUNTDOWN, value, Component.text(String.format("Début dans %s", value)));
                        if (value == 10) {
                            setNextMap();
                            broadcast(Component.text(
                                    String.format("[Infecté] Début dans %s secondes, prochaine map: %s", value,
                                            nextMap)));
                            players.forEach(player -> player.teleport(getCurrentMap().getLobbySpawn()));
                        }
                    }
                }, 20L, 20L);
            }
        }
    }

    private void setNextMap() {
        // Randomly select a map from the available maps
        nextMap = config.getMaps()
                .keySet()
                .stream()
                .skip(new Random().nextInt(config.getMaps().size()))
                .findFirst().orElse("default");
    }

    private void startGame() {
        if (state == State.COUNTDOWN) {
            if (players.size() < minPlayer) {
                state = State.WAITING_PLAYERS;
                broadcast(Component
                        .text(String.format("Il manque %s joueurs pour commencer", minPlayer - players.size())));
            } else {
                app.getLogger().info("Starting the game");
                state = State.INGAME;
                startInGameCountdown();
                // Define teams
                int random = (int) (Math.random() * players.size());
                zombies.add(players.get(random));
                players.stream().filter(player -> !zombies.contains(player)).forEach(survivors::add);
                // Start each team
                zombies.forEach(this::startZombie);
                survivors.forEach(this::startSurvivor);
            }
        }
    }

    private void startInGameCountdown() {
        final int originalTime = getCurrentMap().getTimer();
        AtomicInteger timer = new AtomicInteger(originalTime);
        BossBarUtils.createFor(players);
        Bukkit.getScheduler().runTaskTimer(app, () -> {
            int value = timer.getAndDecrement();
            sendRemainingTime(originalTime, value, null);
            BossBarUtils.setTitle("Il reste %s survivant(s)".formatted(survivors.size()));
            if (value == 0) {
                endGame();
            }
        }, 0L, 20L);
    }

    private void sendRemainingTime(final int originalTime, final int currentTime, final TextComponent actionBar) {
        float xp = (float) currentTime / (float) originalTime;
        players.forEach(player -> {
            player.setLevel(currentTime);
            player.setExp(xp);
            if (Objects.nonNull(actionBar)) {
                player.sendActionBar(actionBar);
            }
        });
    }

    public void startZombie(final Player player) {
        player.sendActionBar(Component.text("Vous êtes un zombie, éliminez les survivants !",
                TextColor.color(255, 0, 0)));
        player.teleport(getZombieSpawn());
        // Give full green leather armor
        player.getInventory().setHelmet(ItemStack.of(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(ItemStack.of(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(ItemStack.of(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(ItemStack.of(Material.LEATHER_BOOTS));
    }

    private void startSurvivor(final Player player) {
        var map = getCurrentMap();
        player.sendActionBar(
                Component.text("Vous êtes un survivant, restez en vie !", TextColor.color(0, 255, 0)));
        player.teleport(map.getSurvivorSpawn());
    }

    private InfectedMap getCurrentMap() {
        return config.getMaps().get(nextMap);
    }

    public void removePlayer(final Player player) {
        if (players.contains(player)) {
            players.remove(player);
            zombies.remove(player);
            survivors.remove(player);
            broadcast(Component.text(String.format("%s a quitté (%s/%s)", player.getName(),
                    players.size(), Bukkit.getMaxPlayers())));
            if (zombies.isEmpty() || survivors.isEmpty()) {
                endGame();
            }
        }
    }

    private void cleanPlayer(final Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.getActivePotionEffects().stream().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private void teleportToLobby(final Player player) {
        player.teleport(config.getLobby());
    }

    public void playerDied(final Player player) {
        if (state == State.INGAME && players.contains(player)) {
            if (survivors.contains(player)) {
                app.getLogger().info(String.format("Survivor %s is now a zombie", player.getName()));
                zombies.add(player);
                survivors.remove(player);
                if (survivors.isEmpty()) {
                    endGame();
                }
            }
        }
    }

    public Location getZombieSpawn() {
        return getCurrentMap().getZombieSpawn();
    }

    private void endGame() {
        if (state == State.INGAME) {
            app.getLogger().info("Ending the game");
            state = State.END_GAME;
            BossBarUtils.clean();
            Bukkit.getScheduler().cancelTasks(app);
            players.forEach(player -> {
                if (!player.isDead()) {
                    player.teleport(getCurrentMap().getLobbySpawn());
                    cleanPlayer(player);
                }
            });
            if (survivors.isEmpty()) {
                broadcast(Component.text("===[ Les zombies ont gagné ]===", TextColor.color(255, 0, 0)));
            } else {
                broadcast(Component.text("===[ Les survivants ont gagné ]===", TextColor.color(0, 255, 0)));
            }
            Bukkit.getScheduler().runTaskLater(app, () -> {
                reset();
            }, 200);
        }
    }

    public Location onPlayerRespawn(final Player player) {
        if (state == State.INGAME && players.contains(player)) {
            startZombie(player);
            return getZombieSpawn();
        } else if (state == State.END_GAME) {
            return getCurrentMap().getLobbySpawn();
        } else {
            return config.getLobby();
        }
    }

    public void broadcast(final Component message) {
        // Do not send non "infected" players a message
        players.forEach(player -> player.sendMessage(message));
    }

    public void reload() {
        Bukkit.getScheduler().cancelTasks(app);
        loadConfig();
        reset();
    }
}
