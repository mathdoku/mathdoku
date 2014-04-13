package net.mathdoku.plus.gridgenerating.CellCoordinates;

public class CellCoordinates {
	public static final CellCoordinates EMPTY = NullCellCoordinates.create();
	private final int row;
	private final int column;

	public static boolean canBeCreated(int row, int column) {
		return (row >= 0 && column >= 0);
	}

	public CellCoordinates(int row, int column) {
		if (row < 0) {
			throw new IllegalArgumentException(
					"Row should be greater than or equal to 0.");
		}
		if (column < 0) {
			throw new IllegalArgumentException(
					"Column should be greater than or equal to 0.");
		}
		this.row = row;
		this.column = column;
	}

	// Package private no argument constructor is needed by the
	// NullCellCoordinates.
	CellCoordinates() {
		row = -1;
		column = -1;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isNull() {
		return false;
	}

	public boolean isNotNull() {
		return !isNull();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CellCoordinates{");
		sb.append("row=").append(row);
		sb.append(", column=").append(column);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CellCoordinates)) {
			return false;
		}

		CellCoordinates that = (CellCoordinates) o;

		if (column != that.column) {
			return false;
		}
		if (row != that.row) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = row;
		result = 31 * result + column;
		return result;
	}
}
