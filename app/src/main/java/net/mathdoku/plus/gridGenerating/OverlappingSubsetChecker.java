package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class OverlappingSubsetChecker {
	private static final String TAG = OverlappingSubsetChecker.class.getName();

	private final Matrix<Integer> correctValueMatrix;
	private int[] countValues;
	private boolean debugLogging;

	public OverlappingSubsetChecker(Matrix<Integer> correctValueMatrix) {
		this.correctValueMatrix = correctValueMatrix;
		debugLogging = false;
	}

	@SuppressWarnings("UnusedReturnValue")
	public OverlappingSubsetChecker enableLogging(boolean enableLogging) {
		debugLogging = enableLogging;
		return this;
	}

	private void initializeUsedValues() {
		// Array for counting values has 1 additional element so the values do
		// not need to be zero-based.
		countValues = new int[correctValueMatrix.size() + 1];
	}

	private void setUsedValue(int value) {
		countValues[value]++;
	}

	private int countDuplicatesValues() {
		int countDuplicates = 0;
		for (int value = 1; value < countValues.length; value++) {
			if (countValues[value] > 1) {
				countDuplicates++;
			}
		}
		return countDuplicates;
	}

	private List<Integer> getDuplicatesValues() {
		List<Integer> duplicateValues = new ArrayList<Integer>();
		for (int value = 1; value < countValues.length; value++) {
			if (countValues[value] > 1) {
				duplicateValues.add(value);
			}
		}
		return duplicateValues;
	}

	public boolean hasOverlap(Matrix<Integer> cageIdMatrix,
			Matrix<Boolean> usedCellsForNewCageMatrix) {
		return hasOverlap(correctValueMatrix, cageIdMatrix,
				usedCellsForNewCageMatrix)
				|| hasOverlap(correctValueMatrix.createTransposedMatrix(),
						cageIdMatrix.createTransposedMatrix(),
						usedCellsForNewCageMatrix.createTransposedMatrix());
	}

	private boolean hasOverlap(Matrix<Integer> correctValueMatrix,
			Matrix<Integer> cageIdMatrix,
			Matrix<Boolean> usedCellsForNewCageMatrix) {
		for (int newCageCol = 0; newCageCol < correctValueMatrix.size(); newCageCol++) {
			if (newCageHasMultipleCellsInColumn(usedCellsForNewCageMatrix,
					newCageCol)
					&& hasOverlapWithAnyColumn(correctValueMatrix,
							cageIdMatrix, usedCellsForNewCageMatrix, newCageCol)) {
				return true;
			}
		}

		// No overlapping subset found
		return false;
	}

	private boolean newCageHasMultipleCellsInColumn(
			Matrix<Boolean> usedCellsForNewCageMatrix, int newCageCol) {
		return usedCellsForNewCageMatrix.countValueInColumn(true, newCageCol) > 1;
	}

	private boolean hasOverlapWithAnyColumn(Matrix<Integer> correctValueMatrix,
			Matrix<Integer> cageIdMatrix,
			Matrix<Boolean> usedCellsForNewCageMatrix, int sourceColumn) {
		for (int targetColumn = 0; targetColumn < correctValueMatrix.size(); targetColumn++) {
			if (targetColumn != sourceColumn
					&& hasOverlapWithColumn(correctValueMatrix, cageIdMatrix,
							usedCellsForNewCageMatrix, sourceColumn,
							targetColumn)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasOverlapWithColumn(Matrix<Integer> correctValueMatrix,
			Matrix<Integer> cageIdMatrix,
			Matrix<Boolean> usedCellsForNewCageMatrix, int sourceColumn,
			int targetColumn) {
		List<Integer> cagesChecked = new ArrayList<Integer>();

		// Iterate all cells in the column from top to bottom.
		for (int row = 0; row < correctValueMatrix.size(); row++) {
			int otherCageId = cageIdMatrix.get(row, targetColumn);
			if (cageIdMatrix.isNotEmpty(row, targetColumn)
					&& usedCellsForNewCageMatrix.get(row, sourceColumn)
					&& !cagesChecked.contains(otherCageId)) {
				// Cell[row][col] is used in a cage which is not
				// yet checked. This is the first row for which
				// the new cage and the other cage has a cell in
				// the columns which are compared.
				cagesChecked.add(otherCageId);
				if (hasOverlapInColumnForCage(correctValueMatrix, cageIdMatrix,
						usedCellsForNewCageMatrix, sourceColumn, targetColumn,
						row, otherCageId)) {
					return true;
				}

			}
		}
		return false;
	}

	private boolean hasOverlapInColumnForCage(
			Matrix<Integer> correctValueMatrix, Matrix<Integer> cageIdMatrix,
			Matrix<Boolean> usedCellsForNewCageMatrix, int sourceColumn,
			int targetColumn, int startRow, int targetCageId) {
		// Check all remaining rows if the checked
		// columns contain a cell for the new cage and
		// the other cage.
		initializeUsedValues();
		for (int row = startRow; row < correctValueMatrix.size(); row++) {
			if (cageIdMatrix.get(row, targetColumn) == targetCageId
					&& usedCellsForNewCageMatrix.get(row, sourceColumn)) {
				// Both cages contain a cell on the same
				// row. Remember values used in those
				// cells.
				setUsedValue(correctValueMatrix.get(row, targetColumn));
				setUsedValue(correctValueMatrix.get(row, sourceColumn));
			}
		}

		// Determine which values are used in both cages
		if (countDuplicatesValues() > 1) {
			if (debugLogging) {
				String dimension = correctValueMatrix.isTransposed() ? "row"
						: "column";
				logNonUniqueSolution(sourceColumn, targetColumn, dimension,
						targetCageId, getDuplicatesValues());
			}
			return true;
		}
		return false;
	}

	private void logNonUniqueSolution(int dimension1, int dimension2,
			String dimension, int otherCageId, List<Integer> duplicateValues) {
		String nonUniqueSolutionMessage = "         This cage type results in a non-unique solution. "
				+ "The new cage contains values %s in %s %d which are also used in %s %d within cage %d.";

		Log.i(TAG, String.format(nonUniqueSolutionMessage,
				duplicateValues.toString(), dimension, dimension1, dimension,
				dimension2, otherCageId));
	}
}
