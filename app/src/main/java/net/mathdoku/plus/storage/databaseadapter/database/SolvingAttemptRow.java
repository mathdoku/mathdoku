package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.enums.SolvingAttemptStatus;

/**
 * Mapping for records in database table SolvingAttemptRow. Note: it depends on the
 * specific query which was executed whether a field is filled.
 */
public class SolvingAttemptRow {
	// Unique row id for the solving attempt in the database.
	public int mId;

	// The grid to which the solving attempt applies. A single grid can have
	// multiple solving attempt. So the grid only contains the definition (cages
	// and cage outcomes) of this grid which are identical for all solving
	// attempts of this grid.
	public int mGridId;

	// Timestamp of creation and last update of the solving attempt.
	public long mDateCreated;
	public long mDateUpdated;

	// The revision of the app used to save the data.
	public int mSavedWithRevision;

	// The compound storage string
	public String mStorageString;

	// Status of the solving attempt.
	public SolvingAttemptStatus mSolvingAttemptStatus;
}
