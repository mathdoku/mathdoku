package net.mathdoku.plus.gridgenerating;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomWeightedListItemSelectorTest {
    private String[] stringItems = new String[]{"a", "b", "c", "d", "e"};
    private int[] weightItems = new int[]{4, 3, 5, 1, 2};
    private RandomWeightedListItemSelector randomWeightedListItemSelector;

    private List<WeightedItem> getWeightedListWithNumberOfItems(int numberOfItems) {
        List<WeightedItem> listWeightedItems = new ArrayList<WeightedItem>();
        for (int i = 1; i <= Math.min(numberOfItems, stringItems.length); i++) {
            WeightedItem<String> weightedItem = new WeightedItem<String>(stringItems[i - 1], weightItems[i - 1]);
            listWeightedItems.add(weightedItem);
        }
        return listWeightedItems;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NullRandomizer_IllegalArgumentException() throws Exception {
        new RandomWeightedListItemSelector(null, getWeightedListWithNumberOfItems(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NullWeightedList_IllegalArgumentException() throws Exception {
        new RandomWeightedListItemSelector(new Random(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_EmptyWeightedList_IllegalArgumentException() throws Exception {
        new RandomWeightedListItemSelector(new Random(), getWeightedListWithNumberOfItems(0));
    }

    @Test
    public void constructor_WeightedListWithOneItem_IllegalArgumentException() throws Exception {
        new RandomWeightedListItemSelector(new Random(), getWeightedListWithNumberOfItems(1));
    }

    @Test
    public void constructor_WeightedListWithMultipleItems_IllegalArgumentException() throws Exception {
        new RandomWeightedListItemSelector(new Random(), getWeightedListWithNumberOfItems(2));
    }

    @Test
    public void setLastItemToBeSelected_ListWithOneItemWhichIsSetAsLastItem_ItemCorrectlyReturned() throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(1));
        int indexToBeSelectedAsLastItem = 0;
        randomWeightedListItemSelector.setLastItemToBeSelected(stringItems[indexToBeSelectedAsLastItem]);
        assertThat((String) randomWeightedListItemSelector.next(), is(stringItems[indexToBeSelectedAsLastItem]));
    }

    @Test
    public void
    setLastItemToBeSelected_ListWithMultipleItemsFromWhichOneItemIsSetAsLastItem_ItemCorrectlyReturnedAsLastItem()
            throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(
                                                                                    stringItems.length));
        int indexToBeSelectedAsLastItem = 2;
        randomWeightedListItemSelector.setLastItemToBeSelected(stringItems[indexToBeSelectedAsLastItem]);
        for (int i = 0; i < stringItems.length - 1; i++) {
            assertThat((String) randomWeightedListItemSelector.next(),
                       is(not(stringItems[indexToBeSelectedAsLastItem])));
        }
        assertThat((String) randomWeightedListItemSelector.next(), is(stringItems[indexToBeSelectedAsLastItem]));
    }

    @Test
    public void
    setLastItemToBeSelected_ListWithMultipleItemsAndLastItemNotInOriginalList_ItemCorrectlyReturnedAsLastItem()
            throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(
                                                                                    stringItems.length));
        String theLastItem = "*** THE LAST ITEM ***";
        randomWeightedListItemSelector.setLastItemToBeSelected(theLastItem);
        for (int i = 0; i < stringItems.length; i++) {
            assertThat((String) randomWeightedListItemSelector.next(), is(not(theLastItem)));
        }
        assertThat((String) randomWeightedListItemSelector.next(), is(theLastItem));
    }

    @Test(expected = IllegalStateException.class)
    public void next_TooManyItemRetrievedFromList_IllegalStateExceptionThrown() throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(1));
        assertThat((String) randomWeightedListItemSelector.next(), is(stringItems[0]));
        randomWeightedListItemSelector.next();
    }

    @Test
    public void next_RetrieveSpecificItemByWeight_ExpectedItemReturned() throws Exception {
        assertThatWeightResultsInItem(1, 0);
        assertThatWeightResultsInItem(2, 0);
        assertThatWeightResultsInItem(3, 0);
        assertThatWeightResultsInItem(4, 0);

        assertThatWeightResultsInItem(5, 1);
        assertThatWeightResultsInItem(6, 1);
        assertThatWeightResultsInItem(7, 1);

        assertThatWeightResultsInItem(8, 2);
        assertThatWeightResultsInItem(9, 2);
        assertThatWeightResultsInItem(10, 2);
        assertThatWeightResultsInItem(11, 2);
        assertThatWeightResultsInItem(12, 2);

        assertThatWeightResultsInItem(13, 3);

        assertThatWeightResultsInItem(14, 4);
        assertThatWeightResultsInItem(15, 4);
    }

    private void assertThatWeightResultsInItem(int weight, int indexExpectedItem) {
        Random randomMock = mock(Random.class);
        // TODO: remove offset "- 1" in randomMock. Offset is needed as
        // randomizedWeight currently needs to be zero based instead of starting
        // at 1. This can not ye be changed as it affects the generated result
        // grid.
        when(randomMock.nextInt(anyInt())).thenReturn(weight - 1);
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(randomMock,
                                                                            getWeightedListWithNumberOfItems(
                                                                                    stringItems.length));
        assertThat((String) randomWeightedListItemSelector.next(), is(stringItems[indexExpectedItem]));
    }

    @Test
    public void next_RetrieveItemsInOrderSpecifiedByRandomized_ItemsReturnedInCorrectOrder() throws Exception {
        Random randomMock = mock(Random.class);

        int[] orderedIndexesOfItems = new int[]{2, 0, 4, 3, 1};
        int[] randomWeightsInOrder = new int[orderedIndexesOfItems.length];
        String[] expectedItemsInOrder = new String[orderedIndexesOfItems.length];

        int[] weight = weightItems.clone();
        for (int i = 0; i < randomWeightsInOrder.length; i++) {
            // TODO: remove offset "- 1" in cumulative weight. Offset is needed
            // as
            // randomizedWeight currently needs to be zero based instead of
            // starting
            // at 1. This can not ye be changed as it affects the generated
            // result
            // grid.
            randomWeightsInOrder[i] = calculateCumulativeWeight(weight, orderedIndexesOfItems[i]) - 1;
            weight[orderedIndexesOfItems[i]] = 0;
            expectedItemsInOrder[i] = stringItems[orderedIndexesOfItems[i]];
        }

        when(randomMock.nextInt(anyInt())).thenReturn(randomWeightsInOrder[0], randomWeightsInOrder[1],
                                                      randomWeightsInOrder[2], randomWeightsInOrder[3],
                                                      randomWeightsInOrder[4]);

        randomWeightedListItemSelector = new RandomWeightedListItemSelector(randomMock,
                                                                            getWeightedListWithNumberOfItems(
                                                                                    stringItems.length));

        String[] resultItemsInOrder = new String[orderedIndexesOfItems.length];
        for (int i = 0; i < resultItemsInOrder.length; i++) {
            resultItemsInOrder[i] = String.valueOf(randomWeightedListItemSelector.next());
        }

        assertThat(resultItemsInOrder, is(expectedItemsInOrder));
    }

    private int calculateCumulativeWeight(int[] weight, int toLastIndex) {
        int cumulativeWeight = 0;
        for (int i = 0; i <= toLastIndex; i++) {
            cumulativeWeight += weight[i];
        }
        return cumulativeWeight;
    }

    @Test
    public void isEmpty_NonEmptyListLastItemNotSet_False() throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(1));
        assertThat(randomWeightedListItemSelector.isEmpty(), is(false));
    }

    @Test
    public void isEmpty_NonEmptyListLastItemSet_False() throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(1));
        int indexToBeSelectedAsLastItem = 0;
        randomWeightedListItemSelector.setLastItemToBeSelected(stringItems[indexToBeSelectedAsLastItem]);
        assertThat(randomWeightedListItemSelector.isEmpty(), is(false));
    }

    @Test
    public void isEmpty_EmptyList_False() throws Exception {
        randomWeightedListItemSelector = new RandomWeightedListItemSelector(new Random(),
                                                                            getWeightedListWithNumberOfItems(1));
        assertThat((String) randomWeightedListItemSelector.next(), is(stringItems[0]));
        assertThat(randomWeightedListItemSelector.isEmpty(), is(true));
    }
}
