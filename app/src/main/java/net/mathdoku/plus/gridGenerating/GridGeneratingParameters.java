package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;

/**
 * Immutable grid generating parameters.
 */
public class GridGeneratingParameters {
	private final GridType gridType;
	private final boolean hideOperators;
	private final PuzzleComplexity puzzleComplexity;
	private final int generatorVersionNumber;
	private final long gameSeed;
	private final int maxCageSize;
	private final int maxCageResult;
	private final int maxCagePermutations;
	private final int maximumSingleCellCages;

	public GridGeneratingParameters(GridGeneratingParametersBuilder builder) {
		gameSeed = builder.getGameSeed();
		gridType = builder.getGridType();
		hideOperators = builder.isHideOperators();
		puzzleComplexity = builder.getPuzzleComplexity();
		generatorVersionNumber = builder.getGeneratorVersionNumber();
		maxCageSize = builder.getMaxCageSize();
		maxCageResult = builder.getMaxCageResult();
		maxCagePermutations = builder.getMaxCagePermutations();
		maximumSingleCellCages = builder.getMaxSingleCellCages();
	}

	public int getGeneratorVersionNumber() {
		return generatorVersionNumber;
	}

	public PuzzleComplexity getPuzzleComplexity() {
		return puzzleComplexity;
	}

	public long getGameSeed() {
		return gameSeed;
	}

	public int getMaxCageSize() {
		return maxCageSize;
	}

	public int getMaxCageResult() {
		return maxCageResult;
	}

	public boolean isHideOperators() {
		return hideOperators;
	}

	public GridType getGridType() {
		return gridType;
	}

	public int getMaxCagePermutations() {
		return maxCagePermutations;
	}

	public int getMaximumSingleCellCages() {
		return maximumSingleCellCages;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GridGeneratingParameters{");
		sb.append("generatorVersionNumber=").append(generatorVersionNumber);
		sb.append(", puzzleComplexity=").append(puzzleComplexity);
		sb.append(", gameSeed=").append(gameSeed);
		sb.append(", maxCageSize=").append(maxCageSize);
		sb.append(", maxCageResult=").append(maxCageResult);
		sb.append(", hideOperators=").append(hideOperators);
		sb.append(", gridType=").append(gridType);
		sb.append(", maxCagePermutations=").append(maxCagePermutations);
		sb.append(", maximumSingleCellCages=").append(maximumSingleCellCages);
		sb.append('}');
		return sb.toString();
	}
}