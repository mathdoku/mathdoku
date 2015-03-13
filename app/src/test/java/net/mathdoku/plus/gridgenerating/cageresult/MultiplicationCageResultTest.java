package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MultiplicationCageResultTest {
    @Test(expected = IllegalStateException.class)
    public void tryToCreate_NullValue_ThrowsIllegalStateException() throws Exception {
        MultiplicationCageResult.tryToCreate(null);
    }

    @Test(expected = IllegalStateException.class)
    public void tryToCreate_TooLittleValues_ThrowsIllegalStateException() throws Exception {
        MultiplicationCageResult.tryToCreate(1);
    }

    @Test
    public void tryToCreate_CorrectNumberOfValues_CageResultCreated() throws Exception {
        MultiplicationCageResult multiplicationCageResult = MultiplicationCageResult.tryToCreate(1, 3);
        assertThat(multiplicationCageResult, is(notNullValue()));
    }

    @Test
    public void testGetResult_2Values_CorrectResult() throws Exception {
        MultiplicationCageResult multiplicationCageResult = MultiplicationCageResult.tryToCreate(1, 3);
        assertThat(multiplicationCageResult.getResult(), is(1 * 3));
    }

    @Test
    public void testGetResult_10Values_CorrectResult() throws Exception {
        MultiplicationCageResult multiplicationCageResult = MultiplicationCageResult.tryToCreate(8, 4, 5, 3, 2, 9, 7, 1,
                                                                                                 6);
        assertThat(multiplicationCageResult.getResult(), is(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9));
    }
}
