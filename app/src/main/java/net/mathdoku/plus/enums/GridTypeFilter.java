package net.mathdoku.plus.enums;

public enum GridTypeFilter {
	ALL, GRID_2X2(GridType.GRID_2X2), GRID_3X3(GridType.GRID_3X3), GRID_4X4(
			GridType.GRID_4x4), GRID_5X5(GridType.GRID_5X5), GRID_6X6(
			GridType.GRID_6X6), GRID_7X7(GridType.GRID_7X7), GRID_8X8(
			GridType.GRID_8X8), GRID_9X9(GridType.GRID_9X9);

	private GridType gridType;

	private GridTypeFilter() {
	}

	private GridTypeFilter(GridType gridType) {
		this.gridType = gridType;
	}

	public static GridTypeFilter fromGridSize(GridType gridType) {
		for (GridTypeFilter gridTypeFilter : values()) {
			if (gridTypeFilter.gridType == gridType) {
				return gridTypeFilter;
			}
		}
		throw new IllegalStateException(String.format(
				"GridType '%s' not found in GridTypeFilter.",
				gridType.toString()));
	}

	public int getGridType() {
		if (this == ALL) {
			throw new IllegalStateException(String.format(
					"Method should not be called om GridTypeFilter '%s' which has "
							+ "no grid size.", GridTypeFilter.ALL.toString()));
		}
		return gridType.getGridSize();
	}
}
