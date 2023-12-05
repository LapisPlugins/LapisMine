/*
 * Copyright 2023 Benjamin Martin
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

package net.lapismc.lapismine.mines;

import org.bukkit.Location;

public class MineBounds {

    public int xMax, xMin, yMax, yMin, zMax, zMin;

    public MineBounds(Mine mine) {
        Location l1 = mine.getL1();
        Location l2 = mine.getL2();
        xMax = Math.max(l1.getBlockX(), l2.getBlockX());
        xMin = Math.min(l1.getBlockX(), l2.getBlockX());
        yMax = Math.max(l1.getBlockY(), l2.getBlockY());
        yMin = Math.min(l1.getBlockY(), l2.getBlockY());
        zMax = Math.max(l1.getBlockZ(), l2.getBlockZ());
        zMin = Math.min(l1.getBlockZ(), l2.getBlockZ());
    }

}
