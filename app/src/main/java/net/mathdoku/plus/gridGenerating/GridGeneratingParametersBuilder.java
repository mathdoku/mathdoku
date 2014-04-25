package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.util.Util;

import java.util.Random;

public class GridGeneratingParametersBuilder {
	public static final String PUZZLE_COMPLEXITY_PARAMETER_NAME = "PuzzleComplexity";
	public static final String HIDE_OPERATOR_PARAMETER_NAME = "HideOperator";
	public static final String GRID_TYPE_PARAMETER_NAME = "GridType";
	private GridType gridType;
	private boolean hideOperators;
	private PuzzleComplexity puzzleComplexity;
	private int generatorVersionNumber;
	private long gameSeed;
	private int maxCageSize;
	private int maxCageResult;
	private int maxCagePermutations;
	private int maxSingleCellCages;

	// Flags whether required parameter is set
	private boolean isGridTypeSet = false;
	private boolean isHideOperatorsSet = false;
	private boolean isPuzzleComplexitySet = false;

	private boolean isParameterDependentOnPuzzleComplexityOverridden = false;
	private boolean isParameterDependentOnGridTypeOverridden = false;

	public GridGeneratingParametersBuilder() {
		gameSeed = new Random().nextLong();
		generatorVersionNumber = Util.getPackageVersionNumber();
	}

	public static GridGeneratingParametersBuilder createCopyOfGridGeneratingParameters(
			GridGeneratingParameters gridGeneratingParameters) {
		return new GridGeneratingParametersBuilder()
				.setGridType(gridGeneratingParameters.getGridType())
				.setHideOperators(gridGeneratingParameters.isHideOperators())
				.setPuzzleComplexity(
						gridGeneratingParameters.getPuzzleComplexity())
				.setGeneratorVersionNumber(
						gridGeneratingParameters.getGeneratorVersionNumber())
				.setGameSeed(gridGeneratingParameters.getGameSeed())
				.setMaxCageSize(gridGeneratingParameters.getMaxCageSize())
				.setMaxCageResult(gridGeneratingParameters.getMaxCageResult())
				.setMaxCagePermutations(
						gridGeneratingParameters.getMaxCagePermutations())
				.setMaxSingleCellCages(
						gridGeneratingParameters.getMaximumSingleCellCages());
	}

	public GridGeneratingParametersBuilder setGridType(GridType gridType) {
		throwErrorWhenDependingParameterHasBeenOverridden(
				isParameterDependentOnGridTypeOverridden,
				GRID_TYPE_PARAMETER_NAME);
		this.gridType = gridType;
		isGridTypeSet = true;
		maxSingleCellCages = Math.max(2, gridType.getGridSize() / 2);
		return this;
	}

	private void throwErrorWhenDependingParameterHasBeenOverridden(
			boolean isDependingParameterOverridden, String parameter) {
		if (isDependingParameterOverridden) {
			throw new GridGeneratingException(String.format(
					"Parameter '%s' can not be set again once any of its depending"
							+ " parameters" + " have been overridden.",
					parameter));

		}
	}

	public GridGeneratingParametersBuilder setHideOperators(
			boolean hideOperators) {
		this.hideOperators = hideOperators;
		isHideOperatorsSet = true;
		return this;
	}

	public GridGeneratingParametersBuilder setPuzzleComplexity(
			PuzzleComplexity puzzleComplexity) {
		throwErrorWhenDependingParameterHasBeenOverridden(
				isParameterDependentOnPuzzleComplexityOverridden,
				PUZZLE_COMPLEXITY_PARAMETER_NAME);
		this.puzzleComplexity = puzzleComplexity;

		switch (puzzleComplexity) {
		case VERY_EASY:
			setVeryEasyPuzzleComplexity();
			break;
		case EASY:
			setEasyPuzzleComplexity();
			break;
		case NORMAL:
			setNormalPuzzleComplexity();
			break;
		case DIFFICULT:
			setDifficultPuzzleComplexity();
			break;
		case VERY_DIFFICULT:
			setVeryDifficultPuzzleComplexity();
			break;
		default:
			throw new GridGeneratingException(String.format(
					"Method setRandomPuzzleComplexity can not be used for "
							+ "complexity '%s'.", puzzleComplexity.toString()));
		}
		isPuzzleComplexitySet = true;

		restrictCageSizeToNumberOfCellsInGrid();

		return this;
	}

