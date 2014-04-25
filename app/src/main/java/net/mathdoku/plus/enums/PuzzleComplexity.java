package net.mathdoku.plus.enums;

public enum PuzzleComplexity {
	// The puzzle complexity id's of the enum values should not be altered as
	// these values are persisted.
	RANDOM(0), VERY_EASY(1), EASY(2), NORMAL(3), DIFFICULT(4), VERY_DIFFICULT(5);

	@SuppressWarnings("unused")
	private static final String TAG = PuzzleComplexity.class.getName();

	private final int mPuzzleComplexityId;

	private PuzzleComplexity(int puzzleComplexityId) {
		mPuzzleComplexityId = puzzleComplexityId;
	}

	public static PuzzleComplexity fromId(String puzzleComplexityId)
			throws IllegalArgumentException {
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
				throw new IllegalArgumentException(
						getErrorStringCannotConvertIdToPuzzleComplexity(puzzleComplexityId));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					getErrorStringCannotConvertIdToPuzzleComplexity(puzzleComplexityId),
					e);
		}
	}

	private static String getErrorStringCannotConvertIdToPuzzleComplexity(
			String puzzleComplexityId) {
		return String.format("Cannot convert id '%s' to a PuzzleComplexity",
				puzzleComplexityId);
	}

	public int getId() {
		return mPuzzleComplexityId;
	}
}
