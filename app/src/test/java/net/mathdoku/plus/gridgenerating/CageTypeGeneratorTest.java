package net.mathdoku.plus.gridgenerating;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Random;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CageTypeGeneratorTest {
    CageTypeGenerator cageTypeGenerator;

    @Before
    public void setup() {
        cageTypeGenerator = CageTypeGenerator.getInstance();
    }

    @Test
    public void getInstance_CreatesMultipleInstances_ReturnsSingletonInstance() throws Exception {
        CageTypeGenerator cageTypeGenerator2 = CageTypeGenerator.getInstance();
        assertThat(cageTypeGenerator2, is(sameInstance(cageTypeGenerator)));
    }

    @Test
    public void getSingleCellCageType_NoParameters_ReturnsCageWith1Cell() throws Exception {
        CageType cageType = cageTypeGenerator.getSingleCellCageType();
        assertThat(cageType, is(notNullValue()));
        assertThat(cageType.size(), is(1));
        assertThat(cageType.getHeight(), is(1));
        assertThat(cageType.getWidth(), is(1));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_0_ReturnsNoCage() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(0);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(0));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_1_ReturnsTheSingleCellCage() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(1);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(1));
        assertThat(cageTypes.get(0), is(cageTypeGenerator.getSingleCellCageType()));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_2_Returns3Cages() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(2);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(3));
        assertThat(cageTypes.get(0)
                           .size(), is(1));
        assertThat(cageTypes.get(1)
                           .size(), is(2));
        assertThat(cageTypes.get(2)
                           .size(), is(2));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_3_Returns9Cages() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(3);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(9));
        assertThat(cageTypes.get(0)
                           .size(), is(1));
        assertThat(cageTypes.get(1)
                           .size(), is(2));
        assertThat(cageTypes.get(2)
                           .size(), is(2));
        assertThat(cageTypes.get(3)
                           .size(), is(3));
        assertThat(cageTypes.get(4)
                           .size(), is(3));
        assertThat(cageTypes.get(5)
                           .size(), is(3));
        assertThat(cageTypes.get(6)
                           .size(), is(3));
        assertThat(cageTypes.get(7)
                           .size(), is(3));
        assertThat(cageTypes.get(8)
                           .size(), is(3));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_4_Returns28Cages() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(4);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(28));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_5_Returns91Cages() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(5);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(91));
    }

    @Test
    public void getCageTypesWithSizeEqualOrLessThan_ValueAboveMaximum_Returns91Cages() throws Exception {
        List<CageType> cageTypes = cageTypeGenerator.getCageTypesWithSizeEqualOrLessThan(6);
        assertThat(cageTypes, is(notNullValue()));
        assertThat(cageTypes.size(), is(91));
    }

    @Test
    public void getRandomCageType_Cells_6_MaxHeight_4_MaxWidth_3_ReturnsValidCageType() throws Exception {
        int numberOfCells = 6;
        int maxHeight = 4;
        int maxWidth = 3;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    @Test
    public void getRandomCageType_Cells_8_MaxHeight_4_MaxWidth_3_ReturnsValidCageType() throws Exception {
        int numberOfCells = 8;
        int maxHeight = 4;
        int maxWidth = 3;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRandomCageType_InvalidNumberOfCells_ReturnsValidCageType() throws Exception {
        int numberOfCells = 0;
        int maxHeight = 4;
        int maxWidth = 3;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRandomCageType_InvalidWidth_ReturnsValidCageType() throws Exception {
        int numberOfCells = 6;
        int maxHeight = 4;
        int maxWidth = 0;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRandomCageType_InvalidHeight_ReturnsValidCageType() throws Exception {
        int numberOfCells = 6;
        int maxHeight = 0;
        int maxWidth = 3;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRandomCageType_InvalidNumberOfCellsForWidthAndHeightWidth_ReturnsValidCageType() throws Exception {
        int maxHeight = 4;
        int maxWidth = 3;
        int numberOfCells = maxWidth * maxHeight + 1;
        assertCageTypeHasRequestedSize(numberOfCells, maxHeight, maxWidth);
    }

    private void assertCageTypeHasRequestedSize(int numberOfCells, int maxHeight, int maxWidth) {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(0);
        CageType cageType = cageTypeGenerator.getRandomCageType(numberOfCells, maxHeight, maxWidth, random);

        assertThat(cageType, is(notNullValue()));
        assertThat(cageType.size(), is(numberOfCells));
        assertThat("Width of cage type is smaller than or equal to maximum width", cageType.getWidth() <= maxWidth,
                   is(true));
        assertThat("Height of cage type is smaller than or equal to maximum height", cageType.getHeight() <= maxHeight,
                   is(true));
    }
}
