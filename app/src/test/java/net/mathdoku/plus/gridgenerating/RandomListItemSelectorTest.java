package net.mathdoku.plus.gridgenerating;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomListItemSelectorTest {
    private String[] stringItems = new String[]{"a", "b", "c", "d", "e"};
    private int[] weightItems = new int[]{4, 3, 5, 1, 2};
    private RandomListItemSelector randomListItemSelector;

    @Test
    public void next_RetrieveSpecificItemByIndex_ExpectedItemReturned() throws Exception {
        for (int i = 0; i < stringItems.length; i++) {
            assertThatIndexResultsInItem(i);
        }
    }

    private void assertThatIndexResultsInItem(int index) {
        Random randomMock = mock(Random.class);
        when(randomMock.nextInt(anyInt())).thenReturn(index);
        randomListItemSelector = new RandomListItemSelector(randomMock, Arrays.asList(stringItems));
        assertThat((String) randomListItemSelector.next(), is(stringItems[index]));
    }
}