package net.cactii.mathdoku;

import java.util.ArrayList;

public class Move {
	private GridCell cell;

	private int previousUserValue;
	private ArrayList<Integer> previousPossibleValues;

	public Move(GridCell cell, int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		this.cell = cell;
		this.previousUserValue = previousUserValue;
		this.previousPossibleValues = (ArrayList<Integer>) previousPossibleValues
				.clone();
	}

	public void Undo() {
		cell.Undo(this.previousUserValue, this.previousPossibleValues);
		cell.Select();
	}

	public String toString() {
		String str = "<cell:" + this.cell.getCellNumber() + " col:"
				+ this.cell.getColumn() + " row:" + this.cell.getRow()
				+ " previous userval:" + this.previousUserValue
				+ " previous possible values:"
				+ previousPossibleValues.toString() + ">";
		return str;
	}

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
		if (!(o instanceof Move)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		Move lhs = (Move) o;

		// Check each field. Primitive fields, reference fields, and nullable
		// reference
		// fields are all treated differently.
		return previousUserValue == lhs.previousUserValue
				&& cell.equals(lhs.cell)
				&& (previousPossibleValues == null ? lhs.previousPossibleValues == null
						: previousPossibleValues
								.equals(lhs.previousPossibleValues));
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
