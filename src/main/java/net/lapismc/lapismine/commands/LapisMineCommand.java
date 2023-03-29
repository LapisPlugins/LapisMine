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
import java.util.Map;

public class LapisMineCommand extends LapisCoreCommand {

    private final LapisMine plugin;

    public LapisMineCommand(LapisMine plugin) {
        super(plugin, "lapismine", "Manage your mines", Collections.singletonList("mine"), true);
        this.plugin = plugin;
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
                    sendMessage(sender, "Create.Success");
                } else {
                    sendMessage(sender, "Create.Help");
                    return;
                }
                return;
            }

            //mine remove (name)
            if (args[0].equalsIgnoreCase("remove")) {
                String name = args[1];
                Mine m = plugin.getMine(name);
                if (m == null) {
                    sendMessage(sender, "Error.NoSuchMine");
                    return;
                }
                m.deleteMine();
                plugin.removeMine(m);
                return;
            }

            //mine (name)
            if (args.length == 1) {
                sendMessage(sender, "Help", "Reset.Help", "Config.Help", "Composition.Help");
                return;
            }

            //mine (name) composition
            if (args[1].equalsIgnoreCase("composition")) {
                if (args.length > 2) {
                    composition(sender, args, p);
                } else {
                    sendMessage(sender, "Composition.Help");
                }
                return;
            }

            //mine (name) reset
            if (args[1].equalsIgnoreCase("reset")) {
                if (args.length == 2) {
                    resetMine(sender, args[0]);
                } else {
                    sendMessage(sender, "Reset.Help");
                }
                return;
            }
            //mine (mineName) config (settingName) (settingValue)
            if (args[1].equalsIgnoreCase("config")) {
                if (args.length == 4) {
                    String mineName = args[0];
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
                                    sendMessage(sender, "Error.MaterialNotFound");
                                    return;
                                }
                                m.setSurface(mat);
                                sendMessage(sender, "Config.Surface.Success");
                                plugin.saveMines();
                            }
                            break;
                        case "resetfrequency":
                            int i;
                            try {
                                i = Integer.parseInt(settingValue);
                            } catch (NumberFormatException e) {
                                sendMessage(sender, "Config.ResetFrequency.NotInt");
                                return;
                            }
                            m.setResetFrequency(i);
                            sendMessage(sender, "Config.ResetFrequency.Success");
                            plugin.saveMines();
                            break;
                        case "teleport":
                            m.setTeleport(p.getLocation());
                            plugin.saveMines();
                            sendMessage(sender, "Config.TeleportSuccess");
                            break;
                        case "replaceonlyair":
                            if (settingValue.equalsIgnoreCase("true")) {
                                m.setReplaceOnlyAir(true);
                                sendMessage(sender, "Config.ReplaceOnlyAir.True");
                            } else if (settingValue.equalsIgnoreCase("false")) {
                                m.setReplaceOnlyAir(false);
                                sendMessage(sender, "Config.ReplaceOnlyAir.False");
                            } else {
                                sendMessage(sender, "Config.ReplaceOnlyAir.NotBoolean");
                            }
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

    private void composition(CommandSender sender, String[] args, Player p) {
        //mine mine composition set MATERIAL PERCENT
        //mine mine composition remove MATERIAL
        //mine mine composition status
        //mine mine composition fill
        String name = args[0];
        Mine m = plugin.getMine(name);
        if (m == null) {
            sendMessage(sender, "Error.NoSuchMine");
            return;
        }
        String command = args[2];
        if (command.equalsIgnoreCase("status")) {
            sendMessage(sender, "Composition.Status");
            Map<Material, Double> matMap = m.getComposition().getMaterialMap();
            for (Material mat : matMap.keySet()) {
                Double d = matMap.get(mat);
                p.sendMessage(mat.name() + " - " + d);
            }
            sendMessage(sender, "Composition.RemainingPercentage");
            p.sendMessage(String.valueOf(m.getComposition().getUnassignedPercentage()));

        } else if (command.equalsIgnoreCase("remove")) {
            if (args.length != 4) {
                sendMessage(sender, "Composition.Remove.Help");
                return;
            }
            Material mat = Material.matchMaterial(args[3]);
            if (mat == null) {
                sendMessage(sender, "Error.MaterialNotFound");
                return;
            }
            if (m.getComposition().removeMaterial(mat)) {
                sendMessage(sender, "Composition.Remove.Success");
            } else {
                sendMessage(sender, "Composition.Remove.MaterialNotPresent");
            }
            sendMessage(sender, "Composition.RemainingPercentage");
            p.sendMessage(String.valueOf(m.getComposition().getUnassignedPercentage()));
            //Save these changes to the mine
            plugin.saveMines();

        } else if (command.equalsIgnoreCase("set")) {
            if (args.length != 5) {
                sendMessage(sender, "Composition.Set.Help");
                return;
            }
            Material mat = Material.matchMaterial(args[3]);
            if (mat == null) {
                sendMessage(sender, "Error.MaterialNotFound");
                return;
            }
            if (!mat.isBlock()) {
                sendMessage(sender, "Composition.Set.NotABlock");
                return;
            }
            String percentageString = args[4];
            double percentage;
            try {
                percentage = Double.parseDouble(percentageString);
            } catch (NumberFormatException e) {
                sendMessage(sender, "Composition.Set.InvalidPercentage");
                return;
            }
            if (m.getComposition().setMaterial(mat, percentage)) {
                sendMessage(sender, "Composition.Set.Success");
            } else {
                sendMessage(sender, "Composition.Set.PercentageTooHigh");
            }
            sendMessage(sender, "Composition.RemainingPercentage");
            p.sendMessage(String.valueOf(m.getComposition().getUnassignedPercentage()));
            //Save the mine to keep these changes
            plugin.saveMines();

        } else if (command.equalsIgnoreCase("fill")) {
            m.getComposition().fillMaterial(plugin.fillMaterial);
            sendMessage(sender, "Composition.Fill");
        }
    }

    private void resetMine(CommandSender sender, String name) {
        Mine m = plugin.getMine(name);
        if (m == null) {
            sendMessage(sender, "Error.NoSuchMine");
            return;
        }
        if (m.resetMine()) {
            m.restartResetTimer();
            sendMessage(sender, "Reset.Success");
        } else {
            sendMessage(sender, "Reset.CompositionIncomplete");
        }
    }
}
