package net.mathdoku.plus.griddefinition;

import android.app.Activity;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testhelper.gridcreator.GridCreator;
import testhelper.gridcreator.GridCreator2x2;
import testhelper.gridcreator.GridCreator4x4;
import testhelper.gridcreator.GridCreator4x4CageIdsNotConsecutiveNorSorted;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridDefinitionCreatorTest {
	@Before
	public void setup() {
		// Instantiate the singleton classes
		Activity activity = new Activity();
		new Util(activity);
	}

	@Test(expected = InvalidParameterException.class)
	public void invoke_ArrayListCellsIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<Cell> cells = null;
		List<Cage> cages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		new GridDefinitionCreator(cells, cages, gridGeneratingParameters)
				.invoke();
	}

	@Test(expected = InvalidParameterException.class)
	public void invoke_ArrayListCellsIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		List<Cell> cells = new ArrayList<Cell>();
		List<Cage> cages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		new GridDefinitionCreator(cells, cages, gridGeneratingParameters)
				.invoke();
	}

	@Test(expected = InvalidParameterException.class)
	public void invoke_ArrayListCagesIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<Cell> cells = mock(ArrayList.class);
		when(cells.size()).thenReturn(1);
		List<Cage> cages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		new GridDefinitionCreator(cells, cages, gridGeneratingParameters)
				.invoke();
	}

	@Test(expected = InvalidParameterException.class)
	public void invoke_ArrayListCagesIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		List<Cell> cells = mock(ArrayList.class);
		when(cells.size()).thenReturn(1);
		List<Cage> cages = new ArrayList<Cage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		new GridDefinitionCreator(cells, cages, gridGeneratingParameters)
				.invoke();
	}

	@Test(expected = InvalidParameterException.class)
	public void invoke_GridGeneratingParametersIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<Cell> cells = mock(ArrayList.class);
		when(cells.size()).thenReturn(1);
		List<Cage> cages = mock(ArrayList.class);
		when(cages.size()).thenReturn(1);
		GridGeneratingParameters gridGeneratingParameters = null;

		new GridDefinitionCreator(cells, cages, gridGeneratingParameters)
				.invoke();
	}

	@Test
	public void invoke_TestGrid2x2_GridDefinitionCreated() throws Exception {
		assertGridDefinition(GridCreator2x2.create());
	}

	private void assertGridDefinition(GridCreator gridCreator) {
		Grid grid = gridCreator.setEmptyGrid().getGrid();
		String gridDefinition = new GridDefinitionCreator(grid.getCells(),
				grid.getCages(), grid.getGridGeneratingParameters()).invoke();
		assertThat(gridDefinition, is(equalTo(gridCreator.getGridDefinition())));
	}

	@Test
	public void invoke_TestGrid4x4_GridDefinitionCreated() throws Exception {
		assertGridDefinition(GridCreator4x4.create());
	}

	@Test
	public void invoke_TestGrid4x4CageIdsNotConsecutive_GridDefinitionCreated()
			throws Exception {
		assertGridDefinition(GridCreator4x4CageIdsNotConsecutiveNorSorted
				.create());
	}
}
