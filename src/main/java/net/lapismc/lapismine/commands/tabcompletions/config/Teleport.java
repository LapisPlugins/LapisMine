package net.lapismc.lapismine.commands.tabcompletions.config;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class Teleport implements LapisTabOption {

    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("teleport");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        //Here
        return Collections.singletonList(new LapisTabOption() {

            @Override
            public List<String> getOptions(CommandSender sender) {
                return Collections.singletonList("here");
            }

            @Override
            public List<LapisTabOption> getChildren(CommandSender sender) {
                return null;
            }
        });
    }
}