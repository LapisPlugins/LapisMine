package net.lapismc.lapismine.commands.tabcompletions.config;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Config implements LapisTabOption {
    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("config");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        List<LapisTabOption> children = new ArrayList<>();
        children.add(new Surface());
        children.add(new ResetFrequency());
        children.add(new ReplaceOnlyAir());
        children.add(new Teleport());
        return children;
    }
}
