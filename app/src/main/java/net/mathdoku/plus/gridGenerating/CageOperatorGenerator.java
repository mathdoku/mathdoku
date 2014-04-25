package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridgenerating.cageresult.CageResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

class CageOperatorGenerator {
	private static final String TAG = CageOperatorGenerator.class.getName();

	private static final int WEIGHT_CAGE_RESULT_WITH_NONE_OPERATOR = 100;
	private static final int WEIGHT_CAGE_RESULT_WITH_DIVIDE_OPERATOR = 50;
	private static final int WEIGHT_CAGE_RESULT_WITH_SUBTRACT_OPERATOR = 30;
	private static final int WEIGHT_CAGE_RESULT_WITH_ADD_OPERATOR = 15;
	private static final int WEIGHT_CAGE_RESULT_WITH_MULTIPLY_OPERATOR = 15;

	private final GridGeneratingParameters gridGeneratingParameters;
	private final Random random;
	private final int[] cellValues;
	private Map<CageResult, Integer> possibleCageResults;
	private final CageResult cageResult;

	public CageOperatorGenerator(
			GridGeneratingParameters gridGeneratingParameters, Random random,
			int... cellValues) {
		this.gridGeneratingParameters = gridGeneratingParameters;
		this.random = random;
		this.cellValues = cellValues;
		cageResult = selectRandomCageOperator();
	}

	private CageResult selectRandomCageOperator() {
		addPossibleCageResults();

		int indexWeight = random.nextInt(getTotalWeightPossibleCageResults());
		int cumulativeWeight = 0;
		for (Map.Entry<CageResult, Integer> entry : possibleCageResults
				.entrySet()) {
			cumulativeWeight += entry.getValue();
			if (indexWeight < cumulativeWeight) {
				return entry.getKey();
			}
		}

		throw new IllegalStateException(
				"Cannot select a random cage operator as the random index is bigger than the total weight.");
	}

	private int getTotalWeightPossibleCageResults() {
		int summedWeight = 0;
		for (Map.Entry<CageResult, Integer> entry : possibleCageResults
				.entrySet()) {
			summedWeight += entry.getValue();
		}
		return summedWeight;
	}

	private void addPossibleCageResults() {
		/*
		 * The list of possible cage results contains all valid cage results and
		 * the cumulative relative weight which is used to select a cage result
		 * randomly.
		 */
		possibleCageResults = new LinkedHashMap<CageResult, Integer>();

		addPossibleCageResult(CageOperator.NONE,
				WEIGHT_CAGE_RESULT_WITH_NONE_OPERATOR);
		addPossibleCageResult(CageOperator.DIVIDE,
				WEIGHT_CAGE_RESULT_WITH_DIVIDE_OPERATOR);
		addPossibleCageResult(CageOperator.SUBTRACT,
				WEIGHT_CAGE_RESULT_WITH_SUBTRACT_OPERATOR);
		addPossibleCageResultMultiply(WEIGHT_CAGE_RESULT_WITH_MULTIPLY_OPERATOR);
		addPossibleCageResult(CageOperator.ADD,
				WEIGHT_CAGE_RESULT_WITH_ADD_OPERATOR);
	}

	private void addPossibleCageResult(CageOperator cageOperator, int weight) {
		if (CageResult.canBeCreated(cageOperator, cellValues)) {
			CageResult possibleCageResult = CageResult.create(cageOperator,
					cellValues);
			if (possibleCageResult.isValid()) {
				possibleCageResults.put(possibleCageResult, weight);
			}
		}
	}

	private void addPossibleCageResultMultiply(int weight) {
		if (CageResult.canBeCreated(CageOperator.MULTIPLY, cellValues)) {
			CageResult cageResultMultiplyOperator = CageResult.create(
					CageOperator.MULTIPLY, cellValues);
			if (cageResultMultiplyOperator.isValid()
					&& cageResultMultiplyOperator.getResult() <= gridGeneratingParameters
							.getMaxCageResult()) {
				possibleCageResults.put(cageResultMultiplyOperator, weight);
			}
		}
	}

	public CageOperator getCageOperator() {
		return cageResult.getCageOperator();
	}

	public int getCageResult() {
		return cageResult.getResult();
	}
}
