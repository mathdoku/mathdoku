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
	private final int gridId;

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

	public SolvingAttemptRow(SolvingAttemptRow source, int newSolvingAttemptId) {
		this(newSolvingAttemptId, source.gridId,
				source.solvingAttemptDateCreated,
				source.solvingAttemptDateUpdated, source.solvingAttemptStatus,
				source.savedWithRevision, source.storageString);
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SolvingAttemptRow{");
		sb.append("solvingAttemptId=")
				.append(solvingAttemptId);
		sb.append(", gridId=")
				.append(gridId);
		sb.append(", solvingAttemptDateCreated=")
				.append(solvingAttemptDateCreated);
		sb.append(", solvingAttemptDateUpdated=")
				.append(solvingAttemptDateUpdated);
		sb.append(", solvingAttemptStatus=")
				.append(solvingAttemptStatus);
		sb.append(", savedWithRevision=")
				.append(savedWithRevision);
		sb.append(", storageString='")
				.append(storageString)
				.append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	@SuppressWarnings("all")
	// Needed to suppress sonar warning on cyclomatic complexity
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SolvingAttemptRow)) {
			return false;
		}

		SolvingAttemptRow that = (SolvingAttemptRow) o;

		if (gridId != that.gridId) {
			return false;
		}
		if (savedWithRevision != that.savedWithRevision) {
			return false;
		}
		if (solvingAttemptDateCreated != that.solvingAttemptDateCreated) {
			return false;
		}
		if (solvingAttemptDateUpdated != that.solvingAttemptDateUpdated) {
			return false;
		}
		if (solvingAttemptId != that.solvingAttemptId) {
			return false;
		}
		if (solvingAttemptStatus != that.solvingAttemptStatus) {
			return false;
		}
		if (storageString != null ? !storageString.equals(
				that.storageString) : that.storageString != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = solvingAttemptId;
		result = 31 * result + gridId;
		result = 31 * result + (int) (solvingAttemptDateCreated ^ solvingAttemptDateCreated >>>
				32);
		result = 31 * result + (int) (solvingAttemptDateUpdated ^ solvingAttemptDateUpdated >>>
				32);
		result = 31 * result + solvingAttemptStatus.hashCode();
		result = 31 * result + savedWithRevision;
		result = 31 * result + (storageString != null ? storageString.hashCode() : 0);
		return result;
	}
}
