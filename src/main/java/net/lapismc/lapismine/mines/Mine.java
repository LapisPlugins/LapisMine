package net.lapismc.lapismine.mines;

import net.lapismc.lapiscore.utils.LocationUtils;
import net.lapismc.lapismine.LapisMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;

/**
 * A class that represents a single mine instance
 */
public class Mine {

    private final LapisMine plugin;
    private final Composition composition;
    private final LocationUtils locationUtils = new LocationUtils();
    private String name;
    private Location teleport, l1, l2;
    private Material surface;
    private Integer resetFrequency;
    private BukkitTask resetTask;

    public Mine(LapisMine plugin, String name, Location l1, Location l2, Composition composition,
                Material surface, Integer resetFrequency) {
        this.plugin = plugin;
        this.name = name;
        //TODO: Calculate the middle at the top maybe?
        teleport = l1.clone().add(0, 2, 0);
        this.l1 = l1;
        this.l2 = l2;
        this.composition = composition;
        this.surface = surface;
        this.resetFrequency = resetFrequency;
        startResetTimer();
    }

    public Mine(LapisMine plugin, YamlConfiguration config) {
        this.plugin = plugin;
        name = config.getString("Name");
        teleport = locationUtils.parseStringToLocation(config.getString("Locations.teleport"));
        l1 = locationUtils.parseStringToLocation(config.getString("Locations.l1"));
        l2 = locationUtils.parseStringToLocation(config.getString("Locations.l2"));
        composition = new Composition(plugin, config.getStringList("Composition"));
        surface = Material.getMaterial(config.getString("Surface", ""));
        resetFrequency = config.getInt("ResetFrequency");
        startResetTimer();
    }

    public Mine(LapisMine plugin, String name, Location l1, Location l2) {
        this(plugin, name, l1, l2, new Composition(plugin), null, 15);
    }

    public void startResetTimer() {
        if (resetTask != null) {
            //Remove the old task, this is so that the mine reset timer can be reset if the mine is manually reset
            plugin.tasks.removeTask(resetTask);
            resetTask.cancel();
        }
        //Make the new task and register it with LapisTaskHandler to make sure it gets shutdown on disable
        resetTask = Bukkit.getScheduler().runTaskTimer(plugin, this::resetMine, resetFrequency * 20 * 60, resetFrequency * 20 * 60);
        plugin.tasks.addTask(resetTask);
    }

    public void resetMine() {
        //Check if the mine is able to reset
        if (!composition.isValidComposition())
            //Don't run if the composition isn't valid
            return;
        //Teleport Players in mine and send them a message
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInMine(p)) {
                p.teleport(teleport);
                p.sendMessage(plugin.config.getMessage("Reset.Teleport"));
            }
        }
        regenerateMine();
    }

    /**
     * Regenerate the blocks within the mine based on the current composition
     * <p>
     * WARNING: This only updates the block, it doesn't teleport players or send them a message
     */
    private void regenerateMine() {
        int xMax, xMin, yMax, yMin, zMax, zMin;
        xMax = Math.max(l1.getBlockX(), l2.getBlockX());
        xMin = Math.min(l1.getBlockX(), l2.getBlockX());
        yMax = Math.max(l1.getBlockY(), l2.getBlockY());
        yMin = Math.min(l1.getBlockY(), l2.getBlockY());
        zMax = Math.max(l1.getBlockZ(), l2.getBlockZ());
        zMin = Math.min(l1.getBlockZ(), l2.getBlockZ());
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    if (y == yMax && surface != null) {
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

    private boolean isPlayerInMine(Player p) {
        int xMax, xMin, yMax, yMin, zMax, zMin;
        xMax = Math.max(l1.getBlockX(), l2.getBlockX());
        xMin = Math.min(l1.getBlockX(), l2.getBlockX());
        yMax = Math.max(l1.getBlockY(), l2.getBlockY());
        yMin = Math.min(l1.getBlockY(), l2.getBlockY());
        zMax = Math.max(l1.getBlockZ(), l2.getBlockZ());
        zMin = Math.min(l1.getBlockZ(), l2.getBlockZ());
        Location pLoc = p.getLocation().getBlock().getLocation();
        if (pLoc.getX() < xMax && pLoc.getX() > xMin) {
            if (pLoc.getZ() < zMax && pLoc.getZ() > zMin) {
                return pLoc.getY() < yMax && pLoc.getY() > yMin;
            }
        }
        return false;
    }

    private void setBlock(int x, int y, int z, Material mat) {
        World w = l1.getWorld();
        w.getBlockAt(x, y, z).setType(mat);
    }

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

    public Composition getComposition() {
        return composition;
    }

    public void setSurface(Material mat) {
        this.surface = mat;
    }

    public int getResetFrequency() {
        return resetFrequency;
    }

    public void setResetFrequency(int minutes) {
        this.resetFrequency = minutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getTeleport() {
        return teleport;
    }

    public void setTeleport(Location loc) {
        this.teleport = loc;
    }

    public Location getL1() {
        return l1;
    }

    public void setL1(Location loc) {
        this.l1 = loc;
    }

    public Location getL2() {
        return l2;
    }

    public void setL2(Location loc) {
        this.l2 = loc;
    }

}
