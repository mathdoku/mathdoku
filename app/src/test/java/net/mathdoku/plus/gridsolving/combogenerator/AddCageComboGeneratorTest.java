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
public class AddCageComboGeneratorTest extends CageComboGeneratorTest {
    private static final int CAGE_SIZE = 2;
    public static final int GRID_SIZE = 4;
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
    public void createAddComboGenerator_ComboGeneratorIsNull_ThrowsIllegalArgumentException() throws Exception {
        CageComboGenerator.create(null, createTwoCellCage(CageOperator.ADD));
    }

    @Test
    public void getCombos_AddCageVisibleOperator_MultipleCombosFound() throws Exception {
        CageBuilder cageBuilder = new CageBuilder().setCageOperator(CageOperator.ADD)
                .setCells(new int[CAGE_SIZE])
                .setHideOperator(false)
                .setResult(6);
        Cage cage = new Cage(cageBuilder);

        when(mockComboGenerator.satisfiesConstraints(any(CageCombo.class)))
                     .thenReturn(true);
        when(mockComboGenerator.getGridSize()).thenReturn(GRID_SIZE);

        assertThat(CageComboGenerator.create(mockComboGenerator, cage).getCombos(),
                   is(getExpectedCageCombos(new int[][]{ {2,4}, {3,3}, {4,2}})));
    }
}