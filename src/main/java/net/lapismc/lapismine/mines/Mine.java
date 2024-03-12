/*
 * Copyright 2024 Benjamin Martin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.lapismc.lapismine.mines;

import net.lapismc.lapiscore.utils.LocationUtils;
import net.lapismc.lapismine.LapisMine;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that represents a single mine instance
 */
public class Mine {

    private final LapisMine plugin;
    private final Composition composition;
    private final LocationUtils locationUtils = new LocationUtils();
    private final String name;
    private Location teleport, l1, l2;
    private Material surface;
    private Integer resetFrequency;
    private boolean replaceOnlyAir;
    private BukkitTask resetTask;
    private BukkitTask warningTask;
    private BukkitTask startCountdownTask;
    private BukkitTask updateCountdownTask;

    /**
     * This initializer should be used when a mine is being created by code
     *
     * @param plugin         The LapisMine plugin instance
     * @param name           The name of the mine
     * @param l1             The maximum point of the mine
     * @param l2             The minimum point of the mine
     * @param composition    The composition of the mine
     * @param surface        The surface material of the mine, null for none
     * @param resetFrequency The reset frequency in minutes
     * @param replaceOnlyAir True if the reset should only replace air blocks
     */
    public Mine(LapisMine plugin, String name, Location l1, Location l2, Composition composition,
                Material surface, Integer resetFrequency, boolean replaceOnlyAir) {
        this.plugin = plugin;
        this.name = name;
        //Calculate the middle at the top
        teleport = new Location(l1.getWorld(), (l1.getBlockX() + l2.getBlockX()) / 2f,
                l1.getY() + 2, (l1.getZ() + l2.getZ()) / 2);
        this.l1 = l1;
        this.l2 = l2;
        this.composition = composition;
        this.surface = surface;
        this.resetFrequency = resetFrequency;
        this.replaceOnlyAir = replaceOnlyAir;
        restartResetTimer();
    }

    /**
     * This initializer should be used when loading from a mines.yml file
     *
     * @param plugin The LapisMines plugin instance
     * @param config The config file to load from
     */
    public Mine(LapisMine plugin, YamlConfiguration config) {
        this.plugin = plugin;
        name = config.getString("Name");
        teleport = locationUtils.parseStringToLocation(config.getString("Locations.teleport"));
        l1 = locationUtils.parseStringToLocation(config.getString("Locations.l1"));
        l2 = locationUtils.parseStringToLocation(config.getString("Locations.l2"));
        composition = new Composition(plugin, config.getStringList("Composition"));
        surface = Material.getMaterial(config.getString("Surface", ""));
        resetFrequency = config.getInt("ResetFrequency");
        replaceOnlyAir = config.getBoolean("ReplaceOnlyAir");
        restartResetTimer();
    }

    /**
     * This initializer should be used to create an empty mine to be configured manually
     *
     * @param plugin The LapisMines plugin instance
     * @param name   The name of the mine
     * @param l1     The maximum point of the mine
     * @param l2     The minimum point of the mine
     */
    public Mine(LapisMine plugin, String name, Location l1, Location l2) {
        this(plugin, name, l1, l2, new Composition(plugin), null, 15, false);
    }

    /**
     * Sets the reset timer back to the start if it was already running or simply starts it if it wasn't running
     */
    public void restartResetTimer() {
        if (resetTask != null) {
            //Remove the old task, this is so that the mine reset timer can be reset if the mine is manually reset
            plugin.tasks.removeTask(resetTask);
            resetTask.cancel();
        }
        //Make the new task and register it with LapisTaskHandler to make sure it gets shutdown on disable
        resetTask = Bukkit.getScheduler().runTaskTimer(plugin, this::resetMine, resetFrequency * 20 * 60, resetFrequency * 20 * 60);
        plugin.tasks.addTask(resetTask);
        //Reset the warning tasks too
        resetWarningTasks();
    }

    public void resetWarningTasks() {
        //Cancel tasks currently scheduled
        if (warningTask != null) {
            plugin.tasks.removeTask(warningTask);
            warningTask.cancel();
        }
        if (startCountdownTask != null) {
            plugin.tasks.removeTask(startCountdownTask);
            startCountdownTask.cancel();
        }
        if (updateCountdownTask != null) {
            //This task is self canceling, so we don't cancel it, just remove it
            plugin.tasks.removeTask(updateCountdownTask);
        }
        //If warnings are enabled, schedule that here, take the reset frequency and subtract the warning time from it
        long resetDelay = resetFrequency * 20 * 60;
        //Warning time is in seconds, convert to ticks and subtract from resetDelay
        long warningDelay = resetDelay - (plugin.getConfig().getLong("WarningTime") * 20);
        //CountdownTime is also in seconds
        long countdownStartDelay = resetDelay - (plugin.getConfig().getLong("CountdownTime") * 20);
        //Reset delay will be equal to warning delay if warning is disabled
        if (warningDelay != resetDelay) {
            warningTask = Bukkit.getScheduler().runTaskLater(plugin, this::warnMineReset, warningDelay);
            plugin.tasks.addTask(warningTask);
        }
        //Check its enabled
        if (countdownStartDelay != resetDelay) {
            //Schedule the other task when the countdown should start
            startCountdownTask = Bukkit.getScheduler().runTaskLater(plugin, this::startCountdownTask, countdownStartDelay);
            plugin.tasks.addTask(startCountdownTask);
        }
    }

