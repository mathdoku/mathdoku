package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;
import net.mathdoku.plus.gridsolving.ComboGenerator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;

import java.util.List;

public class CandidateCageCreator {
	private static final String TAG = CandidateCageCreator.class.getName();

	private final GridGeneratingParameters gridGeneratingParameters;
	private final Matrix<Integer> correctValueMatrix;
	private final Matrix<Integer> cageIdMatrix;
	private CellCoordinates[] cellCoordinatesOfAllCellsInCage;
	private boolean debugLogging;
	private final OverlappingSubsetChecker overlappingSubsetChecker;
	private final CandidateCageCreatorParameters candidateCageCreatorParameters;

	public CandidateCageCreator(
			CandidateCageCreatorParameters candidateCageCreatorParameters) {
		this.candidateCageCreatorParameters = candidateCageCreatorParameters;
		gridGeneratingParameters = candidateCageCreatorParameters
				.getGridGeneratingParameters();
		correctValueMatrix = candidateCageCreatorParameters
				.getCorrectValueMatrix();
		cageIdMatrix = candidateCageCreatorParameters.getCageIdMatrix();
		overlappingSubsetChecker = candidateCageCreatorParameters
				.getOverlappingSubsetChecker();
	}

	public boolean cageTypeDoesNotFitAtCellCoordinates(CageType cageType,
			CellCoordinates originCell) {
		cellCoordinatesOfAllCellsInCage = cageType
				.getCellCoordinatesOfAllCellsInCage(originCell);
		if (cageIdMatrix
				.containsInvalidCellCoordinates(cellCoordinatesOfAllCellsInCage)) {
			return true;
		}
		// noinspection SimplifiableIfStatement
		if (cageIdMatrix.containsNonEmptyCell(cellCoordinatesOfAllCellsInCage)) {
			return true;
		}

		return hasOverlappingSubsetOfValues(cellCoordinatesOfAllCellsInCage);
	}

	private boolean hasOverlappingSubsetOfValues(
			CellCoordinates[] cellCoordinatesOfAllCellsInCage) {
		Matrix<Boolean> usedCellsForNewCageMatrix = new Matrix<Boolean>(
				correctValueMatrix.size(), false);
		usedCellsForNewCageMatrix.setValueToAllCellCoordinates(true,
				cellCoordinatesOfAllCellsInCage);

		if (debugLogging) {
			// Print solution, cage matrix and maskNewCage
			printCageCreationDebugInformation(usedCellsForNewCageMatrix);
		}

		return overlappingSubsetChecker.hasOverlap(cageIdMatrix,
				usedCellsForNewCageMatrix);
	}

	public CellCoordinates[] getCellsCoordinates() {
		return cellCoordinatesOfAllCellsInCage;
	}

	public CandidateCageCreator enableLogging(boolean enableLogging) {
		debugLogging = enableLogging;
		overlappingSubsetChecker.enableLogging(enableLogging);
		return this;
	}

	/**
	 * Print debug information for create cage process to logging.
	 * 
	 * @param maskNewCage
	 *            Mask of cage type which is currently processed.
	 */
	private void printCageCreationDebugInformation(Matrix<Boolean> maskNewCage) {
		Log.d(TAG, "   Checking candidate cage");
		String emptyCell = "  .";
		String usedCell = "  X";
		int gridSizeValue = correctValueMatrix.size();
		for (int row = 0; row < gridSizeValue; row++) {
			String line = "      ";
			for (int col = 0; col < gridSizeValue; col++) {
				line += " " + correctValueMatrix.get(row, col);
			}
			line += "   ";
			for (int col = 0; col < gridSizeValue; col++) {
				line += " "
						+ (cageIdMatrix.isEmpty(row, col) ? emptyCell : String
								.format("%03d", cageIdMatrix.get(row, col)));
			}
			if (maskNewCage != null) {
				line += "   ";
				for (int col = 0; col < gridSizeValue; col++) {
					line += " "
							+ (maskNewCage.isEmpty(row, col) ? emptyCell
									: usedCell);
				}
			}
			Log.d(TAG, line);
		}
	}

	public Cage create(int cageId, List<Cell> cells) {
		CageBuilder cageBuilder = new CageBuilder();
		cageBuilder.setId(cageId);

		cageBuilder.setCells(getAllCellIds(cells));

		CageOperatorGenerator cageOperatorGenerator = candidateCageCreatorParameters
				.createCageOperatorGenerator(getAllCorrectValues(cells));

		CageOperator cageOperator = cageOperatorGenerator.getCageOperator();
		cageBuilder.setCageOperator(cageOperator);
		cageBuilder.setHideOperator(gridGeneratingParameters.isHideOperators());
		cageBuilder.setResult(cageOperatorGenerator.getCageResult());

		Cage candidateCage = cageBuilder.build();

		// Finally check whether the number of permutations of possible
		// solutions for the cage is not to big in case the specific cell values
		// are unknown.
		ComboGenerator comboGenerator = candidateCageCreatorParameters
				.createComboGenerator();
		List<int[]> possibleCombos = comboGenerator.getPossibleCombos(
				candidateCage, cells);
		candidateCage.setPossibleCombos(possibleCombos);

		return candidateCage;
	}

	private int[] getAllCellIds(List<Cell> cells) {
		int[] ids = new int[cells.size()];
		int index = 0;
		for (Cell cell : cells) {
			ids[index++] = cell.getCellId();
		}
		return ids;
	}

	private int[] getAllCorrectValues(List<Cell> cells) {
		int[] correctValues = new int[cells.size()];
		int index = 0;
		for (Cell cell : cells) {
			correctValues[index++] = cell.getCorrectValue();
		}
		return correctValues;
	}
}
