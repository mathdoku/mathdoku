package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SingleCellCageComboGeneratorTest extends CageComboGeneratorTest {
    private static final java.lang.Integer GRID_SIZE = 4;
    ComboGenerator mockComboGenerator = mock(ComboGenerator.class);

    @Before
    public void setup() {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        Preferences.getInstance(TestRunnerHelper.getActivity());
    }

    @After
    public void tearDown() {
        TestRunnerHelper.tearDown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDivideComboGenerator_ComboGeneratorIsNull_ThrowsIllegalArgumentException() throws Exception {
        CageComboGenerator.create(null, createSingleCellCage());
    }

    private Cage createSingleCellCage() {
        CageBuilder cageBuilder = new CageBuilder().setCageOperator(CageOperator.NONE)
                .setCells(new int[1])
                .setHideOperator(false)
                .setResult(2);
        return new Cage(cageBuilder);
    }

    @Test
    public void getCombosForCage_SingleCellCage_ComboFound() throws Exception {
        when(mockComboGenerator.satisfiesConstraints(any(CageCombo.class))).thenReturn(true);
        when(mockComboGenerator.getGridSize()).thenReturn(GRID_SIZE);

        Cage cage = createSingleCellCage();
        assertThat(CageComboGenerator.create(mockComboGenerator, cage).getCombos(),
                   is(getExpectedCageCombos(new int[][]{{cage.getResult()}})));
    }
}