    public void warnMineReset() {
        String warningType = plugin.getConfig().getString("WarningType", "ActionBar");
        String warningMessageTemplate = plugin.config.getMessage("Reset.Warning");
        //Get the reset epoch
        int secondsBeforeReset = plugin.getConfig().getInt("WarningTime");
        long resetEpoch = System.currentTimeMillis() + (secondsBeforeReset * 1000L);
        String warningMessage = warningMessageTemplate.replace("%TimeUntilReset%",
                plugin.prettyTime.getCleanTimeDifference(resetEpoch, 1));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isPlayerInMine(p))
                continue;
            if (warningType.equalsIgnoreCase("Title")) {
                p.sendTitle(plugin.config.getMessage("Reset.WarningTitle"), warningMessage, 10, 60, 10);
            } else if (warningType.equalsIgnoreCase("ActionBar")) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(warningMessage));
            } else if (warningType.equalsIgnoreCase("Chat")) {
                p.sendMessage(warningMessage);
            }
        }
    }

    public void startCountdownTask() {
        //Get the message before text replacement
        String messageTemplate = plugin.config.getMessage("Reset.Countdown");
        String countdownType = plugin.getConfig().getString("CountdownType", "BossBar");
        //Seconds remaining to be decremented
        int totalSeconds = plugin.getConfig().getInt("CountdownTime");
        AtomicInteger secondsRemaining = new AtomicInteger(totalSeconds);
        BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        //Schedule to run every second to update the countdown
        updateCountdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            //PrettyTime replacement
            long resetEpoch = System.currentTimeMillis() + (secondsRemaining.get() * 1000L);
            String countdownMessage = messageTemplate.replace("%TimeUntilReset%",
                    plugin.prettyTime.getCleanTimeDifference(resetEpoch, 1));
            if (countdownType.equalsIgnoreCase("BossBar")) {
                //Update boss bar text
                bossBar.setTitle(countdownMessage);
                //Update progress
                double progress = Math.min(1, Math.max(0, (double) secondsRemaining.get() / totalSeconds));
                bossBar.setProgress(progress);
                //Make sure its visible
                if (!bossBar.isVisible())
                    bossBar.setVisible(true);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isPlayerInMine(p)) {
                    if (countdownType.equalsIgnoreCase("BossBar")) {
                        //Check this player can see the boss bar
                        if (!bossBar.getPlayers().contains(p))
                            bossBar.addPlayer(p);
                    } else if (countdownType.equalsIgnoreCase("ActionBar")) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(countdownMessage));
                    }
                } else {
                    if (countdownType.equalsIgnoreCase("BossBar")) {
                        //Check if this player is shown the boss bar, if so, remove them
                        if (bossBar.getPlayers().contains(p))
                            bossBar.removePlayer(p);
                    }
                }
            }
            secondsRemaining.getAndDecrement();
            //Cancel the task and hide the boss bar when we have gone below 0
            if (secondsRemaining.get() < 0) {
                bossBar.setVisible(false);
                updateCountdownTask.cancel();
            }
        }, 0, 20);
    }

    /**
     * Reset the mine
     *
     * @return true if the mine resets, false if the composition is not complete
     */
    public boolean resetMine() {
        //Check if the mine is able to reset
        if (!composition.isValidComposition())
            //Don't run if the composition isn't valid
            return false;
        //Teleport Players in mine and send them a message
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInMine(p)) {
                p.teleport(teleport);
                p.sendMessage(plugin.config.getMessage("Reset.Teleport"));
            }
        }
        regenerateMine();
        resetWarningTasks();
        return true;
    }

    /**
     * Sets up the mine to be deleted, this includes removing its config file and disabling the reset task
     * This won't remove it from the mines list in the main class, call plugin#removeMine(Mine) for that
     */
    public void deleteMine() {
        //Cancel the reset task
        resetTask.cancel();
        //Delete the mines config data so that it won't be created again on reload
        File f = new File(plugin.getDataFolder(), "Mines" + File.separator + name + ".yml");
        if (!f.delete())
            plugin.getLogger().warning("Unable to delete mine YAML file for " + name);
    }

    /**
     * Check if a players current position is within this mine
     *
     * @param p The player to check
     * @return True if the players location is within the bounds of the mine, otherwise false
     */
    public boolean isPlayerInMine(Player p) {
        MineBounds bounds = new MineBounds(this);
        Location pLoc = p.getLocation().getBlock().getLocation();
        if (pLoc.getX() <= bounds.xMax && pLoc.getX() >= bounds.xMin) {
            if (pLoc.getZ() <= bounds.zMax && pLoc.getZ() >= bounds.zMin) {
                return pLoc.getY() <= bounds.yMax && pLoc.getY() >= bounds.yMin;
            }
        }
        return false;
    }

    /**
     * Regenerate the blocks within the mine based on the current composition
     * <p>
     * WARNING: This only updates the blocks, it doesn't teleport players or send them a message
     */
    private void regenerateMine() {
        MineBounds bounds = new MineBounds(this);
        for (int x = bounds.xMin; x <= bounds.xMax; x++) {
            for (int y = bounds.yMin; y <= bounds.yMax; y++) {
                for (int z = bounds.zMin; z <= bounds.zMax; z++) {
                    if (replaceOnlyAir) {
                        //Get the block and check if it is air
                        Block b = getBlockAt(x, y, z);
                        if (b == null || !b.getType().isAir()) {
                            //If the block isn't air, then we skip it
                            continue;
                        }
                    }
                    if (y == bounds.yMax && surface != null && surface != Material.AIR) {
                        //yMin is the top layer, if we have a surface set then this whole layer should be set as it
                        setBlock(x, y, z, surface);
                    } else {
                        //Not the surface, lets calculate a block to place
                        setBlock(x, y, z, composition.getNextMaterial());
                    }
                }
            }
        }
    }

    private void setBlock(int x, int y, int z, Material mat) {
        //Could implement some kind of queue system here if the need ever arises
        Block b = getBlockAt(x, y, z);
        if (b == null)
            return;
        b.setType(mat, false);
    }

    private Block getBlockAt(int x, int y, int z) {
        World w = l1.getWorld();
        if (w == null)
            return null;
        return w.getBlockAt(x, y, z);
    }

    /**
     * Save the mines information to a YamlConfig file
     *
     * @param config The YamlConfig file to save too
     */
    public void saveMine(YamlConfiguration config) {
        config.set("Name", name);
        config.set("Locations.teleport", locationUtils.parseLocationToString(teleport));
        config.set("Locations.l1", locationUtils.parseLocationToString(l1));
        config.set("Locations.l2", locationUtils.parseLocationToString(l2));
        if (surface != null)
            config.set("Surface", surface.name());
        else
            config.set("Surface", null);
        config.set("Composition", composition.parseToStringList());
        config.set("ResetFrequency", resetFrequency);
        try {
            File f = new File(plugin.getDataFolder(), "Mines" + File.separator + name + ".yml");
            config.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the composition of this mine
     *
     * @return the current composition object
     */
    public Composition getComposition() {
        return composition;
    }

    /**
     * Sets the surface block for the mine
     *
     * @param mat a material to use for the top layer of the mine, must be a block
     */
    public void setSurface(Material mat) {
        if (mat == null) {
            this.surface = null;
            return;
        }
        if (!mat.isBlock())
            return;
        this.surface = mat;
    }

    /**
     * Get the reset frequency of this mine
     *
     * @return the number of minutes between resets
     */
    public int getResetFrequency() {
        return resetFrequency;
    }

    /**
     * Set the reset frequency of this mine
     *
     * @param minutes the number of minutes between each reset
     */
    public void setResetFrequency(int minutes) {
        this.resetFrequency = minutes;
    }

    /**
     * Get the name of the mine
     *
     * @return the name of the mine
     */
    public String getName() {
        return name;
    }

    /**
     * Get the teleport location for this mine, if it hasn't been manually set it will be the top middle of the mine
     *
     * @return the teleport location for this mine
     */
    public Location getTeleport() {
        return teleport;
    }

    /**
     * Set the teleport location for this mine
     *
     * @param loc the teleport location
     */
    public void setTeleport(Location loc) {
        this.teleport = loc;
    }

    /**
     * Get the l1 location, this is the maximum point of the mine
     *
     * @return the l1 location
     */
    public Location getL1() {
        return l1;
    }

    /**
     * Set the l1 location for this mine, it should be the maximum point for the mine.
     *
     * @param loc the new l1 location
     */
    public void setL1(Location loc) {
        this.l1 = loc;
    }

    /**
     * Get the l2 location, this is the minimum point of the mine
     *
     * @return the l2 location
     */
    public Location getL2() {
        return l2;
    }

    /**
     * Set the l2 location for this mine, it should be the minimum point for the mine.
     *
     * @param loc the new l2 location
     */
    public void setL2(Location loc) {
        this.l2 = loc;
    }

    /**
     * Set replace only air. when true, only air blocks will be changed during a mine reset
     *
     * @param replaceOnlyAir true to only replace air blocks
     */
    public void setReplaceOnlyAir(boolean replaceOnlyAir) {
        this.replaceOnlyAir = replaceOnlyAir;
    }
}
