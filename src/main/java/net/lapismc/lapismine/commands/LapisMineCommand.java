package net.lapismc.lapismine.commands;

import net.lapismc.lapiscore.commands.LapisCoreCommand;
import net.lapismc.lapismine.LapisMine;
import net.lapismc.lapismine.mines.Mine;
import net.lapismc.lapismine.worldedit.WorldEdit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class LapisMineCommand extends LapisCoreCommand {

    private LapisMine plugin;

    public LapisMineCommand(LapisMine plugin) {
        super(plugin, "lapismine", "Manage your mines", Collections.singletonList("mine"), true);
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (isNotPlayer(sender, "Error.MustBePlayer"))
            return;

        Player p = (Player) sender;
        if (args.length > 0) {
            //mine create (name)
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 2) {
                    String name = args[1];
                    if (plugin.getMine(name) != null) {
                        sendMessage(sender, "Create.MineExists");
                        return;
                    }
                    WorldEdit we = plugin.worldEditManager.getWorldEdit();
                    if (!we.doesPlayerHaveSelection(p)) {
                        sendMessage(sender, "Create.NoRegion");
                        return;
                    }
                    Location l1 = we.getL1(p);
                    Location l2 = we.getL2(p);
                    plugin.createMine(name, l1, l2);
                } else {
                    sendMessage(sender, "Create.Help");
                    return;
                }
                return;
            }
            //mine reset (mineName)
            if (args[0].equalsIgnoreCase("reset")) {
                if (args.length == 2) {
                    String name = args[1];
                    Mine m = plugin.getMine(name);
                    if (m == null) {
                        sendMessage(sender, "Error.NoSuchMine");
                        return;
                    }
                    m.resetMine();
                    m.startResetTimer();
                    sendMessage(sender, "Reset.Success");
                } else {
                    sendMessage(sender, "Reset.Help");
                }
                return;
            }
            //mine config (mineName) (settingName) (settingValue)
            if (args[0].equalsIgnoreCase("config")) {
                if (args.length == 4) {
                    String mineName = args[1];
                    String settingName = args[2];
                    String settingValue = args[3];
                    Mine m = plugin.getMine(mineName);
                    if (m == null) {
                        sendMessage(sender, "Error.NoSuchMine");
                        return;
                    }
                    switch (settingName.toLowerCase()) {
                        case "surface":
                            if (settingValue.equalsIgnoreCase("none")) {
                                m.setSurface(null);
                            } else {
                                Material mat = Material.matchMaterial(settingValue);
                                if (mat == null) {
                                    sendMessage(sender, "Config.Surface.MaterialNotFound");
                                    return;
                                }
                                m.setSurface(mat);
                                sendMessage(sender, "Config.Surface.Success");
                            }
                            break;
                        case "resetfrequency":
                            int i;
                            try {
                                i = Integer.valueOf(settingValue);
                            } catch (NumberFormatException e) {
                                sendMessage(sender, "Config.ResetFrequency.NotInt");
                                return;
                            }
                            m.setResetFrequency(i);
                            sendMessage(sender, "Config.ResetFrequency.Success");
                            break;
                        default:
                            sendMessage(sender, "Config.NoSuchSetting");
                    }
                } else {
                    sendMessage(sender, "Config.Help");
                }
            }
        }
    }
}
