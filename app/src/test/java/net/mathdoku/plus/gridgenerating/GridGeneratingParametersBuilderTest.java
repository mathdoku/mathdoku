package net.mathdoku.plus.gridgenerating;

import android.app.Activity;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class GridGeneratingParametersBuilderTest {
    private GridGeneratingParametersBuilder gridGeneratingParametersBuilder;
    private GridGeneratingParameters gridGeneratingParameters;

    private final GridType GRID_TYPE = GridType.GRID_5X5;
    private final boolean HIDE_OPERATORS = true;
    private final PuzzleComplexity PUZZLE_COMPLEXITY = PuzzleComplexity.DIFFICULT;
    private final int GENERATOR_REVISION_NUMBER = 598;
    private static final long GAME_SEED = 163348382L;
    private static final int MAX_CAGE_SIZE = 8;
    private static final int MAX_CAGE_RESULT = 9182737;
    private static final int MAX_CAGE_PERMUTATIONS = 7234;
    private static final int MAX_SINGLE_CELL_CAGES = 11;

    @Before
    public void setup() {
        // Instantiate the singleton classes
        Activity activity = new Activity();
        new Util(activity);

        gridGeneratingParametersBuilder = createGridGeneratingParametersBuilderWithRequiredParametersOnly()
                .setGeneratorVersionNumber(
                GENERATOR_REVISION_NUMBER)
                .setGameSeed(GAME_SEED)
                .setMaxCageSize(MAX_CAGE_SIZE)
                .setMaxCageResult(MAX_CAGE_RESULT)
                .setMaxCagePermutations(MAX_CAGE_PERMUTATIONS)
                .setMaxSingleCellCages(MAX_SINGLE_CELL_CAGES);
        gridGeneratingParameters = gridGeneratingParametersBuilder.createGridGeneratingParameters();
    }

    private GridGeneratingParametersBuilder createGridGeneratingParametersBuilderWithRequiredParametersOnly() {
        return new GridGeneratingParametersBuilder().setGridType(GRID_TYPE)
                .setHideOperators(HIDE_OPERATORS)
                .setPuzzleComplexity(PUZZLE_COMPLEXITY);
    }

    @Test
    public void createCopyOfGridGeneratingParameters_AllParametersHaveAValue_CopyIsIdentical() throws Exception {
        assertThat(gridGeneratingParametersBuilder,
                   equalTo(GridGeneratingParametersBuilder.createCopyOfGridGeneratingParameters(
                           gridGeneratingParameters)));
    }

    @Test(expected = GridGeneratingException.class)
    public void setGridType_CanNotSetGridTypeAfterOverrideMaxSingleCellCages_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setGridType(GridType.GRID_5X5)
                .setMaxSingleCellCages(6)
                .setGridType(GridType.GRID_8X8);
    }

    @Test
    public void setPuzzleComplexity_OverridePuzzleComplexity_ErrorThrown() throws Exception {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder()
                .setPuzzleComplexity(
                PuzzleComplexity.NORMAL)
                .setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
        assertThat(gridGeneratingParametersBuilder.getPuzzleComplexity(), is(PuzzleComplexity.DIFFICULT));
    }

    @Test(expected = GridGeneratingException.class)
    public void setPuzzleComplexity_CanNotSetPuzzleComplexityAfterOverrideMaxCageSize_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setPuzzleComplexity(PuzzleComplexity.NORMAL)
                .setMaxCageSize(10)
                .setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
    }

    @Test(expected = GridGeneratingException.class)
    public void setPuzzleComplexity_CanNotSetPuzzleComplexityAfterOverrideMaxCageResult_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setPuzzleComplexity(PuzzleComplexity.NORMAL)
                .setMaxCageResult(123434)
                .setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
    }

    @Test(expected = GridGeneratingException.class)
    public void setPuzzleComplexity_CanNotSetPuzzleComplexityAfterOverrideMaxCagePermutations_ErrorThrown() throws
            Exception {
        new GridGeneratingParametersBuilder().setPuzzleComplexity(PuzzleComplexity.NORMAL)
                .setMaxCagePermutations(12434)
                .setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
    }

    @Test(expected = GridGeneratingException.class)
    public void setPuzzleComplexity_ComplexityRandom_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setPuzzleComplexity(PuzzleComplexity.RANDOM);
    }

    @Test
    public void setPuzzleComplexity_ValidComplexity_MaxCageSizeSet() throws Exception {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder();
        assertThat(gridGeneratingParametersBuilder.getMaxCageSize(), is(0));
        gridGeneratingParametersBuilder.setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
        assertThat(gridGeneratingParametersBuilder.getMaxCageSize(), is(not(0)));
    }

    @Test
    public void setPuzzleComplexity_ValidComplexity_MaxCageResultIsSet() throws Exception {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder();
        assertThat(gridGeneratingParametersBuilder.getMaxCageResult(), is(0));
        gridGeneratingParametersBuilder.setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
        assertThat(gridGeneratingParametersBuilder.getMaxCageResult(), is(not(0)));
    }

    @Test
    public void setPuzzleComplexity_ValidComplexity_MaxCagePermutationsIsSet() throws Exception {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder();
        assertThat(gridGeneratingParametersBuilder.getMaxCagePermutations(), is(0));
        gridGeneratingParametersBuilder.setPuzzleComplexity(PuzzleComplexity.DIFFICULT);
        assertThat(gridGeneratingParametersBuilder.getMaxCagePermutations(), is(not(0)));
    }

    @Test
    public void setRandomPuzzleComplexity_NoParametersSet_PuzzleComplexityAndDependingParametersAreAllSet() throws
            Exception {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder()
                .setRandomPuzzleComplexity();
        PuzzleComplexity defaultPuzzleComplexity = gridGeneratingParametersBuilder.getPuzzleComplexity();
        // As the setRandomComplexity can result in setting the complexity to
        // the default value, it is tried a number of times until another
        // complexity has been set.
        PuzzleComplexity randomPuzzleComplexity;
        int maxTries = 10;
        do {
            gridGeneratingParametersBuilder.setRandomPuzzleComplexity();
        } while (maxTries-- > 0 && gridGeneratingParametersBuilder.getPuzzleComplexity() == defaultPuzzleComplexity);
        assertThat("SetRandomComplexity results in default puzzle complexity several times in a row.",
                   gridGeneratingParametersBuilder.getPuzzleComplexity(), is(not(defaultPuzzleComplexity)));
    }

    @Test(expected = GridGeneratingException.class)
    public void setMaxCageSize_PuzzleComplexityHasNotYetBeenSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setMaxCageSize(MAX_CAGE_SIZE);
    }

    @Test(expected = GridGeneratingException.class)
    public void setMaxCageResult_PuzzleComplexityHasNotYetBeenSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setMaxCageResult(MAX_CAGE_RESULT);
    }

    @Test(expected = GridGeneratingException.class)
    public void setMaxCagePermutations_PuzzleComplexityHasNotYetBeenSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setMaxCagePermutations(MAX_CAGE_PERMUTATIONS);
    }

    @Test(expected = GridGeneratingException.class)
    public void setMaxSingleCellCages_GridTypeHasNotYetBeenSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setMaxSingleCellCages(MAX_SINGLE_CELL_CAGES);
    }

    @Test(expected = GridGeneratingException.class)
    public void createGridGeneratingParameters_GridTypeNotSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setHideOperators(HIDE_OPERATORS)
                .setPuzzleComplexity(PUZZLE_COMPLEXITY)
                .setGeneratorVersionNumber(GENERATOR_REVISION_NUMBER)
                .setGameSeed(GAME_SEED)
                .setMaxCageSize(MAX_CAGE_SIZE)
                .setMaxCageResult(MAX_CAGE_RESULT)
                .createGridGeneratingParameters();
    }

    @Test(expected = GridGeneratingException.class)
    public void createGridGeneratingParameters_HideOperatorsNotSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setGridType(GRID_TYPE)
                .setPuzzleComplexity(PUZZLE_COMPLEXITY)
                .setGeneratorVersionNumber(GENERATOR_REVISION_NUMBER)
                .setGameSeed(GAME_SEED)
                .setMaxCageSize(MAX_CAGE_SIZE)
                .setMaxCageResult(MAX_CAGE_RESULT)
                .createGridGeneratingParameters();
    }

    @Test(expected = GridGeneratingException.class)
    public void createGridGeneratingParameters_PuzzleComplexityNotSet_ErrorThrown() throws Exception {
        new GridGeneratingParametersBuilder().setGridType(GRID_TYPE)
                .setHideOperators(HIDE_OPERATORS)
                .setGeneratorVersionNumber(GENERATOR_REVISION_NUMBER)
                .setGameSeed(GAME_SEED)
                .setMaxCageSize(MAX_CAGE_SIZE)
                .setMaxCageResult(MAX_CAGE_RESULT)
                .createGridGeneratingParameters();
    }

    @Test
    public void createGridGeneratingParameters_OnlyRequiredParametersSet_BuilderCreated() throws Exception {
        GridGeneratingParameters gridGeneratingParameters =
                createGridGeneratingParametersBuilderWithRequiredParametersOnly().createGridGeneratingParameters();
        assertThat(gridGeneratingParameters, is(notNullValue(GridGeneratingParameters.class)));
    }

    @Test
    public void createGridGeneratingParameters_MaxCageSizeIsBiggerThanNumberOfCellsInGrid_MaxCageRestricted() throws
            Exception {
        GridType gridType = GridType.GRID_9X9;
        int numberOfCellsInGrid = gridType.getNumberOfCells();
        GridGeneratingParameters gridGeneratingParameters =
                createGridGeneratingParametersBuilderWithRequiredParametersOnly().setGridType(
                gridType)
                .setMaxCageSize(numberOfCellsInGrid + 100)
                .createGridGeneratingParameters();
        assertThat(gridGeneratingParameters.getMaxCageSize(), is(numberOfCellsInGrid));
    }

    @Test
    public void createGridGeneratingParameters_GridType_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getGridType(), is(GRID_TYPE));
    }

    @Test
    public void createGridGeneratingParameters_HideOperators_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.isHideOperators(), is(HIDE_OPERATORS));
    }

    @Test
    public void createGridGeneratingParameters_PuzzleComplexity_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getPuzzleComplexity(), is(PUZZLE_COMPLEXITY));
    }

    @Test
    public void createGridGeneratingParameters_GeneratorRevisionNumber_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getGeneratorVersionNumber(), is(GENERATOR_REVISION_NUMBER));
    }

    @Test
    public void createGridGeneratingParameters_GameSeed_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getGameSeed(), is(GAME_SEED));
    }

    @Test
    public void createGridGeneratingParameters_MaxCageSize_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getMaxCageSize(), is(MAX_CAGE_SIZE));
    }

    @Test
    public void createGridGeneratingParameters_MaxCageResult_IsSetCorrectly() throws Exception {
        assertThat(gridGeneratingParameters.getMaxCageResult(), is(MAX_CAGE_RESULT));
    }

}
