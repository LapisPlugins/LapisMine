package net.lapismc.lapismine;

import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapismine.mines.Mine;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class LapisMine extends LapisCorePlugin {

    public Material fillMaterial;
    private final List<Mine> mines = new ArrayList<>();

    @Override
    public void onEnable() {
        config = new LapisCoreConfiguration(this, 1, 1);
        fillMaterial = Material.getMaterial(getConfig().getString("FillMaterial", "STONE"));
        //TODO: Load Mines
        File minesFolder = new File(getDataFolder(), "Mines");
        if (!minesFolder.exists())
            minesFolder.mkdir();
        for (File f : minesFolder.listFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            mines.add(new Mine(this, config));
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
