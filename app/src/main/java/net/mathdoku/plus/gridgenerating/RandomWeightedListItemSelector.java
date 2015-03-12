package net.mathdoku.plus.gridgenerating;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This class is used to select a a random item from a list of items with given type. The same
 * selector instance can be used multiple times to select the next random item from the list. Each
 * item in the list will only be chosen once.
 * <p/>
 * A specific item can be set (optional) which will not be chosen until all other elements have been
 * chosen before.
 * <p/>
 * Each item has a certain weight which affects the chance of the item to be chose. A bigger weight
 * increases the chance the item is chosen.
 *
 * @param <T>
 *         The data type of the items in the list.
 */

public class RandomWeightedListItemSelector<T> {
    private final Random random;
    private final List<WeightedItem<T>> list;
    private T lastItemToBeSelected;

    public RandomWeightedListItemSelector(Random random, List<WeightedItem<T>> list) {
        if (random == null) {
            throw new IllegalArgumentException("Cannot create instance when random is 'null'.");
        }
        if (list == null) {
            throw new IllegalArgumentException("Cannot create instance when list is 'null'.");
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Cannot create instance when list is empty.");
        }
        this.random = random;
        this.list = new ArrayList<WeightedItem<T>>(list);
    }

    /**
     * Sets the item which is chosen only when all other items have been chosen before.
     */
    public void setLastItemToBeSelected(T lastItem) {
        this.lastItemToBeSelected = lastItem;
        removeWeightedItem(lastItem);
    }

    private void removeWeightedItem(T lastItem) {
        Iterator<WeightedItem<T>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeightedItem weightedItem = iterator.next();
            if (weightedItem.getItem()
                    .equals(lastItem)) {
                iterator.remove();
            }
        }
    }

    public T next() {
        if (list.isEmpty()) {
            if (lastItemToBeSelected == null) {
                throw new IllegalStateException(
                        "List is empty. Last item to be selected (if applicable) has already " +
                                "been" + " returned.");
            }
            T nextItem = lastItemToBeSelected;
            lastItemToBeSelected = null;
            return nextItem;
        }

        WeightedItem<T> weightedItem = getWeightedItemByWeight(getRandomIndexWeight());
        list.remove(weightedItem);
        return weightedItem.getItem();
    }

    private int getRandomIndexWeight() {
        return random.nextInt(getTotalWeight()) + 1;
    }

    private int getTotalWeight() {
        int totalWeight = 0;
        for (WeightedItem weightedItem : list) {
            totalWeight += weightedItem.getWeight();
        }
        return totalWeight;
    }

    private WeightedItem<T> getWeightedItemByWeight(int indexWeight) {
        int cumulativeWeight = 0;
        for (WeightedItem<T> weightedItem : list) {
            cumulativeWeight += weightedItem.getWeight();
            if (cumulativeWeight >= indexWeight) {
                return weightedItem;
            }
        }
        throw new IllegalStateException("Index out of bounds.");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return list.isEmpty() && lastItemToBeSelected == null;
    }
}
