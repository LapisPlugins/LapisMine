package net.lapismc.lapismine.commands.tabcompletions;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class Remove implements LapisTabOption {
    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("remove");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        return Collections.singletonList(new MineName(false));
    }
}
