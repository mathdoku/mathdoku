package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridsolving.ComboGenerator;

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

	public ComboGenerator createComboGenerator() {
		return new ComboGenerator(gridType.getGridSize());
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GridGeneratingParameters)) {
			return false;
		}

		GridGeneratingParameters that = (GridGeneratingParameters) o;

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
		if (maximumSingleCellCages != that.maximumSingleCellCages) {
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
		result = 31 * result + maximumSingleCellCages;
		return result;
	}
}