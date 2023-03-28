package net.lapismc.lapismine.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class WorldEdit {

    private final WorldEditPlugin worldEdit;

    public WorldEdit(boolean isFAWE) {
        if (isFAWE)
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
        else
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }


    public boolean doesPlayerHaveSelection(Player p) {
        LocalSession session = getSession(p);
        return session.isSelectionDefined(session.getSelectionWorld());
    }


    public Location getL1(Player p) {
        //Get the players session
        LocalSession session = getSession(p);
        BlockVector3 maxPoint;
        try {
            //Attempt to get the selected region
            maxPoint = session.getSelection().getMaximumPoint();
        } catch (IncompleteRegionException e) {
            //Region not completely selected, return null
            return null;
        }
        //Return the max point of the region
        return new Location(Bukkit.getWorld(session.getSelectionWorld().getName()),
                maxPoint.getBlockX(), maxPoint.getBlockY(), maxPoint.getBlockZ());
    }


    public Location getL2(Player p) {
        //Get the players session
        LocalSession session = getSession(p);
        BlockVector3 minPoint;
        try {
            //Attempt to get the selected region
            minPoint = session.getSelection().getMinimumPoint();
        } catch (IncompleteRegionException e) {
            //Region not completely selected, return null
            return null;
        }
        //Return the min point of the region
        return new Location(Bukkit.getWorld(session.getSelectionWorld().getName()),
                minPoint.getBlockX(), minPoint.getBlockY(), minPoint.getBlockZ());
    }

    private LocalSession getSession(Player p) {
        return worldEdit.getSession(p);
    }
}
