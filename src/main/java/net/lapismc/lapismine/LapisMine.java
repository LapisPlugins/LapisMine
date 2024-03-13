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

package net.lapismc.lapismine;

import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapiscore.utils.PrettyTimeUtil;
import net.lapismc.lapismine.commands.LapisMineCommand;
import net.lapismc.lapismine.mines.Mine;
import net.lapismc.lapismine.worldedit.WorldEditIntegrationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A plugin to manage automatically resetting mines
 */
public final class LapisMine extends LapisCorePlugin implements Listener {

    private static LapisMine instance;
    /**
     * The material that should be used to complete compositions
     */
    public Material fillMaterial;
    /**
     * A utility class for accessing a players world edit selection
     */
    public WorldEditIntegrationManager worldEditManager;
    /**
     * A utility class for formatting time differences as Strings
     */
    public PrettyTimeUtil prettyTime;
    private final List<Mine> mines = new ArrayList<>();

    /**
     * Get an instance of this class
     *
     * @return the currently running instance of this class
     */
    public static LapisMine getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        LapisMine.instance = this;
        config = new LapisCoreConfiguration(this, 1, 1);
        new LapisMineFileWatcher(this);
        fillMaterial = Material.getMaterial(getConfig().getString("FillMaterial", "STONE"));
        worldEditManager = new WorldEditIntegrationManager(this);
        prettyTime = new PrettyTimeUtil();
        loadMines();
        new LapisMineCommand(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        saveMines();
        super.onDisable();
    }

    /**
     * Checking for joining players who are in a mine and should be teleported out because it reset while they were offline
     *
     * @param e The player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        //Check if the player is within the bounds of a mine
        boolean isPlayerInMine = false;
        Mine mine = null;
        for (Mine m : mines) {
            if (m.isPlayerInMine(p)) {
                isPlayerInMine = true;
                mine = m;
                break;
            }
        }
        //Don't continue if they aren't in a mine
        if (!isPlayerInMine)
            return;
        //Get the times for when they were last online and when the mine last reset
        long lastPlayed = p.getLastPlayed();
        long lastReset = mine.getLastReset();
        //If the mine reset is more recent, they were in the mine before it reset
        if (lastPlayed < lastReset) {
            //Teleport them out to the teleport point and let them know it was reset while they were offline
            p.teleport(mine.getTeleport());
            p.sendMessage(config.getMessage("Reset.LoginTeleport"));
        }
    }

    /**
     * Creates an empty mine object with the given points and name, will not proceed if there is already a mine with this name
     *
     * @param name The name of the mine
     * @param l1   The maximum point of the mine
     * @param l2   the minimum point of the mine
     */
    public void createMine(String name, Location l1, Location l2) {
        if (getMine(name) != null)
            return;
        Mine m = new Mine(this, name, l1, l2);
        mines.add(m);
        saveMines();
    }

    /**
     * Add a mine to the stored list of mines, only use this if you have initialized a mine yourself
     *
     * @param mine The mine to be tracked
     */
    public void addMine(Mine mine) {
        mines.add(mine);
    }

    /**
     * Simply removes the mine from the stored list of mines in the main class.
     * Make sure you call deleteMine on the mine class first if you are trying to delete it
     *
     * @param m The mine to remove
     */
    public void removeMine(Mine m) {
        mines.remove(m);
    }

    /**
     * Get a mine using the mines name
     *
     * @param name The name of the mine you wish to fetch
     * @return the Mine object for the given mine name, null if there is no Mine for that name
     */
    public Mine getMine(String name) {
        for (Mine m : mines) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get a list of all mines
     *
     * @return a list containing all mines
     */
    public List<Mine> getMines() {
        return mines;
    }


    private void loadMines() {
        File minesFolder = new File(getDataFolder(), "Mines");
        if (!minesFolder.exists())
            if (!minesFolder.mkdir())
                return;
        File[] mineFiles = minesFolder.listFiles();
        if (mineFiles == null)
            return;
        for (File f : mineFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            mines.add(new Mine(this, config));
        }
        getLogger().info("Loaded " + mines.size() + " mine(s)!");
    }

    /**
     * Save the mines current settings to its Yaml Configuration file
     * This can be safely called at any time without interrupting the operation of the mine
     */
    public void saveMines() {
        File minesFolder = new File(getDataFolder(), "Mines");
        for (Mine m : mines) {
            File f = new File(minesFolder, m.getName() + ".yml");
            if (!f.exists()) {
                try {
                    if (!f.createNewFile())
                        throw new IOException("Could not create file " + f.getName());
                } catch (IOException e) {
                    getLogger().warning("Failed to save file for " + m.getName());
                }
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            m.saveMine(config);
        }
    }

}
