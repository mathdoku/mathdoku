package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class SingeCellCageResult extends CageResult {
	static SingeCellCageResult tryToCreate(int... cellValues)
			throws InstantiationException {
		if (canBeCreated(cellValues)) {
			return new SingeCellCageResult(cellValues[0]);
		}
		throw new InstantiationException(String.format(
				"Cannot instantiate with specified values: %s",
				Arrays.toString(cellValues)));
	}

	private SingeCellCageResult(int cellValue) {
		super(CageOperator.NONE, cellValue);
	}

	@Override
	public int getResult() {
		return getCellValue(0);
	}

	private static boolean canBeCreated(int... cellValues) {
		return cellValues != null && cellValues.length == 1;
	}
}
