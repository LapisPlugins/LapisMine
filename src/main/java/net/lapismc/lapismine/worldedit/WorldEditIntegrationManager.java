package net.lapismc.lapismine.worldedit;

import net.lapismc.lapismine.LapisMine;
import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getServer;

public class WorldEditIntegrationManager {

    private WorldEdit worldEdit;

    public WorldEditIntegrationManager(LapisMine plugin) {
        //Check which world edit is installed
        boolean isWorldEditInstalled = getServer().getPluginManager().isPluginEnabled("WorldEdit");
        boolean isFAWEInstalled = getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
        if (isWorldEditInstalled || isFAWEInstalled) {
            worldEdit = new WorldEdit(isFAWEInstalled);
        } else {
            plugin.getLogger().severe("WorldEdit or FAWE not installed." +
                    " You must install wither world edit or fast async world edit");
            plugin.getLogger().warning("Shutting down plugin since dependencies are not met");
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
        }
    }

    public WorldEdit getWorldEdit() {
        return worldEdit;
    }

}
