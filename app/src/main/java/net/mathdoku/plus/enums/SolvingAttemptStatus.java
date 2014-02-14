package net.mathdoku.plus.enums;

public enum SolvingAttemptStatus {
	// The solving attempt status id's of the enum values should not be altered
	// as these values are persisted.
	UNDETERMINED(-1), NOT_STARTED(0), UNFINISHED(50), FINISHED_SOLVED(100), REVEALED_SOLUTION(
			101);

	private final int mSolvingAttemptStatusId;

	private SolvingAttemptStatus(int solvingAttemptStatusId) {
		mSolvingAttemptStatusId = solvingAttemptStatusId;
	}

	public int getId() {
		return mSolvingAttemptStatusId;
	}

	/**
	 * Gets the derived status of a solving attempt.
	 * 
	 * @param isRevealed
	 *            True is the solution of the solving attempt is revealed. False
	 *            otherwise.
	 * @param isActive
	 *            True is the solving attempt is active. False otherwise.
	 * @param isEmpty
	 *            True is no user values and no maybe values are filled in for
	 *            the solving attempt. False otherwise.
	 * @return The status of the solving attempt.
	 */
	public static SolvingAttemptStatus getDerivedStatus(boolean isRevealed,
			boolean isActive, boolean isEmpty) {
		if (isRevealed) {
			return REVEALED_SOLUTION;
		}

		if (!isActive) {
			return FINISHED_SOLVED;
		}

		if (isEmpty) {
			return NOT_STARTED;
		}

		return UNFINISHED;
	}
}