	private void setVeryEasyPuzzleComplexity() {
		maxCageSize = 2;
		maxCageResult = 9 * 8;
		maxCagePermutations = 20;
	}

	private void setEasyPuzzleComplexity() {
		maxCageSize = 3;
		maxCageResult = 9 * 9 * 8;
		maxCagePermutations = 20;
	}

	private void setNormalPuzzleComplexity() {
		maxCageSize = 4;
		// maxCageResult is limited. Real maximum = 9 * 9 * 8 * 8 = 5,184
		maxCageResult = 2500;
		maxCagePermutations = 40;
	}

	private void setDifficultPuzzleComplexity() {
		maxCageSize = 5;
		// maxCageResult is limited. Real maximum = 9 * 9 * 9 * 8 * 8 = 46,656
		maxCageResult = 9999;
		maxCagePermutations = 80;
	}

	private void setVeryDifficultPuzzleComplexity() {
		maxCageSize = 6;
		// maxCageResult is limited. Real maximum = 9 * 9 * 9 * 8 * 8 * 8 =
		// 373,248
		maxCageResult = 99999;
		maxCagePermutations = 120;
	}

	private void restrictCageSizeToNumberOfCellsInGrid() {
		if (gridType != null) {
			maxCageSize = Math.min(gridType.getNumberOfCells(), maxCageSize);
		}
	}

	public GridGeneratingParametersBuilder setRandomPuzzleComplexity() {
		int index;
		do {
			index = new Random().nextInt(PuzzleComplexity.values().length);
		} while (PuzzleComplexity.values()[index] == PuzzleComplexity.RANDOM);

		return setPuzzleComplexity(PuzzleComplexity.values()[index]);
	}

	public GridGeneratingParametersBuilder setGeneratorVersionNumber(
			int generatorVersionNumber) {
		this.generatorVersionNumber = generatorVersionNumber;
		return this;
	}

	public GridGeneratingParametersBuilder setGameSeed(long gameSeed) {
		this.gameSeed = gameSeed;
		return this;
	}

	public GridGeneratingParametersBuilder setMaxCageSize(int maxCageSize) {
		throwErrorWhenPuzzleComplexityHasNotYetBeenSet("MaxCageSize");
		isParameterDependentOnPuzzleComplexityOverridden = maxCageSize != this.maxCageSize;
		this.maxCageSize = maxCageSize;
		return this;
	}

	private void throwErrorWhenPuzzleComplexityHasNotYetBeenSet(String parameter) {
		if (!isPuzzleComplexitySet) {
			throw new GridGeneratingException(String.format(
					"Parameter '%s' can only be set after parameter "
							+ "'PuzzleComplexity' has " + "been set first.",
					parameter));
		}
	}

	public GridGeneratingParametersBuilder setMaxCageResult(int maxCageResult) {
		throwErrorWhenPuzzleComplexityHasNotYetBeenSet("MaxCageResult");
		isParameterDependentOnPuzzleComplexityOverridden = maxCageResult != this.maxCageResult;
		this.maxCageResult = maxCageResult;
		return this;
	}

	public GridGeneratingParametersBuilder setMaxCagePermutations(
			int maxCagePermutations) {
		throwErrorWhenPuzzleComplexityHasNotYetBeenSet("MaxCagePermutations");
		isParameterDependentOnPuzzleComplexityOverridden = maxCagePermutations != this.maxCagePermutations;
		this.maxCagePermutations = maxCagePermutations;
		return this;
	}

	public GridGeneratingParametersBuilder setMaxSingleCellCages(
			int maxSingleCellCages) {
		if (!isGridTypeSet) {
			throw new GridGeneratingException(
					"Parameter 'MaxSingleCellCages' can only be set after parameter 'GridType' has been set first.");
		}
		isParameterDependentOnGridTypeOverridden = maxSingleCellCages != this.maxSingleCellCages;
		this.maxSingleCellCages = maxSingleCellCages;
		return this;
	}

