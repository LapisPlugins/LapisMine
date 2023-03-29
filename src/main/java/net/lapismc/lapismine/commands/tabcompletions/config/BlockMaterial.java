package net.lapismc.lapismine.commands.tabcompletions.config;

import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockMaterial implements LapisTabOption {

    private final boolean hasPercentChild;

    public BlockMaterial(boolean hasPercentChild) {
        this.hasPercentChild = hasPercentChild;
    }

    @Override
    public List<String> getOptions(CommandSender sender) {
        List<String> materials = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.isBlock())
                materials.add(m.name());
        }
        return materials;
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        if (!hasPercentChild)
            return null;
        //Percent child
        return Collections.singletonList(new LapisTabOption() {

            @Override
            public List<String> getOptions(CommandSender sender) {
                return Collections.singletonList("(Percent 0.0-1.0)");
            }

            @Override
            public List<LapisTabOption> getChildren(CommandSender sender) {
                return null;
            }
        });
    }
}
