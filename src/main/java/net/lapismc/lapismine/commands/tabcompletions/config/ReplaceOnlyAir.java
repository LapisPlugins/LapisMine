package net.lapismc.lapismine.commands.tabcompletions.config;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReplaceOnlyAir implements LapisTabOption {

    @Override
    public List<String> getOptions(CommandSender sender) {
        return Collections.singletonList("replaceOnlyAir");
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        List<LapisTabOption> children = new ArrayList<>();
        //True
        children.add(new LapisTabOption() {
            @Override
            public List<String> getOptions(CommandSender sender) {
                return Collections.singletonList("true");
            }

            @Override
            public List<LapisTabOption> getChildren(CommandSender sender) {
                return null;
            }
        });
        //False
        children.add(new LapisTabOption() {
            @Override
            public List<String> getOptions(CommandSender sender) {
                return Collections.singletonList("false");
            }

            @Override
            public List<LapisTabOption> getChildren(CommandSender sender) {
                return null;
            }
        });
        return children;
    }

}