	public GridGeneratingParameters createGridGeneratingParameters() {
		throwErrorWhenNotAllRequiredParametersAreSet();
		restrictCageSizeToNumberOfCellsInGrid();
		return new GridGeneratingParameters(this);
	}

	private void throwErrorWhenNotAllRequiredParametersAreSet() {
		throwErrorWhenParameterNotSet(isGridTypeSet, GRID_TYPE_PARAMETER_NAME);
		throwErrorWhenParameterNotSet(isHideOperatorsSet,
				HIDE_OPERATOR_PARAMETER_NAME);
		throwErrorWhenParameterNotSet(isPuzzleComplexitySet,
				PUZZLE_COMPLEXITY_PARAMETER_NAME);
	}

	private void throwErrorWhenParameterNotSet(boolean parameterIsSet,
			String parameter) {
		if (!parameterIsSet) {
			throw new GridGeneratingException(String.format(
					"Required parameter '%s' is not set in builder '%s'.",
					parameter, this.getClass().getSimpleName()));
		}
	}

	public GridType getGridType() {
		return gridType;
	}

	public boolean isHideOperators() {
		return hideOperators;
	}

	public PuzzleComplexity getPuzzleComplexity() {
		return puzzleComplexity;
	}

	public int getGeneratorVersionNumber() {
		return generatorVersionNumber;
	}

	public long getGameSeed() {
		return gameSeed;
	}

	public int getMaxCageResult() {
		return maxCageResult;
	}

	public int getMaxCageSize() {
		return maxCageSize;
	}

	public int getMaxCagePermutations() {
		return maxCagePermutations;
	}

	public int getMaxSingleCellCages() {
		return maxSingleCellCages;
	}

	@Override
	@SuppressWarnings("all") // Needed to suppress sonar warning on cyclomatic complexity
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GridGeneratingParametersBuilder)) {
			return false;
		}

		GridGeneratingParametersBuilder that = (GridGeneratingParametersBuilder) o;

		if (gameSeed != that.gameSeed) {
			return false;
		}
		if (generatorVersionNumber != that.generatorVersionNumber) {
			return false;
		}
		if (hideOperators != that.hideOperators) {
			return false;
		}
		if (maxCagePermutations != that.maxCagePermutations) {
			return false;
		}
		if (maxCageResult != that.maxCageResult) {
			return false;
		}
		if (maxCageSize != that.maxCageSize) {
			return false;
		}
		if (maxSingleCellCages != that.maxSingleCellCages) {
			return false;
		}
		if (gridType != that.gridType) {
			return false;
		}
		if (puzzleComplexity != that.puzzleComplexity) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = gridType.hashCode();
		result = 31 * result + (hideOperators ? 1 : 0);
		result = 31 * result + puzzleComplexity.hashCode();
		result = 31 * result + generatorVersionNumber;
		result = 31 * result + (int) (gameSeed ^ (gameSeed >>> 32));
		result = 31 * result + maxCageSize;
		result = 31 * result + maxCageResult;
		result = 31 * result + maxCagePermutations;
		result = 31 * result + maxSingleCellCages;
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(
				"GridGeneratingParametersBuilder{");
		sb.append("gridType=").append(gridType);
		sb.append(", hideOperators=").append(hideOperators);
		sb.append(", puzzleComplexity=").append(puzzleComplexity);
		sb.append(", generatorVersionNumber=").append(generatorVersionNumber);
		sb.append(", gameSeed=").append(gameSeed);
		sb.append(", maxCageSize=").append(maxCageSize);
		sb.append(", maxCageResult=").append(maxCageResult);
		sb.append(", maxCagePermutations=").append(maxCagePermutations);
		sb.append(", maxSingleCellCages=").append(maxSingleCellCages);
		sb.append(", isGridTypeSet=").append(isGridTypeSet);
		sb.append(", isHideOperatorsSet=").append(isHideOperatorsSet);
		sb.append(", isPuzzleComplexitySet=").append(isPuzzleComplexitySet);
		sb.append(", isParameterDependentOnPuzzleComplexityOverridden=")
				.append(isParameterDependentOnPuzzleComplexityOverridden);
		sb.append(", isParameterDependentOnGridTypeOverridden=").append(
				isParameterDependentOnGridTypeOverridden);
		sb.append('}');
		return sb.toString();
	}
}