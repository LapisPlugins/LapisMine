/*
 * Copyright 2024 Benjamin Martin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.lapismc.lapismine;

import net.lapismc.lapiscore.utils.LapisCoreFileWatcher;
import net.lapismc.lapismine.mines.Mine;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LapisMineFileWatcher extends LapisCoreFileWatcher {

    private final LapisMine plugin;

    /**
     * Start the file watcher
     *
     * @param plugin The LapisCorePlugin that the file watcher should be registered to
     */
    public LapisMineFileWatcher(LapisMine plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void checkOtherFile(File f) {
        //Replace the file with the fully qualified file
        f = new File(plugin.getDataFolder().getAbsolutePath(), f.getPath());
        if (!f.getParentFile().getName().contains("Mines"))
            return;
        //We now know that it is one of our mine config files that has been edited
        //Load the Yaml
        YamlConfiguration mineYaml = YamlConfiguration.loadConfiguration(f);
        String mineName = mineYaml.getString("Name");
        Mine toReload = plugin.getMine(mineName);
        if (toReload == null) {
            plugin.getLogger().warning("Tried to reload " + mineName + " from an edited Yaml file, but couldn't find a mine of that name");
            return;
        }
        //Shutdown and register the mine
        toReload.shutdownMine();
        plugin.removeMine(toReload);
        //Load the new mine
        Mine newMine = new Mine(plugin, mineYaml);
        plugin.addMine(newMine);
        //Notify console that it was loaded
        plugin.getLogger().info("Changes made to mine \"" + newMine.getName() + "\" have been loaded!");
    }

}
