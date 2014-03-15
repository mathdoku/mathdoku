package net.mathdoku.plus.enums;

public enum GridSize {
	// Elements have to be ordered in order of increasing size.
	GRID_4X4(4), GRID_5X5(5), GRID_6X6(6), GRID_7X7(7), GRID_8X8(8), GRID_9X9(9);

	private final static GridSize SMALLEST = GridSize.values()[0];
	private final static GridSize BIGGEST = GridSize.values()[GridSize.values().length - 1];

	private final int size;
	private final String gridSizeText;

	private GridSize(int size) {
		this.size = size;
		gridSizeText = String.format("%dx%d", size, size);
	}

	public int getGridSize() {
		return size;
	}

	public String getGridSizeText() {
		return gridSizeText;
	}

	public static String[] getAllGridSizes() {
		GridSize[] gridSizes = GridSize.values();

		String[] gridSizeTextAll = new String[gridSizes.length];
		for (int i = 0; i < gridSizes.length; i++) {
			gridSizeTextAll[i] = gridSizes[i].getGridSizeText();
		}

		return gridSizeTextAll;
	}

	public static int toZeroBasedIndex(int gridSize) {
		return gridSize - SMALLEST.size;
	}

	public static GridSize fromZeroBasedIndex(int index) {
		return fromInteger(index + SMALLEST.size);
	}

	public static GridSize fromInteger(int gridSize) {
		if (gridSize < SMALLEST.size || gridSize > BIGGEST.size) {
			throw new IllegalArgumentException(String.format(
					"Size %d cannot be converted to a valid GridSize.",
					gridSize));
		}

		return GridSize.values()[toZeroBasedIndex(gridSize)];
	}

	public static int getSmallestGridSize() {
		return SMALLEST.size;
	}

	public static int getBiggestGridSize() {
		return BIGGEST.size;
	}
}
