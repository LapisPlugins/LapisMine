package net.lapismc.lapismine.mines;

import net.lapismc.lapiscore.utils.LocationUtils;
import net.lapismc.lapismine.LapisMine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

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

    public Mine(LapisMine plugin, String name, Location l1, Location l2, Composition composition, Material surface) {
        this.plugin = plugin;
        this.name = name;
        //TODO: Calculate default teleport location
        this.l1 = l1;
        this.l2 = l2;
        this.composition = composition;
        this.surface = surface;
    }

    public Mine(LapisMine plugin, YamlConfiguration config) {
        this.plugin = plugin;
        name = config.getString("Name");
        teleport = locationUtils.parseStringToLocation(config.getString("Locations.teleport"));
        l1 = locationUtils.parseStringToLocation(config.getString("Locations.l1"));
        l2 = locationUtils.parseStringToLocation(config.getString("Locations.l2"));
        composition = new Composition(plugin, config.getStringList("Composition"));
        surface = Material.getMaterial(config.getString("Surface", ""));
    }

    public Mine(LapisMine plugin, String name, Location l1, Location l2) {
        this(plugin, name, l1, l2, new Composition(plugin), null);
    }

    /**
     * Regenerate the blocks within the mine based on the current composition
     */
    public void regenerateMine() {
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
