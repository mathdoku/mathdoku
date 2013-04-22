package net.cactii.mathdoku.storage.database;

import net.cactii.mathdoku.GridGenerating.GridGeneratingParameters;

/**
 * Mapping for records in database table Grid
 *
 */
public class GridRow {

	// Unique row id for the grid in the database.
	public int mId;

	// The definition (cages and cage outcomes) of this grid
	public String mDefinition;

	// Size of the grid
	public int mGridSize;

	// Timestamp of creation
	public long mDateCreated;

	// Parameters used to generate the grid.
	public GridGeneratingParameters mGridGeneratingParameters;
}
