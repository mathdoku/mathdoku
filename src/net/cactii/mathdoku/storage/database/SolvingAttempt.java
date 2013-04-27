package net.cactii.mathdoku.storage.database;

/**
 * Mapping for records in database table SolvingAttempt. Note: it depends on the
 * specific query which was executed whether a field is filled.
 * 
 */
public class SolvingAttempt {

	// Unique row id for the grid in the database.
	public int mId;

	// The definition (cages and cage outcomes) of this grid
	public int mGridId;

	// Timestamp of creation and last update
	public long mDateCreated;
	public long mDateUpdated;
}
