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

package net.lapismc.lapismine.mines;

import net.lapismc.lapismine.LapisMine;
import org.bukkit.Material;

import java.util.*;

/**
 * Stores the composition of the mine and can provide a block stream to regen the mine
 */
public class Composition {

    private final LapisMine plugin;
    private final HashMap<Material, Double> materialMap;
    private RandomCollection<Material> compiledMaterials;

    /**
     * This initializer should be used when it is a new composition that is yet to be built
     *
     * @param plugin The LapisMine main class
     */
    public Composition(LapisMine plugin) {
        this.plugin = plugin;
        materialMap = new HashMap<>();
        compiledMaterials = null;
    }

    /**
     * This initializer should be used when loading from a mine config.
     *
     * @param plugin    The LapisMine main class
     * @param materials The Composition array list to be parsed
     */
    public Composition(LapisMine plugin, List<String> materials) {
        this.plugin = plugin;
        materialMap = new HashMap<>();
        for (String s : materials) {
            String[] data = s.split(":");
            Material mat = Material.getMaterial(data[0]);
            Double percentage = Double.valueOf(data[1]);
            materialMap.put(mat, percentage);
        }
        compileMaterials();
    }

    /**
     * Get the next material based on the set percentages
     * If percentages didn't total to 1 before this operation, the remaining space will be filled by stone
     *
     * @return the resulting material
     */
    public Material getNextMaterial() {
        if (getTotalPercentage() != 1)
            fillMaterial(plugin.fillMaterial);

        if (compiledMaterials == null)
            compileMaterials();

        return compiledMaterials.next();
    }

    /**
     * Get the material map, note that this is the non compiled version
     *
     * @return a map containing materials and their change of occurrence between 0 and 1
     */
    public Map<Material, Double> getMaterialMap() {
        return materialMap;
    }

    /**
     * Adds a material to the composition
     *
     * @param mat        The material to add
     * @param percentage The percentage chance of this material occurring within the total composition
     * @return True if the percentage will be at or under 1 after this operation, false if the percentage is too high
     */
    public boolean setMaterial(Material mat, Double percentage) {
        if (getTotalPercentage() + percentage > 1) {
            //Total too high, can we fit it with reducing the default fill material
            Double fillMaterialPercentage = materialMap.get(plugin.fillMaterial);
            if (fillMaterialPercentage >= percentage) {
                //It's possible to fit this amount by reducing default fill material
                materialMap.remove(plugin.fillMaterial);
                materialMap.put(mat, percentage);
                fillMaterial(plugin.fillMaterial);
                compiledMaterials = null;
                return true;
            } else {
                //Cannot fit even with a reduction of fill
                return false;
            }
        } else {
            materialMap.put(mat, percentage);
            compiledMaterials = null;
            return true;
        }
    }

    /**
     * Remove a material from the composition
     *
     * @param mat The material to remove
     * @return returns true if the material was a part of the composition and has been removed, otherwise false
     */
    public boolean removeMaterial(Material mat) {
        if (!materialMap.containsKey(mat))
            return false;
        materialMap.remove(mat);
        compiledMaterials = null;
        return true;
    }

    /**
     * Fill the remaining percentage of the composition
     *
     * @param mat The material to fill with
     */
    public void fillMaterial(Material mat) {
        removeMaterial(mat);
        Double percentage = 1 - getTotalPercentage();
        setMaterial(mat, percentage);
    }

    /**
     * Check if the composition is valid
     *
     * @return true if all percentages add to 1, otherwise false
     */
    public boolean isValidComposition() {
        return getTotalPercentage() == 1;
    }

    /**
     * Used to save the composition to a config file
     *
     * @return a list of materials and doubles serialized to strings
     */
    public List<String> parseToStringList() {
        List<String> data = new ArrayList<>();
        for (Material mat : materialMap.keySet()) {
            Double d = materialMap.get(mat);
            String s = mat.name() + ":" + d.toString();
            data.add(s);
        }
        return data;
    }

    /**
     * Calculates and returns the unassigned percentage
     *
     * @return the percentage of the mines composition that has not yet been assigned to a material
     */
    public double getUnassignedPercentage() {
        return 1 - getTotalPercentage();
    }

    /**
     * @return The current total percentage of all materials
     */
    private double getTotalPercentage() {
        Double d = 0d;
        for (Double toAdd : materialMap.values()) {
            d += toAdd;
        }
        return d;
    }

    /**
     * Takes out own materials list and copies it into the RandomCollection class for later use
     */
    private void compileMaterials() {
        compiledMaterials = new RandomCollection<>();
        for (Material mat : materialMap.keySet()) {
            Double percentage = materialMap.get(mat);
            compiledMaterials.add(percentage, mat);
        }
    }

    /**
     * Class from StackOverflow, <a href="https://stackoverflow.com/a/6409791">Peter Lawrey</a>
     */
    static class RandomCollection<E> {
        private final NavigableMap<Double, E> map = new TreeMap<>();
        private final Random random;
        private double total = 0;

        public RandomCollection() {
            this(new Random());
        }

        public RandomCollection(Random random) {
            this.random = random;
        }

        public void add(double weight, E result) {
            if (weight <= 0) return;
            total += weight;
            map.put(total, result);
        }

        public E next() {
            double value = random.nextDouble() * total;
            return map.higherEntry(value).getValue();
        }
    }


}
