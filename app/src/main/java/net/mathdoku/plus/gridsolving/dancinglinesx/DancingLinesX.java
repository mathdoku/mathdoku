package net.mathdoku.plus.gridsolving.dancinglinesx;

import android.util.Log;

import net.mathdoku.plus.gridsolving.GridSolver;

import java.util.ArrayList;
import java.util.List;

public class DancingLinesX {
    @SuppressWarnings("unused")
    private static final String TAG = DancingLinesX.class.getName();

    public enum SolveType {
        ONE,
        MULTIPLE,
        ALL
    }

    private final DancingLinesXColumn root = new DancingLinesXColumn();
    private DancingLinesXColumn[] colHeaders;
    private DancingLinesXNode[] nodes;
    private int numberOfNodesAllocated;
    private DancingLinesXNode lastNodeAdded;
    private List<Integer> trySolution;
    private List<Integer> foundSolution;
    private int countSolutions;
    private boolean isValid;
    private int previousRowIndex = -1;
    private SolveType solveType;

    private int complexity;

    public DancingLinesX() {
        trySolution = new ArrayList<Integer>();
        isValid = true;
    }

    public void init(int numCols, int numNodes) {
        colHeaders = new DancingLinesXColumn[numCols + 1];
        for (int c = 1; c <= numCols; c++) {
            colHeaders[c] = new DancingLinesXColumn();
        }

        nodes = new DancingLinesXNode[numNodes + 1];
        this.numberOfNodesAllocated = 0;

        DancingLinesXColumn prev = root;
        for (int i = 1; i <= numCols; i++) {
            prev.setRight(colHeaders[i]);
            colHeaders[i].setLeft(prev);
            prev = colHeaders[i];
        }
        root.setLeft(colHeaders[numCols]);
        colHeaders[numCols].setRight(root);
    }

    public int getRowsInSolution() {
        return foundSolution.size();
    }

    public int getSolutionRow(int row) {
        return foundSolution.get(row - 1);
    }

    private void coverCol(DancingLinesXColumn coverCol) {
        DancingLinesX2DNode i, j;
        coverCol.getRight()
                .setLeft(coverCol.getLeft());
        coverCol.getLeft()
                .setRight(coverCol.getRight());

        i = coverCol.getDown();
        while (i != coverCol) {
            j = i.getRight();
            while (j != i) {
                j.getDown()
                        .setUp(j.getUp());
                j.getUp()
                        .setDown(j.getDown());
                ((DancingLinesXNode) j).getColumn()
                        .decreaseSize();
                j = j.getRight();
            }
            i = i.getDown();
        }
    }

    private void uncoverCol(DancingLinesXColumn uncoverCol) {
        DancingLinesX2DNode i, j;

        i = uncoverCol.getUp();
        while (i != uncoverCol) {
            j = i.getLeft();
            while (j != i) {
                ((DancingLinesXNode) j).getColumn()
                        .increaseSize();
                j.getDown()
                        .setUp(j);
                j.getUp()
                        .setDown(j);
                j = j.getLeft();
            }
            i = i.getUp();
        }
        uncoverCol.getRight()
                .setLeft(uncoverCol);
        uncoverCol.getLeft()
                .setRight(uncoverCol);
    }

    private DancingLinesXColumn chooseMinCol() {
        int minSize = Integer.MAX_VALUE;
        DancingLinesXColumn search, minColumn;

        minColumn = search = (DancingLinesXColumn) root.getRight();

        while (search != root) {
            if (search.getSize() < minSize) {
                minColumn = search;
                minSize = minColumn.getSize();
                if (minSize == 0) {
                    break;
                }
            }
            search = (DancingLinesXColumn) search.getRight();
        }
        if (minSize == 0) {
            return null;
        } else {
            return minColumn;
        }
    }

    public void addNode(int columnIndex, int rowIndex) {
        nodes[++numberOfNodesAllocated] = new DancingLinesXNode(colHeaders[columnIndex], rowIndex);
        if (previousRowIndex == rowIndex) {
            nodes[numberOfNodesAllocated].setLeft(lastNodeAdded);
            nodes[numberOfNodesAllocated].setRight(lastNodeAdded.getRight());
            lastNodeAdded.setRight(nodes[numberOfNodesAllocated]);
            nodes[numberOfNodesAllocated].getRight()
                    .setLeft(nodes[numberOfNodesAllocated]);
        } else {
            previousRowIndex = rowIndex;
            nodes[numberOfNodesAllocated].setLeft(nodes[numberOfNodesAllocated]);
            nodes[numberOfNodesAllocated].setRight(nodes[numberOfNodesAllocated]);
        }
        lastNodeAdded = nodes[numberOfNodesAllocated];
    }

    /**
     * Determines the number of solutions that can be found for this grid.
     *
     * @param solveType
     *         The solve type to be used to determine the number of solutions.
     * @return The number of solutions, given the solution type, that can be found for this grid.
     */
    @SuppressWarnings("SameParameterValue")
    public int solve(SolveType solveType) {
        if (!isValid) {
            return -1;
        }

        this.solveType = solveType;
        countSolutions = 0;
        complexity = 0;
        search(trySolution.size());
        return countSolutions;
    }

    private void search(int k) {
        DancingLinesXColumn chosenCol;
        DancingLinesX2DNode r, j;

        // A solution is found in case all columns are covered
        if (root.getRight() == root) {
            countSolutions++;
            foundSolution = new ArrayList<Integer>(trySolution);
            if (GridSolver.DEBUG) {
                Log.i(TAG,
                      "Solution " + countSolutions + " found which consists of following moves: " + trySolution
                              .toString());
            }
            return;
        }

        // In case no solution is yet found, select the next column to be
        // covered. Now two things can happen. Either such a column can be
        // found, and the puzzle solving will be taken one level deeper. Or such
        // a column can not be found in which case a backtrack will be done. The
        // more often a permutation is tried, the harder to solve the puzzle is.
        complexity++;

        chosenCol = chooseMinCol();
        if (chosenCol != null) {

            coverCol(chosenCol);
            r = chosenCol.getDown();

            while (r != chosenCol) {
                if (k >= trySolution.size()) {
                    trySolution.add(((DancingLinesXNode) r).getRowIdx());
                } else {
                    trySolution.set(k, ((DancingLinesXNode) r).getRowIdx());
                }
                j = r.getRight();
                while (j != r) {
                    coverCol(((DancingLinesXNode) j).getColumn());
                    j = j.getRight();
                }
                search(k + 1);
                if (solveType == SolveType.ONE && countSolutions > 0) {
                    // Stop as soon as we find 1 solution
                    return;
                }
                if (solveType == SolveType.MULTIPLE && countSolutions > 1) {
                    // Stop as soon as we find multiple solutions
                    return;
                }
                j = r.getLeft();
                while (j != r) {
                    uncoverCol(((DancingLinesXNode) j).getColumn());
                    j = j.getLeft();
                }
                r = r.getDown();
            }
            uncoverCol(chosenCol);
        }
    }

    public int getComplexity() {
        return complexity;
    }
}
