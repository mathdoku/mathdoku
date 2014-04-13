package net.mathdoku.plus.gridgenerating.CellCoordinates;

public class NullCellCoordinates extends CellCoordinates {
	private static NullCellCoordinates singletonNullCellCoordinates = null;

	private NullCellCoordinates() {
	}

	public static NullCellCoordinates create() {
		if (singletonNullCellCoordinates == null) {
			singletonNullCellCoordinates = new NullCellCoordinates();
		}
		return singletonNullCellCoordinates;
	}

	@Override
	public boolean isNull() {
		return true;
	}
}
