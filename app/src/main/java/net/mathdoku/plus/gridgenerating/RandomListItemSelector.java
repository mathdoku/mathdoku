package net.mathdoku.plus.gridgenerating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class is used to select a a random item from a list of items with given
 * type. The same selector instance can be used multiple times to select the
 * next random item from the list. Each item in the list will only be chosen
 * once.
 * 
 * A specific item can be set (optional) which will not be chosen until all
 * other elements have been chosen before. Except for this item, each item has
 * an equal chance to be chosen because all items have an equals weight. *
 * 
 * @param <T>
 *            The data type of the items in the list.
 */
public class RandomListItemSelector<T> extends
		RandomWeightedListItemSelector<T> {
	public RandomListItemSelector(Random random, List<T> list) {
		super(random, convertToWeightedList(list));
	}

	private static <T> List<WeightedItem<T>> convertToWeightedList(List<T> list) {
		List<WeightedItem<T>> weightedItemList = new ArrayList<WeightedItem<T>>();
		for (T item : list) {
			weightedItemList.add(new WeightedItem<T>(item, 1));
		}
		return weightedItemList;
	}
}
