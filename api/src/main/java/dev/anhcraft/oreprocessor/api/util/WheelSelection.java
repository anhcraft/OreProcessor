package dev.anhcraft.oreprocessor.api.util;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class WheelSelection<E> {
    private final TreeMap<Double, E> rouletteWheel;
    private final Map<E, Double> elementMap;
    private double totalWeight;

    public WheelSelection() {
        rouletteWheel = new TreeMap<>();
        elementMap = new LinkedHashMap<>();
        totalWeight = 0;
    }

    /**
     * Adds an element to the wheel.
     * @param element The element to add.
     * @param weight The weight of the element.
     * @throws IllegalArgumentException If the weight is negative or if the element already exists.
     */
    public void add(E element, double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero.");
        }

        if (elementMap.containsKey(element)) {
            throw new IllegalArgumentException("Element already exists.");
        }

        totalWeight += weight;
        elementMap.put(element, weight);
        rouletteWheel.put(totalWeight, element);
    }

    /**
     * Gets the weight of an element.
     * @param element The element to get the weight of.
     * @return The weight of the element.
     * @throws IllegalArgumentException If the element does not exist.
     */
    public double getWeight(E element) {
        if (!elementMap.containsKey(element)) {
            throw new IllegalArgumentException("Element does not exist.");
        }

        return elementMap.get(element);
    }

    /**
     * Removes an element from the wheel.
     * @param element The element to remove.
     * @throws IllegalArgumentException If the element does not exist.
     */
    public void remove(E element) {
        if (!elementMap.containsKey(element)) {
            throw new IllegalArgumentException("Element does not exist.");
        }

        double weight = elementMap.get(element);
        elementMap.remove(element);
        totalWeight -= weight;
        rouletteWheel.values().removeIf(e -> e.equals(element));
    }

    /**
     * Checks if an element exists in the wheel.
     * @param element The element to check.
     * @return True if the element exists, false otherwise.
     */
    public boolean contains(E element) {
        return elementMap.containsKey(element);
    }

    /**
     * Checks if the wheel is empty.
     * @return True if the wheel is empty, false otherwise.
     */
    public boolean isEmpty() {
        return elementMap.isEmpty();
    }

    /**
     * Rolls the wheel.
     * @return The element that was rolled.
     */
    @Nullable
    public E roll() {
        if (rouletteWheel.isEmpty()) {
            return null;
        }
        if (rouletteWheel.size() == 1) {
            return rouletteWheel.firstEntry().getValue();
        }

        double randomWeight = ThreadLocalRandom.current().nextDouble(totalWeight);
        Map.Entry<Double, E> selectedEntry = rouletteWheel.ceilingEntry(randomWeight);
        return selectedEntry != null ? selectedEntry.getValue() : null;
    }
}
