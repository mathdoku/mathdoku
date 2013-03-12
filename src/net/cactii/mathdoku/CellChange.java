package net.cactii.mathdoku;

import java.util.ArrayList;

/**
 * The CellChange holds undo information for a GridCell.
 */
public class CellChange {
	// The cell for which the undo information is stored.
	private GridCell cell;

	// Properties of the GridCell which can be restored.
	private int previousUserValue;
	private ArrayList<Integer> previousPossibleValues;

	// Undo information for other cell which are changed as a result of changing
	// the cell.
	private ArrayList<CellChange> relatedCellChanges;

	/**
	 * Creates a new [@link #CellChange] instance.
	 * 
	 * @param cell
	 *            The cell to which the undo information is related.
	 * @param previousUserValue
	 *            The user value of the cell before it is changed.
	 * @param previousPossibleValues
	 *            The possible values of the cell before it is changed.
	 */
	public CellChange(GridCell cell, int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		this.cell = cell;
		this.previousUserValue = previousUserValue;
		this.previousPossibleValues = new ArrayList<Integer>(previousPossibleValues);
		this.relatedCellChanges = null;
	}

	/**
	 * Restores a GridCell using the undo information.
	 * 
	 * @return The grid cell for which a change was made undone.
	 */
	public GridCell restore() {
		if (this.relatedCellChanges != null) {
			// First Undo all related moves.
			for (CellChange relatedMove : this.relatedCellChanges) {
				relatedMove.restore();
			}
		}
		cell.Undo(this.previousUserValue, this.previousPossibleValues);

		return cell;
	}

	/**
	 * Relates cell changes which belong together. In case a cell change is
	 * restored, all its related cell changes will be restored as well.
	 * 
	 * @param relatedCellChange
	 *            The cell change which will be related to this cell change.
	 */
	public void addRelatedMove(CellChange relatedCellChange) {
		if (this.relatedCellChanges == null) {
			this.relatedCellChanges = new ArrayList<CellChange>();
		}
		this.relatedCellChanges.add(relatedCellChange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "<cell:" + this.cell.getCellNumber() + " col:"
				+ this.cell.getColumn() + " row:" + this.cell.getRow()
				+ " previous userval:" + this.previousUserValue
				+ " previous possible values:"
				+ previousPossibleValues.toString() + ">";
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's
		// specification.
		if (!(o instanceof CellChange)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		CellChange lhs = (CellChange) o;

		// Check each field. Primitive fields, reference fields, and nullable
		// reference
		// fields are all treated differently.
		return previousUserValue == lhs.previousUserValue
				&& cell.equals(lhs.cell)
				&& (previousPossibleValues == null ? lhs.previousPossibleValues == null
						: previousPossibleValues
								.equals(lhs.previousPossibleValues));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
