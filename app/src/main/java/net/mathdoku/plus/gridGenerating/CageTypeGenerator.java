package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CageTypeGenerator {
    @SuppressWarnings("unused")
    private static final String TAG = CageTypeGenerator.class.getName();

    /**
     * Size of largest cages to be generated. Be careful with adjusting this value as the number of cage types growth
     * exponentially. Generating ALL cage types for a given size can take long.
     * <p/>
     * <p/>
     * <pre>
     * 	MaxSize    #cage_types     #cumulative
     *        1              1               1
     *        2              2               3
     *        3              6               9
     *        4             19              28
     *        5             63              91
     *        6            216             307
     *        7            760           1,067
     *        8          2,725           3,792
     *        9          9,910          13,702
     * </pre>
     */
    public static final int MAX_SIZE_STANDARD_CAGE_TYPE = 5;
    private static final CageType SINGLE_CELL_CAGE_TYPE = createSingleCellCageType();

    private static CageTypeGenerator cageTypeGeneratorSingletonInstance = null;
    private final CageTypeList cageTypeList;

    private class CageTypeList extends ArrayList<CageType> {
        public boolean addUnique(CageType cageType) {
            if (cageType != null && !contains(cageType)) {
                return add(cageType);
            }
            return false;
        }

        public void addAllUnique(CageTypeList cageTypeList) {
            for (CageType cageType : cageTypeList) {
                add(cageType);
            }
        }

        public CageTypeList filterOnHeightAndWidth(int maxHeight, int maxWidth) {
            CageTypeList filteredCageTypeList = new CageTypeList();
            for (CageType cageType : this) {
                if (cageType.getHeight() <= maxHeight && cageType.getWidth() <= maxWidth) {
                    filteredCageTypeList.addUnique(cageType);
                }
            }

            return filteredCageTypeList;
        }
    }

    private CageTypeGenerator() {
        cageTypeList = new CageTypeList();
        cageTypeList.addUnique(SINGLE_CELL_CAGE_TYPE);
        for (int cageSize = 2; cageSize <= MAX_SIZE_STANDARD_CAGE_TYPE; cageSize++) {
            cageTypeList.addAllUnique(createMultipleCellCageTypes(cageSize));
        }

        logNumberOfGeneratedCageTypes();
    }

    private static CageType createSingleCellCageType() {
        boolean[][] singleCellMatrix = new boolean[1][1];
        singleCellMatrix[0][0] = true;
        return new CageType(singleCellMatrix);
    }

    private CageTypeList createMultipleCellCageTypes(int targetCageSize) {
        CageTypeList cageTypeListTargetCageSize = new CageTypeList();
        for (CageType cageType : getCageTypesWithSize(targetCageSize - 1)) {
            addCageTypesToListByExtending(cageTypeListTargetCageSize, cageType);
        }
        return cageTypeListTargetCageSize;
    }

    private CageTypeList addCageTypesToListByExtending(CageTypeList cageTypeList, CageType cageType) {
        boolean[][] newCageTypeMatrix = cageType.getExtendedCageTypeMatrix();
        for (int row = 0; row < newCageTypeMatrix.length; row++) {
            for (int column = 0; column < newCageTypeMatrix[row].length; column++) {
                if (newCageTypeMatrix[row][column]) {
                    // This cell was already occupied in the original grid. Fill
                    // each free cell above, right, below or left to create a
                    // new cage type.

                    addCageTypeToListByExtendingToRowColumn(cageTypeList, newCageTypeMatrix, row - 1, column);
                    addCageTypeToListByExtendingToRowColumn(cageTypeList, newCageTypeMatrix, row, column + 1);
                    addCageTypeToListByExtendingToRowColumn(cageTypeList, newCageTypeMatrix, row + 1, column);
                    addCageTypeToListByExtendingToRowColumn(cageTypeList, newCageTypeMatrix, row, column - 1);
                }
            }
        }
        return cageTypeList;
    }

    private void addCageTypeToListByExtendingToRowColumn(CageTypeList cageTypeList, boolean[][] newCageTypeMatrix,
                                                         int row, int column) {
        CageType cageType = createCageTypeByExtendingToRowColumn(newCageTypeMatrix, row, column);
        boolean added = cageTypeList.addUnique(cageType);
        if (GridGenerator.DEBUG_NORMAL && added) {
            Log.i(TAG, "Found a new cage type:\n" + cageType.toString());
        }
    }

    private CageType createCageTypeByExtendingToRowColumn(boolean[][] newCageTypeMatrix, int row, int column) {
        if (!newCageTypeMatrix[row][column]) {
            newCageTypeMatrix[row][column] = true;
            CageType cageType = new CageType(newCageTypeMatrix);
            newCageTypeMatrix[row][column] = false;

            return cageType;
        } else {
            return null;
        }
    }

    private void logNumberOfGeneratedCageTypes() {
        if (GridGenerator.DEBUG_FULL) {
            int maxCageSize = 0;
            for (CageType cageType : cageTypeList) {
                maxCageSize = Math.max(maxCageSize, cageType.size());
            }
            int[] countCageTypesPerSize = new int[maxCageSize];
            for (CageType cageType : cageTypeList) {
                countCageTypesPerSize[cageType.size() - 1]++;
            }
            for (int i = 0; i < countCageTypesPerSize.length; i++) {
                Log.i(TAG, String.format("Number of cage types with %d cells: %d", i + 1, countCageTypesPerSize[i]));
            }
        }
    }

    /**
     * Get an instance to the singleton instance of the cage type generator.
     * <p/>
     * The {@link CageTypeGenerator} containing cage types having minimum 1 and maximum {@value
     * #MAX_SIZE_STANDARD_CAGE_TYPE} cells. Cages of bigger size have to generated with {@link #getRandomCageType(int,
     * int, int, java.util.Random)}.
     *
     * @return The singleton instance for the cage type generator.
     */
    public static CageTypeGenerator getInstance() {
        if (cageTypeGeneratorSingletonInstance == null) {
            cageTypeGeneratorSingletonInstance = new CageTypeGenerator();
        }
        return cageTypeGeneratorSingletonInstance;
    }

    public CageType getSingleCellCageType() {
        return SINGLE_CELL_CAGE_TYPE;
    }

    public List<CageType> getCageTypesWithSizeEqualOrLessThan(int maxCageSize) {
        return getCageTypesWithSizeInRange(1, maxCageSize);
    }

    private List<CageType> getCageTypesWithSizeInRange(int minCageSize, int maxCageSize) {
        List<CageType> cageTypes = new ArrayList<CageType>();
        for (CageType cageType : cageTypeList) {
            if (cageType.size() >= minCageSize && cageType.size() <= maxCageSize) {
                cageTypes.add(cageType);
            }
        }
        return cageTypes;
    }

    /**
     * Get a random cage type of the requested size. It is possible to retrieve a cage type of a bigger size than is
     * pre-generated.
     *
     * @param numberOfCells
     *         The number of cells the cage consists of.
     * @param maxHeight
     *         The maximum height 0f the cell. Use 0 in case width does not matter.
     * @param maxWidth
     *         The maximum width 0f the cell. Use 0 in case width does not matter.
     * @param random
     *         The random generator to use for randomized decisions.
     * @return A cage type.
     */
    public CageType getRandomCageType(int numberOfCells, int maxHeight, int maxWidth, Random random) {
        validateGetRandomCageTypeParameters(numberOfCells, maxHeight, maxWidth);

        if (numberOfCells <= MAX_SIZE_STANDARD_CAGE_TYPE) {
            // Cage types of the requested size are already generated
            return selectRandomCageTypeFromList(numberOfCells, maxWidth, maxHeight, random);
        } else {
            return deriveNewCageType(numberOfCells, maxHeight, maxWidth, random);
        }
    }

    private void validateGetRandomCageTypeParameters(int numberOfCells, int maxHeight, int maxWidth) {
        if (numberOfCells <= 0) {
            throw new IllegalArgumentException(
                    String.format("Number of cells (%d) for cage is invalid", numberOfCells));
        }
        if (maxWidth <= 0) {
            throw new IllegalArgumentException(String.format("Maximum width (%d) for cage is invalid", maxWidth));
        }
        if (maxHeight <= 0) {
            throw new IllegalArgumentException(String.format("Maximum height (%d) for cage is invalid", maxHeight));
        }
        if (maxWidth * maxHeight < numberOfCells) {
            throw new IllegalArgumentException(
                    String.format("Number of cells (%d) does not fit in available space (%d x " + "%d)", numberOfCells,
                                  maxHeight, maxWidth));
        }
    }

    private CageType selectRandomCageTypeFromList(int cageSize, int maxWidth, int maxHeight, Random random) {
        RandomListItemSelector<CageType> randomCageTypeSelector;
        randomCageTypeSelector = new RandomListItemSelector<CageType>(random, getCageTypesWithSize(cageSize));
        do {
            CageType cageType = randomCageTypeSelector.next();
            if (cageType.getWidth() <= maxWidth && cageType.getHeight() <= maxHeight) {
                return cageType;
            }
        } while (!randomCageTypeSelector.isEmpty());

        return null;
    }

    private List<CageType> getCageTypesWithSize(int cageSize) {
        return getCageTypesWithSizeInRange(cageSize, cageSize);
    }

    private CageType deriveNewCageType(int numberOfCells, int maxHeight, int maxWidth, Random random) {
        CageType baseCageType = selectRandomCageTypeFromList(MAX_SIZE_STANDARD_CAGE_TYPE, maxWidth, maxHeight, random);
        if (baseCageType != null) {
            return deriveNewCageType(baseCageType, numberOfCells, maxWidth, maxHeight, random);
        } else {
            return null;
        }
    }

    private CageType deriveNewCageType(CageType baseCageType, int numberOfCells, int maxWidth, int maxHeight,
                                       Random random) {
        CageTypeList derivedCageTypes = addCageTypesToListByExtending(new CageTypeList(),
                                                                      baseCageType).filterOnHeightAndWidth(maxHeight,
                                                                                                           maxWidth);
        if (derivedCageTypes.isEmpty()) {
            return null;
        }

        RandomListItemSelector<CageType> randomListItemSelector;
        randomListItemSelector = new RandomListItemSelector<CageType>(random, derivedCageTypes);
        CageType randomCageType = randomListItemSelector.next();
        if (randomCageType.size() == numberOfCells) {
            return randomCageType;
        } else {
            // Use the new cage type as basis to extend with another cell.
            return deriveNewCageType(randomCageType, numberOfCells, maxWidth, maxHeight, random);
        }
    }
}
