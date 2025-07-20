/*
 * Copyright 2025 Benjamin Martin
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

package net.lapismc.lapismine.worldedit;

import net.lapismc.lapismine.LapisMine;
import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getServer;

public class WorldEditIntegrationManager {

    private WorldEdit worldEdit;

    public WorldEditIntegrationManager(LapisMine plugin) {
        //Check which world edit is installed
        boolean isWorldEditInstalled = getServer().getPluginManager().isPluginEnabled("WorldEdit");
        boolean isFAWEInstalled = getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
        if (isWorldEditInstalled || isFAWEInstalled) {
            worldEdit = new WorldEdit(isFAWEInstalled);
        } else {
            plugin.getLogger().severe("WorldEdit or FAWE not installed." +
                    " You must install wither world edit or fast async world edit");
            plugin.getLogger().warning("Shutting down plugin since dependencies are not met");
            plugin.tasks.runTask(() -> Bukkit.getPluginManager().disablePlugin(plugin), false);
        }
    }

    public WorldEdit getWorldEdit() {
        return worldEdit;
    }

}
