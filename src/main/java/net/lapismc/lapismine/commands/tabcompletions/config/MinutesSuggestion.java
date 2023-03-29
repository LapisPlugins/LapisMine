package net.lapismc.lapismine.commands.tabcompletions.config;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class MinutesSuggestion implements LapisTabOption {
    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("(Minutes)");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        return null;
    }
}
