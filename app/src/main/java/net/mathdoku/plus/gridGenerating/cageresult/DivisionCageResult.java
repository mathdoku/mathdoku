package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class DivisionCageResult extends CageResult {
	static DivisionCageResult tryToCreate(int... cellValues)
			throws InstantiationException {
		if (canBeCreated(cellValues)) {
			return new DivisionCageResult(cellValues[0], cellValues[1]);
		}
		throw new InstantiationException(String.format(
				"Cannot instantiate with specified values: %s",
				Arrays.toString(cellValues)));
	}

	private static boolean canBeCreated(int... cellValues) {
		return hasCorrectNumberOfCells(cellValues) && canDivide(cellValues);
	}

	private static boolean canDivide(int[] cellValues) {
		return Math.max(cellValues[0], cellValues[1])
				% Math.min(cellValues[0], cellValues[1]) == 0;
	}

	private static boolean hasCorrectNumberOfCells(int[] cellValues) {
		return cellValues != null && cellValues.length == 2;
	}

	private static int getResult(int cellValue1, int cellValue2) {
		return Math.max(cellValue1, cellValue2)
				/ Math.min(cellValue1, cellValue2);
	}

	private DivisionCageResult(int cellValue1, int cellValue2) {
		super(CageOperator.DIVIDE, cellValue1, cellValue2);
	}

	@Override
	public int getResult() {
		return getResult(getCellValue(0), getCellValue(1));
	}
}
