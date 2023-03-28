package net.lapismc.lapismine;

import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapismine.commands.LapisMineCommand;
import net.lapismc.lapismine.mines.Mine;
import net.lapismc.lapismine.worldedit.WorldEditIntegrationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LapisMine extends LapisCorePlugin {

    public Material fillMaterial;
    public WorldEditIntegrationManager worldEditManager;
    private final List<Mine> mines = new ArrayList<>();

    @Override
    public void onEnable() {
        config = new LapisCoreConfiguration(this, 1, 1);
        fillMaterial = Material.getMaterial(getConfig().getString("FillMaterial", "STONE"));
        worldEditManager = new WorldEditIntegrationManager(this);
        loadMines();
        new LapisMineCommand(this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        saveMines();
        super.onDisable();
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
     * Simply removes the mine from the stored list of mines in the main class. Make sure you call deleteMine on the mine class first
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

    private void loadMines() {
        File minesFolder = new File(getDataFolder(), "Mines");
        if (!minesFolder.exists())
            minesFolder.mkdir();
        //TODO: Add message for how many mines get loaded
        for (File f : minesFolder.listFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            mines.add(new Mine(this, config));
        }
    }

    public void saveMines() {
        File minesFolder = new File(getDataFolder(), "Mines");
        for (Mine m : mines) {
            File f = new File(minesFolder, m.getName() + ".yml");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            m.saveMine(config);
        }
    }

}
