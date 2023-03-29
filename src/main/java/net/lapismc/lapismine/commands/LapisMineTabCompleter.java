package net.lapismc.lapismine.commands;

import net.lapismc.lapiscore.commands.tabcomplete.LapisCoreTabCompleter;
import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import net.lapismc.lapismine.commands.tabcompletions.Create;
import net.lapismc.lapismine.commands.tabcompletions.MineName;
import net.lapismc.lapismine.commands.tabcompletions.Remove;

import java.util.ArrayList;
import java.util.List;

public class LapisMineTabCompleter extends LapisCoreTabCompleter {

    private final LapisMineCommand command;

    public LapisMineTabCompleter(LapisMineCommand command) {
        this.command = command;
        buildTabCompletions();
    }

    private void buildTabCompletions() {
        List<LapisTabOption> topLevelOptions = new ArrayList<>();
        topLevelOptions.add(new Create());
        topLevelOptions.add(new Remove());
        topLevelOptions.add(new MineName(true));
        registerTopLevelOptions(this.command, topLevelOptions);
    }


}
