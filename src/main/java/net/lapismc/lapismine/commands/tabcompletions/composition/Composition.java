package net.lapismc.lapismine.commands.tabcompletions.composition;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Composition implements LapisTabOption {
    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("composition");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        List<LapisTabOption> children = new ArrayList<>();
        children.add(new Set());
        children.add(new Remove());
        children.add(new Status());
        children.add(new Fill());
        return children;
    }
}
