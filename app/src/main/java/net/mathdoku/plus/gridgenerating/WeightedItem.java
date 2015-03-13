package net.mathdoku.plus.gridgenerating;

/**
 * Wraps an item of given type with a specified weight.
 *
 * @param <T>
 *         The data type of the item to be wrapped.
 */
public class WeightedItem<T> {
    private final int weight;
    private final T item;

    public WeightedItem(T item, int weight) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot instantiate an null item");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException(String.format("Cannot instantiate with weight '%d'.", weight));
        }
        this.weight = weight;
        this.item = item;
    }

    public int getWeight() {
        return weight;
    }

    public T getItem() {
        return item;
    }
}
