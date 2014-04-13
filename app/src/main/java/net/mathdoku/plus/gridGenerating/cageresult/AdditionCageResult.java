package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class AdditionCageResult extends CageResult {
	static AdditionCageResult tryToCreate(int... cellValues)
			throws InstantiationException {
		if (canBeCreated(cellValues)) {
			return new AdditionCageResult(cellValues);
		}
		throw new InstantiationException(String.format(
				"Cannot instantiate with specified values: %s",
				Arrays.toString(cellValues)));
	}

	private static boolean canBeCreated(int... cellValues) {
		return cellValues != null && cellValues.length >= 2;
	}

	private AdditionCageResult(int... cellValues) {
		super(CageOperator.ADD, cellValues);
	}

	@Override
	public int getResult() {
		int result = 0;
		for (int cellValue : getCellValues()) {
			result += cellValue;
		}

		return result;
	}
}
