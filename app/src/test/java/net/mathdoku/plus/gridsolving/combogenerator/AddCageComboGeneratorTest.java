package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.grid.Grid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;
import testhelper.gridcreator.GridCreator4x4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class AddCageComboGeneratorTest {
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
        new AddCageComboGenerator(null);
    }

    @Test
    public void getCombosForCage_OneCellCage_OneComboFound() throws Exception {
        Grid grid = GridCreator4x4.createEmptyGrid();
        Cage cage = grid.getCage(5);
        assertThat(cage.getOperator(), is(CageOperator.NONE));
        assertThat(cage.getNumberOfCells(), is(1));

        when(mockComboGenerator.satisfiesConstraints(any(CageCombo.class)))
                .thenReturn(true);
        when(mockComboGenerator.getGridSize()).thenReturn(grid.getGridSize());

        assertThat(new AddCageComboGenerator(mockComboGenerator).getCombosForCage(cage), is(
                getExpectedCageCombos(new int[][] {{cage.getResult()}})));
    }

    @Test
    public void getCombosForCage_MultipleCellCage_MultipleCombosFound() throws Exception {
        Grid grid = GridCreator4x4.createEmptyGrid();
        Cage cage = grid.getCage(3);
        assertThat(cage.getOperator(), is(CageOperator.ADD));
        assertThat(cage.getResult(), is(6));
        assertThat(cage.getNumberOfCells(), is(2));

        when(mockComboGenerator.satisfiesConstraints(any(CageCombo.class)))
                     .thenReturn(true);
        when(mockComboGenerator.getGridSize()).thenReturn(grid.getGridSize());

        assertThat(new AddCageComboGenerator(mockComboGenerator).getCombosForCage(cage),
                   is(getExpectedCageCombos(new int[][]{ {2,4}, {3,3}, {4,2}})));
    }

    private List<CageCombo> getExpectedCageCombos(int[][] combos) {
        List<CageCombo> cageCombos = new ArrayList<CageCombo>();
        for (int[] combo : combos) {
            CageCombo cageCombo = new CageCombo();
            for (int value : combo) {
                cageCombo.append(value);
            }
            cageCombos.add(cageCombo);
        }
        return cageCombos;
    }
}