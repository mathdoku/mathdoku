package net.mathdoku.plus.enums;

public enum GridType {
	// Elements have to be ordered in order of increasing size. Size ALL is a
	// value which can can only be used in some size filter lists to indicate
	// that no selection should be made on size.
	GRID_2X2(2), GRID_3X3(3), GRID_4x4(4), GRID_5X5(5), GRID_6X6(6), GRID_7X7(7), GRID_8X8(
			8), GRID_9X9(9);

	private final static GridType GRID_TYPE_WITH_SMALLEST_SIZE = GridType
			.values()[0];
	private final static GridType GRID_TYPE_WITH_BIGGEST_SIZE = GridType
			.values()[GridType.values().length - 1];

	private final int size;

	private GridType(int size) {
		this.size = size;
	}

	public int getGridSize() {
		return size;
	}

	public int toZeroBasedIndex() {
		return toZeroBasedIndex(size);
	}

	public static int toZeroBasedIndex(int gridSize) {
		return gridSize - GRID_TYPE_WITH_SMALLEST_SIZE.size;
	}

	public static GridType fromZeroBasedIndex(int index) {
		return fromInteger(index + GRID_TYPE_WITH_SMALLEST_SIZE.size);
	}

	public static GridType fromInteger(int gridSize) {
		if (gridSize < GRID_TYPE_WITH_SMALLEST_SIZE.size
				|| gridSize > GRID_TYPE_WITH_BIGGEST_SIZE.size) {
			throw new IllegalArgumentException(String.format(
					"Size %d cannot be converted to a valid GridType.",
					gridSize));
		}

		return GridType.values()[toZeroBasedIndex(gridSize)];
	}

	public static int getSmallestGridSize() {
		return GRID_TYPE_WITH_SMALLEST_SIZE.size;
	}

	public static int getBiggestGridSize() {
		return GRID_TYPE_WITH_BIGGEST_SIZE.size;
	}

	public static int getGridSizeRange() {
		return GRID_TYPE_WITH_BIGGEST_SIZE.size
				- GRID_TYPE_WITH_SMALLEST_SIZE.size + 1;
	}

	public static GridType getFromNumberOfCells(int numberOfCells) {
		int gridSize = (int) Math.sqrt(numberOfCells);
		if (gridSize * gridSize != numberOfCells) {
			throw new IllegalArgumentException(
					String
							.format("Cannot determine a valid grid size for number of cells equal to %d.",
									numberOfCells));
		}
		return fromInteger(gridSize);
	}

	public int getNumberOfCells() {
		return size * size;
	}
}
