package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridgenerating.cageresult.CageResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class CageOperatorGenerator {
	private static final String TAG = CageOperatorGenerator.class.getName();

	private static final Logger LOGGER = Logger.getLogger(TAG);

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

		// Todo: remove next if statement when refactoring of grid generator is
		// completed. Removing this if statement alters the grid which is
		// generated as it results in an addition invocation of the randomizer.
		if (possibleCageResults.size() == 1
				&& possibleCageResults
						.entrySet()
						.iterator()
						.next()
						.getKey()
						.getCageOperator() == CageOperator.NONE) {
			return possibleCageResults.entrySet().iterator().next().getKey();
		}

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
		if (!possibleCageResults.isEmpty()) {
			// Todo: remove this return statement when refactoring of grid
			// generator is completed.
			// Removing this if statement alters the grid which is generated as
			// it results in an addition invocation of the randomizer.
			return;
		}
		addPossibleCageResult(CageOperator.DIVIDE,
				WEIGHT_CAGE_RESULT_WITH_DIVIDE_OPERATOR);
		addPossibleCageResult(CageOperator.SUBTRACT,
				WEIGHT_CAGE_RESULT_WITH_SUBTRACT_OPERATOR);

		// TODO: do not use different weights based on number of cells in cage.
		// This should be done after refactoring is done as it influences the
		// randomizer ans therefore changes the generated grid.
		addPossibleCageResultMultiply(
				CageOperator.MULTIPLY,
				cellValues.length == 2 ? WEIGHT_CAGE_RESULT_WITH_MULTIPLY_OPERATOR
						: 50);

		// TODO: do not use different weights based on number of cells in cage.
		// This should be done after refactoring is done as it influences the
		// randomizer ans therefore changes the generated grid.
		addPossibleCageResult(CageOperator.ADD,
				cellValues.length == 2 ? WEIGHT_CAGE_RESULT_WITH_ADD_OPERATOR
						: 50);
	}

	private void addPossibleCageResult(CageOperator cageOperator, int weight) {
		CageResult possibleCageResult = CageResult
				.tryToCreate(cageOperator, cellValues);
		if (possibleCageResult.isValid()) {
			possibleCageResults.put(possibleCageResult, weight);
		}
	}

	private void addPossibleCageResultMultiply(CageOperator cageOperator,
			int weight) {
		// Multiply
		CageResult cageResultMultiplyOperator = CageResult.tryToCreate(
				cageOperator, cellValues);
		if (cageResultMultiplyOperator.isValid()) {
			if (cageResultMultiplyOperator.getResult() <= gridGeneratingParameters
					.getMaxCageResult()) {
				possibleCageResults.put(cageResultMultiplyOperator, weight);
			} else {
				// Todo: it would be better to remove this branch as
				// multiplication for the current values is not allowed. But
				// this will change the generator result as the total weights of
				// the operators will change.
				LOGGER.log(Level.ALL,
						"GameSeed: %d. Operator MULTIPLY is not allowed as result of "
								+ "multiplication %d is above limit %d.",
						new Object[] { gridGeneratingParameters.getGameSeed(),
								cageResultMultiplyOperator.getResult(),
								gridGeneratingParameters.getMaxCageResult() });

				CageResult cageResultAddOperator = CageResult.tryToCreate(
						CageOperator.ADD, cellValues);
				if (cageResultAddOperator.isValid()) {
					possibleCageResults.put(cageResultAddOperator, weight);
				}
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
