package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CageResult {
	private static final String TAG = CageResult.class.getName();

	private int[] cellValues;
	private CageOperator cageOperator;

	CageResult(CageOperator cageOperator, int... cellValues) {
		this.cellValues = cellValues;
		this.cageOperator = cageOperator;
	}

	// Protected constructor for NullCageResult
	CageResult() {
	}

	public static CageResult tryToCreate(CageOperator cageOperator,
			int... cellValues) {
		if (cellValues == null) {
			throw new IllegalArgumentException(
					"Parameter cellValues cannot be null.");
		}

		try {
			switch (cageOperator) {
			case NONE:
				return SingeCellCageResult.tryToCreate(cellValues);
			case ADD:
				return AdditionCageResult.tryToCreate(cellValues);
			case SUBTRACT:
				return SubtractionCageResult.tryToCreate(cellValues);
			case MULTIPLY:
				return MultiplicationCageResult.tryToCreate(cellValues);
			case DIVIDE:
				return DivisionCageResult.tryToCreate(cellValues);
			}
		} catch (InstantiationException e) {
			Logger.getLogger(TAG).log(Level.WARNING, "Create new cage result failed", e);
			return NullCageResult.create();
		}

		throw new IllegalArgumentException(String.format(
				"Operator '%s' not allowed for cell values '%s'.",
				cageOperator.toString(), Arrays.toString(cellValues)));
	}

	public abstract int getResult();

	public CageOperator getCageOperator() {
		return cageOperator;
	}

	int getCellValue(int index) {
		return cellValues[index];
	}

	int[] getCellValues() {
		return cellValues;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean isNull() {
		return false;
	}

	public boolean isValid() {
		return !isNull();
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CageResult{");
		sb.append("cellValues=").append(Arrays.toString(cellValues));
		sb.append(", cageOperator=").append(cageOperator);
		sb.append('}');
		return sb.toString();
	}
}
