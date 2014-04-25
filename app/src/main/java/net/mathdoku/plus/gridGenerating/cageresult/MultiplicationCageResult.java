package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class MultiplicationCageResult extends CageResult {
	private MultiplicationCageResult(int... cellValues) {
		super(CageOperator.MULTIPLY, cellValues);
	}

	static MultiplicationCageResult tryToCreate(int... cellValues) {
		if (canBeCreated(cellValues)) {
			return new MultiplicationCageResult(cellValues);
		}
		throw new IllegalStateException(String.format(
				"Cannot instantiate with specified values: %s",
				Arrays.toString(cellValues)));
	}

	public static boolean canBeCreated(int... cellValues) {
		return cellValues != null && cellValues.length >= 2;
	}

	@Override
	public int getResult() {
		int result = 1;
		for (int cellValue : getCellValues()) {
			result *= cellValue;
		}

		return result;
	}
}
