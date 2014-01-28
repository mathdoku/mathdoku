package net.mathdoku.plus.enums;

import java.security.InvalidParameterException;

public enum PuzzleComplexity {
	// The puzzle complexity id's of the enum values should not be altered as
	// these
	// values are persisted.
	RANDOM(0), VERY_EASY(1), EASY(2), NORMAL(3), DIFFICULT(4), VERY_DIFFICULT(5);

	private final int mPuzzleComplexityId;

	private PuzzleComplexity(int puzzleComplexityId) {
		mPuzzleComplexityId = puzzleComplexityId;
	}

	static public PuzzleComplexity fromId(String puzzleComplexityId) {
		int id = -1;
		try {
			id = Integer.parseInt(puzzleComplexityId);
			switch (id) {
			case 0:
				return RANDOM;
			case 1:
				return VERY_EASY;
			case 2:
				return EASY;
			case 3:
				return NORMAL;
			case 4:
				return DIFFICULT;
			case 5:
				return VERY_DIFFICULT;
			default:
				throw new InvalidParameterException("Cannot convert id '"
						+ puzzleComplexityId + "' to a PuzzleComplexity");
			}
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("Cannot convert id '"
					+ puzzleComplexityId + "' to a PuzzleComplexity");
		}
	}

	public int getId() {
		return mPuzzleComplexityId;
	}
}
