package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.SolvingAttemptStatus;

/**
 * Mapping for records in database table SolvingAttemptRow. Note: it depends on
 * the specific query which was executed whether a field is filled.
 */
public class SolvingAttemptRow {
	private final int solvingAttemptId;

	// The grid to which the solving attempt applies. A single grid can have
	// multiple solving attempts. So the grid only contains the definition
	// (cages and cage outcomes) of this grid which are identical for all
	// solving attempts of this grid.
	private int gridId;

	private final long solvingAttemptDateCreated;
	private final long solvingAttemptDateUpdated;
	private final SolvingAttemptStatus solvingAttemptStatus;

	private final int savedWithRevision;
	private final String storageString;

	public SolvingAttemptRow(int solvingAttemptId, int gridId,
			long solvingAttemptDateCreated, long solvingAttemptDateUpdated,
			SolvingAttemptStatus solvingAttemptStatus, int savedWithRevision,
			String storageString) {
		this.solvingAttemptId = solvingAttemptId;
		this.gridId = gridId;
		this.solvingAttemptDateCreated = solvingAttemptDateCreated;
		this.solvingAttemptDateUpdated = solvingAttemptDateUpdated;
		this.solvingAttemptStatus = solvingAttemptStatus;
		this.savedWithRevision = savedWithRevision;
		this.storageString = storageString;
	}

	public int getSolvingAttemptId() {
		return solvingAttemptId;
	}

	public long getSolvingAttemptDateCreated() {
		return solvingAttemptDateCreated;
	}

	public long getSolvingAttemptDateUpdated() {
		return solvingAttemptDateUpdated;
	}

	public SolvingAttemptStatus getSolvingAttemptStatus() {
		return solvingAttemptStatus;
	}

	public int getSavedWithRevision() {
		return savedWithRevision;
	}

	public String getStorageString() {
		return storageString;
	}

	public int getGridId() {
		return gridId;
	}
}
