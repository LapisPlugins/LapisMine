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
     */
    public Composition(LapisMine plugin) {
        this.plugin = plugin;
        materialMap = new HashMap<>();
        compiledMaterials = null;
    }

    /**
     * This initializer should be used when loading from a mine config.
     *
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
     * Adds a material to the composition
     *
     * @param mat        The material to add
     * @param percentage The percentage chance of this material occurring within the total composition
     * @return True if the percentage will be at or under 1 after this operation, false if the percentage is too high
     */
    public boolean addMaterial(Material mat, Double percentage) {
        if (getTotalPercentage() + percentage > 1) {
            //Total too high, can we fit it with reducing the default fill material
            Double fillMaterialPercentage = materialMap.get(plugin.fillMaterial);
            if (fillMaterialPercentage >= percentage) {
                //It's possible to fit this amount by reducing default fill material
                materialMap.remove(plugin.fillMaterial);
                materialMap.put(mat, percentage);
                fillMaterial(plugin.fillMaterial);
                return true;
            } else {
                //Cannot fit even with a reduction of fill
                return false;
            }
        } else {
            materialMap.put(mat, percentage);
            return true;
        }
    }

    /**
     * Fill the remaining percentage of the composition
     *
     * @param mat The material to fill with
     */
    public void fillMaterial(Material mat) {
        Double percentage = 1 - getTotalPercentage();
        addMaterial(mat, percentage);
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

        public RandomCollection<E> add(double weight, E result) {
            if (weight <= 0) return this;
            total += weight;
            map.put(total, result);
            return this;
        }

        public E next() {
            double value = random.nextDouble() * total;
            return map.higherEntry(value).getValue();
        }
    }


}
