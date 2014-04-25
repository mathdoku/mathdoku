package net.mathdoku.plus.puzzle.cellchange;

import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.storage.CellChangeStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * The CellChange holds undo information for a Cell.
 */
public class CellChange {
	@SuppressWarnings("unused")
	private static final String TAG = CellChange.class.getName();

	// The cell for which the undo information is stored.
	private Cell mCell;

	// Properties of the Cell which can be restored.
	private int mPreviousEnteredValue;
	private final List<Integer> mPreviousPossibleValues;

	// Undo information for other cell which are changed as a result of changing
	// the cell.
	private List<CellChange> mRelatedCellChanges;

	/**
	 * Creates a new [@link #CellChange] instance.
	 * 
	 * @param cell
	 *            The cell to which the undo information is related.
	 * @param previousEnteredValue
	 *            The entered value of the cell before it is changed.
	 * @param previousPossibleValues
	 *            The possible values of the cell before it is changed.
	 */
	public CellChange(Cell cell, int previousEnteredValue,
			List<Integer> previousPossibleValues) {
		mCell = cell;
		mPreviousEnteredValue = previousEnteredValue;
		mPreviousPossibleValues = new ArrayList<Integer>(previousPossibleValues);
		mRelatedCellChanges = null;
	}

	public CellChange(Cell cell) {
		mCell = cell;
		mPreviousEnteredValue = cell.getEnteredValue();
		mPreviousPossibleValues = new ArrayList<Integer>(cell.getPossibles());
		mRelatedCellChanges = null;
	}

	public CellChange(CellChangeStorage cellChangeStorage) {
		mCell = cellChangeStorage.getCell();
		mPreviousEnteredValue = cellChangeStorage.getPreviousEnteredValue();
		mPreviousPossibleValues = cellChangeStorage.getPreviousPossibleValues();
		mRelatedCellChanges = cellChangeStorage.getRelatedCellChanges();
	}

	/**
	 * Restores the entered value and maybe values of a Cell using the undo
	 * information.
	 */
	public void restore() {
		if (this.mRelatedCellChanges != null) {
			// First Undo all related moves.
			for (CellChange relatedMove : this.mRelatedCellChanges) {
				relatedMove.restore();
			}
		}
		// Always restore the entered value as value 0 indicates an empty cell.
		mCell.setEnteredValue(mPreviousEnteredValue);
		if (mPreviousPossibleValues != null) {
			for (int possible : mPreviousPossibleValues) {
				mCell.addPossible(possible);
			}
		}
	}

	/**
	 * Relates cell changes which belong together. In case a cell change is
	 * restored, all its related cell changes will be restored as well.
	 * 
	 * @param relatedCellChange
	 *            The cell change which will be related to this cell change.
	 */
	public void addRelatedMove(CellChange relatedCellChange) {
		if (this.mRelatedCellChanges == null) {
			this.mRelatedCellChanges = new ArrayList<CellChange>();
		}
		this.mRelatedCellChanges.add(relatedCellChange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<cell:" + this.mCell.getCellId() + " col:"
				+ this.mCell.getColumn() + " row:" + this.mCell.getRow()
				+ " previous entered value:" + this.mPreviousEnteredValue
				+ " previous possible values:"
				+ mPreviousPossibleValues.toString() + ">";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("all") // Needed to suppress sonar warning on cyclomatic complexity
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
		return mPreviousEnteredValue == lhs.mPreviousEnteredValue
				&& mCell.equals(lhs.mCell)
				&& (mPreviousPossibleValues == null ? lhs.mPreviousPossibleValues == null
						: mPreviousPossibleValues
								.equals(lhs.mPreviousPossibleValues));
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

	public Cell getCell() {
		return mCell;
	}

	public Integer getPreviousEnteredValue() {
		return mPreviousEnteredValue;
	}

	public List<Integer> getPreviousPossibleValues() {
		// Return copy of list of previous possible values so the requesting
		// object cannot manipulate the original list.
		return new ArrayList(mPreviousPossibleValues);
	}

	public List<CellChange> getRelatedCellChanges() {
		// Return copy of list of related cell changes so the requesting
		// object cannot manipulate the original list.
		return mRelatedCellChanges == null ? null : new ArrayList(
				mRelatedCellChanges);
	}
}
