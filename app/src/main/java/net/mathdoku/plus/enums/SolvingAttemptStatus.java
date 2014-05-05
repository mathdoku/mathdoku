package net.mathdoku.plus.enums;

import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;

public enum SolvingAttemptStatus {
	// The solving attempt status id's of the enum values should not be altered
	// as these values are persisted. Each solving attempt status (except
	// UNDETERMINED) is attached to a single status filter. Each status filter
	// can be attached to multiple solving attempt statuses.
	UNDETERMINED(-1, null),
	//
	NOT_STARTED(0, GridDatabaseAdapter.StatusFilter.UNFINISHED),
	//
	UNFINISHED(50, GridDatabaseAdapter.StatusFilter.UNFINISHED),
	//
	FINISHED_SOLVED(100, GridDatabaseAdapter.StatusFilter.SOLVED),
	//
	REVEALED_SOLUTION(101, GridDatabaseAdapter.StatusFilter.REVEALED);

	private final int solvingAttemptStatusId;
	private final GridDatabaseAdapter.StatusFilter attachedToStatusFilter;

	private SolvingAttemptStatus(int solvingAttemptStatusId,
			GridDatabaseAdapter.StatusFilter attachedToStatusFilter) {
		this.solvingAttemptStatusId = solvingAttemptStatusId;
		this.attachedToStatusFilter = attachedToStatusFilter;
	}

	public int getId() {
		return solvingAttemptStatusId;
	}

	public GridDatabaseAdapter.StatusFilter getAttachedToStatusFilter() {
		return attachedToStatusFilter;
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
