package net.mathdoku.plus.enums;

public enum GridType {
    // Elements have to be ordered in order of increasing size. Size ALL is a
    // value which can can only be used in some size filter lists to indicate
    // that no selection should be made on size.
    GRID_2X2(2, GridTypeFilter.GRID_2X2),
    //
    GRID_3X3(3, GridTypeFilter.GRID_3X3),
    //
    GRID_4X4(4, GridTypeFilter.GRID_4X4),
    //
    GRID_5X5(5, GridTypeFilter.GRID_5X5),
    //
    GRID_6X6(6, GridTypeFilter.GRID_6X6),
    //
    GRID_7X7(7, GridTypeFilter.GRID_7X7),
    //
    GRID_8X8(8, GridTypeFilter.GRID_8X8),
    //
    GRID_9X9(9, GridTypeFilter.GRID_9X9);

    private static final GridType GRID_TYPE_WITH_SMALLEST_SIZE = GridType.values()[0];
    private static final GridType GRID_TYPE_WITH_BIGGEST_SIZE = GridType.values()[GridType.values().length - 1];

    private final int size;
    private final GridTypeFilter attachedToGridTypeFilter;

    private GridType(int size, GridTypeFilter attachedToGridTypeFilter) {
        this.size = size;
        this.attachedToGridTypeFilter = attachedToGridTypeFilter;
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
        if (gridSize < GRID_TYPE_WITH_SMALLEST_SIZE.size || gridSize > GRID_TYPE_WITH_BIGGEST_SIZE.size) {
            throw new IllegalArgumentException(
                    String.format("Size %d cannot be converted to a valid GridType.", gridSize));
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
        return GRID_TYPE_WITH_BIGGEST_SIZE.size - GRID_TYPE_WITH_SMALLEST_SIZE.size + 1;
    }

    public static GridType getFromNumberOfCells(int numberOfCells) {
        int gridSize = (int) Math.sqrt(numberOfCells);
        if (gridSize * gridSize != numberOfCells) {
            throw new IllegalArgumentException(
                    String.format("Cannot determine a valid grid size for number of cells equal to %d.",
                                  numberOfCells));
        }
        return fromInteger(gridSize);
    }

    public int getNumberOfCells() {
        return size * size;
    }

    public GridTypeFilter getAttachedToGridTypeFilter() {
        return attachedToGridTypeFilter;
    }

    public static GridType fromGridTypeFilter(GridTypeFilter gridTypeFilter) {
        GridType firstGridType = null;
        for (GridType gridType : values()) {
            if (gridType.getAttachedToGridTypeFilter() == gridTypeFilter) {
                if (firstGridType != null) {
                    throw new IllegalStateException(
                            String.format("Cannot determine grid type as GridTypeFilter '%s' is " + "attached " +
                                                  "to multiple grid types.", gridTypeFilter.name()));
                }
                firstGridType = gridType;
            }
        }
        if (firstGridType == null) {
            throw new IllegalStateException(String.format(
                    "Cannot determine grid type as GridTypeFilter '%s' is attached to multiple grid types.",
                    gridTypeFilter.name()));
        }
        return firstGridType;
    }
}
