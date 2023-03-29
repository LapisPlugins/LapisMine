package net.lapismc.lapismine.commands.tabcompletions;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import net.lapismc.lapismine.LapisMine;
import net.lapismc.lapismine.commands.tabcompletions.composition.Composition;
import net.lapismc.lapismine.commands.tabcompletions.config.Config;
import net.lapismc.lapismine.mines.Mine;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class MineName implements LapisTabOption {

    private final LapisMine plugin;
    private final boolean hasChildren;

    public MineName(boolean hasChildren) {
        this.plugin = LapisMine.getInstance();
        this.hasChildren = hasChildren;
    }

    @Override
    public List<String> getOptions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        for (Mine m : plugin.getMines()) {
            options.add(m.getName());
        }
        return options;
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        if (!hasChildren)
            return null;
        List<LapisTabOption> children = new ArrayList<>();
        children.add(new Config());
        children.add(new Composition());
        children.add(new Reset());
        return children;
    }
}
